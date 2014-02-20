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

(def modal-content [:div
                    [:p "This is how you generate a modal using Filament."]
                    [:p "The following inputs should popup their pickers.  When the popups are open, pressing exscape should not close the modal."]
                    [:input {:type "file" :name "file"}][:br]
                    [:input {:type "date" :name "date"}][:br]
                    [:input {:type "text" :name "text"}][:br]
                    [:select {:name "select"} [:option "One"][:option "Two"][:option "Three"]][:br]
                    [:textarea {:name "textarea"}]])

(def modal (modal/create-modal "modal"))

(defn show-modal []
  (modal/populate! modal {:title "Modal" :content (h/html modal-content)})
  (modal/show! modal))

(defn arm-button []
  (util/override-click! (dom/by-id "click-me") show-modal))

(defn demo-modal []
  (dom/append! (.-body js/document) (h/html dom))
  (arm-button))
