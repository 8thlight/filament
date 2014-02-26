(ns com.eighthlight.filament.forms-spec
  (:require-macros [hiccup.core :as h]
                   [speclj.core :refer [describe context it should= with before around should-contain should-not-contain]])
  (:require [com.eighthlight.filament.forms :as forms]
            [com.eighthlight.filament.spec-helper :as helper]
            [domina :as dom]
            [domina.css :as css]
            [hiccup.core]
            [speclj.core]))

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

  (context "with a field"

    (with field (dom/single-node (h/html [:div
                                          [:label {:for "one"} "Label"]
                                          [:input#one {:type "text" :name "one"}]])))
    (before (dom/append! (css/sel "body") @field))

    (it "displays an error on a form field"
      (forms/display-errors {:errors {:one ["is lonely"]}})

      (let [errors (dom/nodes (css/sel "p.field-errors"))]
        (should= 1 (count errors))
        (should= "one is lonely" (dom/text (first errors))))
      (should-contain "field-error" (dom/classes (dom/by-id "one"))))

    (it "displays multuple errors on a form field"
      (forms/display-errors {:errors {:one ["is lonely" "is solitary"]}})

      (let [errors (dom/nodes (css/sel "p.field-errors"))]
        (should= 1 (count errors))
        (should= "one is lonely, one is solitary" (dom/text (first errors)))))

    (it "removes errors"
      (forms/display-errors {:errors {:one ["is lonely" "is solitary"]}})
      (forms/teardown-errors)

      (should-not-contain "field-error" (vec (dom/classes (dom/by-id "one"))))
      (should= 0 (count (dom/nodes (css/sel "p.field-errors")))))

    )
  )
