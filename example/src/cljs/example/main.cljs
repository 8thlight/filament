(ns example.main
  (:require [example.autocomplete-demo :refer [demo-autocomplete]]
            [example.forms-demo :refer [demo-forms]]
            [example.modal-demo :refer [demo-modal]]))

(defn ^:export init []
  (demo-autocomplete)
  (demo-forms)
  (demo-modal))