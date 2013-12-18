(ns com.eighthlight.filament.forms-spec
  (:require-macros [hiccup.core :as h]
                   [specljs.core :refer [describe context it should= with before around should-contain should-not-contain]])
  (:require [com.eighthlight.filament.forms :as forms]
            [com.eighthlight.filament.spec-helper :as helper]
            [domina :as dom]
            [domina.css :as css]
            [hiccup.core]
            [specljs.core]))

(describe "Forms"

  (helper/with-clean-dom)

  (it "collects all the data on a form"
    (dom/append! (css/sel "body")
      (h/html [:form#test-form [:input {:type "text" :name "red" :value "red"}]
               [:input {:type "checkbox" :name "orange" :checked true}]
               [:input {:type "checkbox" :name "yellow"}]
               [:input {:type "radio" :name "green" :value "grass"}]
               [:input {:type "radio" :name "green" :value "pea" :checked true}]
               [:select {:name "blue"}
                [:option {:value "sky"}]
                [:option {:value "sea" :selected true}]]
               [:textarea {:name "indigo"} "like blue"]]))
    (should= {:red "red", :orange "on", :green "pea", :blue "sea", :indigo "like blue"}
      (forms/form-data (dom/by-id "test-form"))))
  )
