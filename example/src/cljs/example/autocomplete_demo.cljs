(ns example.autocomplete-demo
  (:require-macros [hiccup.core :as h])
  (:require [clojure.string :as string]
            [com.eighthlight.filament.autocomplete-field :as autocomplete]
            [domina :as dom]
            [hiccup.core]))

(def colors ["red"
             "orange"
             "yellow"
             "green"
             "blue"
             "indigo"
             "violet"])

(def dom [:form {:action "/fooey" :autocomplete "off"}
          [:label {:for "autocomplete-field"} "Autocomplete:"]
          [:input#autocomplete-field {:type "text"}]])

(defn autocomplete-on-select [[name value]]
  (dom/set-value! (dom/by-id "autocomplete-field") value))

(defn autocomplete-on-search [q]
  (autocomplete/show-dropdown (dom/by-id "autocomplete-field")
                              (map #(vector % %)
                                   (filter #(re-find (re-pattern q) %) colors))))

(defn demo-autocomplete []
  (dom/append! (.-body js/document) (h/html dom))
  (let [field (dom/by-id "autocomplete-field")]
    (autocomplete/arm-field field {:on-select autocomplete-on-select
                                   :on-search autocomplete-on-search
                                   :smallest-query-length 3})))
