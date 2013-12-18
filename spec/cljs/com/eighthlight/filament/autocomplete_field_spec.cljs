(ns com.eighthlight.filament.autocomplete-field-spec
  (:require-macros [hiccup.core :as h]
                   [specljs.core :refer [describe context it should= should-contain with before around should-invoke should-not-invoke]])
  (:require [com.eighthlight.filament.async :as async]
            [com.eighthlight.filament.autocomplete-field :as ac]
            [com.eighthlight.filament.fx :as fx]
            [com.eighthlight.filament.spec-helper :as helper]
            [com.eighthlight.filament.util :as util]
            [domina :as dom]
            [domina.css :as css]
            [domina.events :as event]
            [hiccup.core]
            [specljs.core]))

(describe "Autocomplete Field"

  (helper/with-clean-dom)
  (before
    (dom/set-html! (css/sel "body") (h/html [:input#testfield {:type "text" :name "testfield"}])))
  (with input (dom/by-id "testfield"))
  (with trail (atom []))

  (it "arms a text field with search behavior"
    (should-not-invoke
      async/handle-throttled-events {}
      (ac/arm-field @input {:on-search #(swap! @trail conj :armed)})
      (dom/set-value! @input "change")
      (event/dispatch! @input :keyup {})
      (should= [:armed] @@trail)))

  (it "arms a text field with throttled search behavior"
    (should-invoke
      async/handle-throttled-events {:with [:* :* :* 500]}
      (ac/arm-field @input {:throttle 500 :on-search #(swap! @trail conj :armed)})
      (dom/set-value! @input "change")
      (event/dispatch! @input :keyup {})))

  (it "returns the value of the armed text field when searching"
    (ac/arm-field @input {:on-search #(swap! @trail conj %)})
    (dom/set-value! @input "Piper")
    (event/dispatch! @input :keyup {})
    (should= ["Piper"] @@trail))

  (it "DOESN'T invoke callback if text hasn't changed"
    (dom/set-value! @input "Piper")
    (ac/arm-field @input {:on-search #(swap! @trail conj %)})
    (event/dispatch! @input :keyup {})
    (should= [] @@trail))

  (it "DOES invoke callback if text hasn't changed but arrow is pressed"
    (dom/set-value! @input "Piper")
    (ac/arm-field @input {:on-search #(swap! @trail conj %)})
    (event/dispatch! @input :keyup {"keyCode" util/DOWN_ARROW})
    (should= ["Piper"] @@trail))

  (it "DOESN'T invoke callback if text hasn't changed, arrow is pressed, but dropdown is open"
    (dom/set-value! @input "Piper")
    (ac/arm-field @input {:on-search #(swap! @trail conj %)})
    (ac/show-dropdown @input [])
    (event/dispatch! @input :keyup {"keyCode" util/DOWN_ARROW})
    (should= [] @@trail))

  (context "Dropdown"

    (with dropdown (ac/show-dropdown @input [["one" "1"] ["two" "2"] ["three" "3"]]))
    (before
      (ac/arm-field @input {:on-search #(swap! @trail conj (str "search:" %))
                            :on-select #(swap! @trail conj (str "select:" (second %)))})
      @dropdown)

    (it "displays the correct number of callback results"
      (should= 3 (count (ac/active-options))))

    (it "displays the content of the callback results"
      (should= ["one" "two" "three"] (map dom/text (ac/active-options))))

    (it "only displays results if a list does not already exist"
      (ac/show-dropdown @input [["one" "1"] ["two" "2"] ["three" "3"] ["four" "4"]])
      (should= 1 (count (dom/nodes (css/sel "ul"))))
      (should= 4 (count (dom/nodes (css/sel "li")))))

    (it "handles selection of item with click"
      (event/dispatch! (second (ac/active-options)) :mousedown {})
      (should= ["select:2"] @@trail))

    (it "highlights nothing by default"
      (should= nil (ac/highlighted-option)))

    (it "down arrow highlights the next item"
      (event/dispatch! @input :keydown {"keyCode" util/DOWN_ARROW})
      (should= ["highlighted"] (dom/classes (first (ac/active-options))))
      (should= nil (dom/classes (second (ac/active-options)))))

    (it "up arrow highlights the previous item"
      (event/dispatch! @input :keydown {"keyCode" util/DOWN_ARROW})
      (event/dispatch! @input :keydown {"keyCode" util/UP_ARROW})
      (should= ["highlighted"] (dom/classes (first (ac/active-options))))
      (should= nil (dom/classes (second (ac/active-options)))))

    (it "pressing return selects item"
      (event/dispatch! @input :keydown {"keyCode" util/DOWN_ARROW})
      (event/dispatch! @input :keyup {"keyCode" util/ENTER})
      (should= "select:1" (first @@trail))
      (should= nil (ac/dropdown)))

    (it "goes away on when the input loses focus"
      (event/dispatch! @input :blur {})
      (should= nil (ac/dropdown)))

    (it "goes away on ECS pressed"
      (event/dispatch! @input :keydown {"keyCode" util/ESC})
      (should= nil (ac/dropdown)))

    )
  )

