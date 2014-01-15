(ns example.flash-demo
  (:require-macros [hiccup.core :as h])
  (:require [clojure.string :as string]
            [com.eighthlight.filament.flash :as flash]
            [com.eighthlight.filament.util :as util]
            [domina :as dom]
            [hiccup.core]))

(def dom [:div.example
          [:h2 "com.eighthlight.filament.flash"]
          [:p "Try clicking the buttons below.
          You'll see the text below added to the page as a flash message.  error, notice, and success use distinct and expressive colors.
          clear-flash! will remove them all."]
          [:input#flash-input {:type "text" :value "A flash message"}]
          [:br]
          [:button#flash-error-button "flash-error!"]
          [:button#flash-notice-button "flash-notice!"]
          [:button#flash-success-button "flash-success!"]
          [:button#clear-flash-button "clear-flash!"]
          [:button#flash-messages-button "flash-messages"]
          ])

(defn demo-flash []
  (dom/append! (.-body js/document) (h/html dom))
  (util/override-click! (dom/by-id "flash-error-button") #(flash/flash-error! (dom/value (dom/by-id "flash-input"))))
  (util/override-click! (dom/by-id "flash-notice-button") #(flash/flash-notice! (dom/value (dom/by-id "flash-input"))))
  (util/override-click! (dom/by-id "flash-success-button") #(flash/flash-success! (dom/value (dom/by-id "flash-input"))))
  (util/override-click! (dom/by-id "clear-flash-button") #(flash/clear-flash!))
  (util/override-click! (dom/by-id "flash-messages-button") #(js/alert (flash/flash-messages))))

