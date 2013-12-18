(ns example.main
  (:require [example.autocomplete-demo :refer [demo-autocomplete]]))

(defn ^:export init []
  (demo-autocomplete))