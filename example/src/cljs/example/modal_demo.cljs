(ns example.modal-demo
  (:require-macros [hiccup.core :as h])
  (:require [com.eighthlight.filament.modal :as modal]
            [com.eighthlight.filament.util :as util]
            [domina :as dom]
            [hiccup.core]))

(def dom [:div.example
          [:h2 "com.eighthlight.filament.modal"]
          [:p "Clicking on the button will open a modal.  Try it!"]
          [:button {:id "click-me"} "Click me!"]])

(def modal (modal/create-modal "modal"))

(defn show-modal []
  (modal/populate! modal {:title "Modal" :content "This is how you generate a modal using Filament."})
  (modal/show! modal))

(defn arm-button []
  (util/override-click! (dom/by-id "click-me") show-modal))

(defn demo-modal []
  (dom/append! (.-body js/document) (h/html dom))
  (arm-button))