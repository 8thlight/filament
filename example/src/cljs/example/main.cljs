(ns example.main
  (:require [example.autocomplete-demo :refer [demo-autocomplete]]
            [example.modal-demo :refer [demo-modal]]))

(defn ^:export init []
  (demo-autocomplete)
  (demo-modal))