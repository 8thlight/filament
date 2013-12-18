(ns com.eighthlight.filament.util-spec
  (:require-macros [hiccup.core :as h]
                   [specljs.core :refer [should should-not describe context it should= with before]])
  (:require [com.eighthlight.filament.spec-helper :as helper]
            [com.eighthlight.filament.util :as util]
            [domina :as dom]
            [domina.css :as css]
            [hiccup.core]
            [specljs.core]))

(describe "Util"

  (helper/with-clean-dom)

  (context "Flash container"

    (with flash-div (dom/single-node (h/html [:div#flash-container])))
    (before (dom/append! (css/sel "body") @flash-div))

    (it "displays an error in the flash container"
      (util/flash-error "That's why you always leave a note!")
      (should= "That's why you always leave a note!"
        (dom/text (css/sel "#flash-container"))))

    (it "displays a default error in the flash container"
      (util/flash-error "")
      (should= "There was a problem."
        (dom/text (css/sel "#flash-container"))))

    (it "removes errors from the flash container"
      (util/flash-error "I'm a monster!")
      (util/clear-flash)
      (should= ""
        (dom/text (css/sel "#flash-container")))))

  (context "DOM helpers"
    (with blink-tag (dom/single-node (h/html [:blink#annoying])))
    (before (dom/append! (css/sel "body") @blink-tag))

    (it "returns the id of a selected element"
      (should= "annoying"
        (util/element-id (css/sel "blink"))))

    (it "returns whether a search term is not blank"
      (should (util/not-blank? "search term"))
      (should-not (util/not-blank? ""))
      (should-not (util/not-blank? "       ")))))
