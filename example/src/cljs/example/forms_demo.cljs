(ns example.forms-demo
  (:require-macros [hiccup.core :as h])
  (:require [clojure.string :as string]
            [com.eighthlight.filament.forms :as forms]
            [com.eighthlight.filament.util :as util]
            [domina :as dom]
            [hiccup.core]))

(def dom [:div.example
          [:h2 "com.eighthlight.filament.forms"]
          [:p "Click on the buttons below to see how their corresponding functions behave on the following form."]
          [:button#form-data "Demonstrate form-date"]
          [:button#show-errors "Demonstrate display-errors"]
          [:button#clear-errors "Demonstrate teardown-errors"]
          [:form#forms-demo-form {:action "/fooey" :autocomplete "off"}
           [:div
            [:label {:for "field1"} "Field 1:"]
            [:input#field1 {:name "field1" :type "text" :value "1"}]]
           [:div
            [:label {:for "field2"} "Field 2:"]
            [:select#field2 {:name "field2"}
             [:option "Yes"]
             [:option "No"]
             [:option "Maybe"]]]
           [:div
            [:label {:for "field3"} "Field 3:"]
            [:textarea#field3 {:name "field3"} "Some content"]]]])

(defn show-form-data []
  (js/alert (pr-str (forms/form-data (dom/by-id "forms-demo-form")))))

(def model-with-errors {:errors {:field1 ["has one error"]
                                 :field2 ["first error" "second error"]
                                 :field3 ["last error"]}})
(defn display-errors []
  (forms/display-errors model-with-errors))

(defn demo-forms []
  (dom/append! (.-body js/document) (h/html dom))
  (util/override-click! (dom/by-id "form-data") show-form-data)
  (util/override-click! (dom/by-id "show-errors") display-errors)
  (util/override-click! (dom/by-id "clear-errors") forms/teardown-errors)
  )

