(ns com.eighthlight.filament.forms
  (:require-macros [hiccups.core :as h])
  (:require [clojure.string :as str]
            [domina :as dom]
            [domina.css :as css]
            [goog.dom.forms]))


(defn form-data [form]
  (let [form-map (goog.dom.forms.getFormDataMap (dom/single-node form))]
    (reduce
      (fn [result key]
        (let [value (.get form-map key)]
          (if (and value (= 1 (count value)))
            (assoc result (keyword key) (first value))
            (assoc result (keyword key) value))))
      {}
      (.getKeys form-map))))