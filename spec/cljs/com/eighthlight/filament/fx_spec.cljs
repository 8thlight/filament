(ns com.eighthlight.filament.fx-spec
  (:require-macros [hiccups.core :as h]
                   [specljs.core :refer [describe it should=]])
  (:require [com.eighthlight.filament.fx :as fx]
            [com.eighthlight.filament.spec-helper :as helper]
            [domina :as dom]
            [domina.css :as css]
            [specljs.core]))

(describe "FX"

  (helper/with-clean-dom)

  (it "shows, hides, and detects visibility"
    (let [node (dom/single-node (h/html [:div#test]))]
      (dom/append! (css/sel "body") node)

      (should= true (fx/visible? node))
      (should= false (fx/hidden? node))

      (fx/hide! node)

      (should= false (fx/visible? node))
      (should= true (fx/hidden? node))

      (fx/show! node)

      (should= true (fx/visible? node))
      (should= false (fx/hidden? node)))))
