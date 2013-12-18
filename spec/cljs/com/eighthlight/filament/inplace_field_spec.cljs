(ns com.eighthlight.filament.inplace-field-spec
  (:require-macros [hiccup.core :as h]
                   [specljs.core :refer [describe context it should= with before around should-contain should-not-contain stub with-stubs should-have-invoked should-not-have-invoked]])
  (:require [com.eighthlight.filament.fx :as fx]
            [com.eighthlight.filament.inplace-field :as inplace]
            [com.eighthlight.filament.modal :as modal]
            [com.eighthlight.filament.spec-helper :as helper]
            [com.eighthlight.filament.util :as util]
            [domina :as dom]
            [domina.css :as css]
            [domina.events :as event]
            [hiccup.core]
            [specljs.core]))

(describe "Inplace Field"

  (with remote-result (atom nil))
  (with remote-stub (stub "remote" {:invoke (fn [model key value callbacks] ((:on-success callbacks) (or @@remote-result value)))}))
  (with field (inplace/create-inplace-field {:key "abc123" :field-name "some value"} :field-name @remote-stub))
  (with value-container (inplace/value-container @field))
  (with value (inplace/value-node @field))
  (with input-container (inplace/input-container @field))
  (with input (inplace/input-node @field))
  (before (dom/append! (css/sel "body") @field))
  (helper/with-clean-dom)
  (with-stubs)

  (context "generation"

    (it "with default input"
      (let [result (dom/single-node (h/html (inplace/inplace-field "abc123")))]
        (should= "INPUT" (.-tagName (inplace/input-node result)))
        (should= "text" (dom/attr (inplace/input-node result) "type"))))

    (it "can supply the input"
      (let [result (dom/single-node (h/html (inplace/inplace-field "abc123" [:div#custom])))]
        (should= "DIV" (.-tagName (inplace/input-node result)))
        (should= "custom" (dom/attr (inplace/input-node result) "id"))))

    (it "gets the value of default"
      (let [result (dom/single-node (h/html (inplace/inplace-field "abc123")))]
        (dom/set-value! (inplace/input-node result) "foo")
        (should= "foo" (inplace/inplace-value (inplace/input-node result)))))

    (it "sets the value of default"
      (let [result (dom/single-node (h/html (inplace/inplace-field "abc123")))]
        (inplace/set-inplace-value (inplace/input-node result) "foobar")
        (should= "foobar" (inplace/inplace-value (inplace/input-node result)))))

    (defmethod inplace/inplace-value "whacky" [input] "WHACKY VALUE!")
    (defmethod inplace/display-value "whacky" [input model field-key] [model field-key])
    (defmethod inplace/set-inplace-value "whacky" [input] "???")

    (it "gets the value from custom inputs"
      (let [result (dom/single-node (h/html (inplace/inplace-field "abc123" [:div#custom {:type "whacky"}])))]
        (should= "WHACKY VALUE!" (inplace/inplace-value (inplace/input-node result)))))

    (it "gets the value from custom inputs"
      (let [result (dom/single-node (h/html (inplace/inplace-field "abc123" [:div#custom {:type "whacky"}])))]
        (should= [{:my :model} :the-field-key]
                 (inplace/display-value (inplace/input-node result) {:my :model} :the-field-key))))

    )

  (it "records activation"
    (let [new-field (dom/single-node (h/html (inplace/inplace-field "test-id")))]
      (dom/append! (css/sel "body") new-field)
      (should= nil (dom/get-data new-field :activated?))
      (inplace/activate-inplace-field!
        new-field
        {:key "123" :kind "foo" :bar "bar"}
        :bar
        @remote-stub)
      (should= true (dom/get-data new-field :activated?))))

  (it "can be constructed"
    (should= "SPAN" (.-tagName @field))
    (should= "inplace-field" (dom/attr @field "class"))
    (should= "some value" (dom/html @value))
    (should= false (fx/hidden? @value-container))
    (should= true (fx/hidden? @input-container)))

  (it "shows the input when clicking on the value"
    (event/dispatch! @value :click {})
    (should= true (fx/hidden? @value-container))
    (should= false (fx/hidden? @input-container))
    (should= "some value" (dom/value @input)))

  (it "focuses on input when clicking on the value"
    (event/dispatch! @value :click {})
    (should= true (fx/focused? @input)))

  (it "saves the value on lost focus"
    (event/dispatch! @value :click {})
    (dom/set-value! @input "new value")
    (event/dispatch! @input :blur {})
    (should-have-invoked "remote" {:with [:* :* "new value" :*]})
    (should= false (fx/hidden? @value-container))
    (should= true (fx/hidden? @input-container))
    (should= "new value" (dom/text @value)))

  (it "esc cancels the edit"
    (event/dispatch! @value :click {})
    (dom/set-value! @input "new value")
    (event/dispatch! @input :keyup {"keyCode" util/ESC})
    (should= false (fx/hidden? @value-container))
    (should= true (fx/hidden? @input-container))
    (should= "some value" (dom/text @value)))

  (it "doesn't save when edit was ESCd"
    (event/dispatch! @value :click {})
    (dom/set-value! @input "new value")
    (event/dispatch! @input :keyup {"keyCode" util/ESC})
    (event/dispatch! @input :blur {})
    (should-not-have-invoked "remote")
    (should= "some value" (dom/text @value)))

  (it "pressing ENTER saves"
    (event/dispatch! @value :click {})
    (dom/set-value! @input "new value")
    (event/dispatch! @input :keyup {"keyCode" util/ENTER})
    (should-have-invoked "remote" {:with [:* :* "new value" :*]})
    (should= :view (dom/get-data @field :mode))
    (should= "new value" (dom/text @value)))

  (it "pressing ENTER doesn't make a request if the value doesn't change"
    (event/dispatch! @value :click {})
    (event/dispatch! @input :keyup {"keyCode" util/ENTER})
    (should-not-have-invoked "remote")
    (should= :view (dom/get-data @field :mode))
    (should= "some value" (dom/text @value)))

  (it "handles errors"
    (event/dispatch! @value :click {})
    (dom/set-value! @input "new BAD value")
    (reset! @remote-result {:errors {:foo ["bar"]}})
    (event/dispatch! @input :blur {})
    (should-have-invoked "remote" {:with [:* :* "new BAD value" :*]})
    (should= true (fx/hidden? @value-container))
    (should= false (fx/hidden? @input-container))
    (should-contain "error" (dom/classes @input))
    (should= false (fx/hidden? (inplace/errors-node @field)))
    (should= "<p>foo bar</p>" (dom/html (inplace/errors-node @field))))

  (it "can still continue with valid edit after errors"
    (event/dispatch! @value :click {})
    (reset! @remote-result {:errors {:foo ["bar"]}})
    (event/dispatch! @input :blur {})

    (reset! @remote-result "new value")
    (event/dispatch! @input :blur {})

    (should= false (fx/hidden? @value-container))
    (should= true (fx/hidden? @input-container))
    (should-not-contain "error" (dom/classes @input))
    (should= "" (dom/html (inplace/errors-node @field)))
    (should= true (fx/hidden? (inplace/errors-node @field))))

  )
