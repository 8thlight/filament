(ns com.eighthlight.filament.modal-spec
  (:require-macros [specljs.core :refer [describe it should= should-not=]])
  (:require [com.eighthlight.filament.fx :as fx]
            [com.eighthlight.filament.modal :as modal]
            [com.eighthlight.filament.spec-helper :as helper]
            [com.eighthlight.filament.util :as util]
            [domina :as dom]
            [domina.css :as css]
            [domina.xpath :as xpath]
            [domina.events :as event]
            [specljs.core]))

(describe "Modal"

  (helper/with-clean-dom)

  (it "creates a model node"
    (let [modal (modal/create-modal "modal-id" "modal-class")
          modal-attrs (dom/attrs modal)]
      (should= "modal-class-backdrop" (:class modal-attrs))
      (should= "modal-id" (:id modal-attrs))))

  (it "creates a modal node with the default class"
    (let [modal (modal/create-modal "modal-id")
          modal-attrs (dom/attrs modal)]
      (should= "modal-backdrop" (:class modal-attrs))
      (should= "modal-id" (:id modal-attrs))))

  (it "show adds the modal and makes it visible"
    (let [modal (modal/create-modal "modal-id")]
      (modal/show! modal)
      (should= modal (dom/by-id "modal-id"))
      (should= true (fx/visible? modal))
      (should= modal js/document.activeElement)))

  (it "treats multiple show! calls for the same modal is idempotent"
    (let [modal (modal/create-modal "modal-id" "custom-modal-class")]
      (modal/show! modal)
      (modal/show! modal)
      (should= 1 (count (dom/nodes (css/sel ".custom-modal-class"))))))

  (it "sets the title"
    (let [modal (modal/create-modal "modal-id")]
      (modal/set-title! modal "Ohai!")
      (should= "Ohai!" (dom/text (css/sel modal ".modal-header h3")))))

  (it "sets the content"
    (let [modal (modal/create-modal "modal-id")
          content "<div class=\"foo\">Here is some content</div>"]
      (modal/set-content! modal content)
      (should= content (dom/html (css/sel modal ".modal-body")))))

  (it "populates"
    (let [modal (modal/create-modal "modal-id")]
      (modal/populate! modal {:title "Hello" :content "Something here"})
      (should= "Hello" (dom/text (css/sel modal ".modal-header h3")))
      (should= "Something here" (dom/text (css/sel modal ".modal-body")))))

  (it "hides"
    (should= nil (dom/by-id "modal-id"))
    (let [modal (modal/create-modal "modal-id")]
      (modal/show! modal)
      (should= modal (dom/by-id "modal-id"))
      (modal/hide! modal)
      (should= nil (dom/by-id "modal-id"))))

  (it "clears"
    (let [modal (modal/create-modal "modal-id")]
      (modal/clear! modal)
      (should= "" (dom/text (css/sel modal ".modal-header h3")))
      (should= "" (dom/text (css/sel modal ".modal-body")))))

  (it "closes modal when clicking on backdrop"
    (let [modal (modal/create-modal "modal-id")]
      (modal/show! modal)
      (event/dispatch! modal :click {})
      (should= nil (dom/by-id "modal-id"))))

  (it "closes the modal when clicking on close button"
    (let [modal (modal/create-modal "modal-id")]
      (modal/show! modal)
      (event/dispatch! (css/sel modal ".modal-close-button") :click {})
      (should= nil (dom/by-id "modal-id"))))

  (it "doesn't close when clicking on modal body"
    (let [modal (modal/create-modal "modal-id")]
      (modal/show! modal)
      (event/dispatch! (css/sel modal ".modal") :click {})
      (should-not= nil (dom/by-id "modal-id"))))

  (it "pressing ESC closes modal"
    (let [modal (modal/create-modal "modal-id")]
      (modal/show! modal)
      (event/dispatch! (css/sel modal ".modal-close-button") :keyup {"keyCode" util/ESC "target" modal})
      (should= nil (dom/by-id "modal-id"))))

  (it "pressing ESC in dropdown doesn't close modal"
    (let [modal (modal/create-modal "modal-id")]
      (modal/set-content! modal "<select id='dropdown'><option>Foo</option></select>")
      (modal/show! modal)
      (let [dropdown (dom/single-node (dom/by-id "dropdown"))]
        (event/dispatch! dropdown :click {})
        (event/dispatch! dropdown :keyup {"keyCode" util/ESC "target" dropdown}))
      (should-not= nil (dom/by-id "modal-id"))))

  (it "pressing space doesn't close the modal"
    (let [modal (modal/create-modal "modal-id")]
      (modal/show! modal)
      (event/dispatch! (css/sel modal ".modal-close-button") :keyup {"keyCode" 32})
      (should= modal (dom/by-id "modal-id"))))

  )