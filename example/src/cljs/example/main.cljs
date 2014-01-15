(ns example.main
  (:require [example.autocomplete-demo :refer [demo-autocomplete]]
            [example.flash-demo :refer [demo-flash]]
            [example.forms-demo :refer [demo-forms]]
            [example.modal-demo :refer [demo-modal]]))

(defn ^:export init []
  (demo-autocomplete)
  (demo-flash)
  (demo-forms)
  (demo-modal))