(ns com.eighthlight.filament.forms
  (:require-macros [hiccup.core :as h])
  (:require [clojure.string :as str]
            [domina :as dom]
            [domina.css :as css]
            [goog.dom.forms]))


(defn form-data
  "Takes a form node, and returns a map of all the data in the form."
  [form]
  (let [form-map (goog.dom.forms.getFormDataMap (dom/single-node form))]
    (reduce
      (fn [result key]
        (let [value (.get form-map key)]
          (if (and value (= 1 (count value)))
            (assoc result (keyword key) (first value))
            (assoc result (keyword key) value))))
      {}
      (.getKeys form-map))))

(defn display-errors
  "Highlights all the errors for a given model and display the error messages next to the field.

  Assumes:
   - (:errors model) will return a Metis style map of errors eg. {:field-name [\"first error\" \"second error\"]}
   - each input node has an id that matches the model's field name.

  CSS:
    .field-error - is applied to input nodes that have an error
    p.field-errors - contains the error messages and is added to the dom before each error input"
  [model]
  (doseq [[field errors] (:errors model)]
    (let [error-message (str/join ", " (map #(str (name field) " " %) errors))
          input (dom/by-id (name field))]
      (dom/add-class! input "field-error")
      (dom/insert-before! input (h/html [:p.field-errors error-message])))))

(defn teardown-errors
  "Removed all the error messages and highlighting done by `display-errors`"
  []
  (dom/detach! (css/sel "p.field-errors"))
  (dom/remove-class! (css/sel ".field-error") "field-error"))