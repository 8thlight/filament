(ns com.eighthlight.filament.collapsable-spec
  (:require-macros [speclj.core :refer [describe it should= with before around]])
  (:require [com.eighthlight.filament.spec-helper :as helper]
            [com.eighthlight.filament.collapsable :as c]
            [com.eighthlight.filament.fx :as fx]
            [domina :as dom]
            [domina.css :as css]
            [domina.events :as event]
            [speclj.core]))

(describe "Collapsable"

  (helper/with-clean-dom)
  (before
    (dom/set-html! (css/sel "body")
      "<div id='switch'/><div id='subject'/>"))
  (with switch (dom/by-id "switch"))
  (with subject (dom/by-id "subject"))

  (it "records activation"
    (should= nil (dom/get-data @switch :activated?))
    (c/activate-collapsable! @switch @subject)
    (should= true (dom/get-data @switch :activated?)))

  (it "activates with visible subject"
    (c/activate-collapsable! @switch @subject)
    (should= ["expanded"] (dom/classes @switch)))

  (it "activates with hidden subject"
    (fx/hide! @subject)
    (c/activate-collapsable! @switch @subject)
    (should= ["collapsed"] (dom/classes @switch)))

  (it "collapses the subject on click"
    (c/activate-collapsable! @switch @subject :animation :none)
    (event/dispatch! @switch :click {})
    (should= false (fx/visible? @subject))
    (should= ["collapsed"] (dom/classes @switch)))

  (it "expands the subject on another click"
    (c/activate-collapsable! @switch @subject :animation :none)
    (event/dispatch! @switch :click {})
    (event/dispatch! @switch :click {})
    (should= true (fx/visible? @subject))
    (should= ["expanded"] (dom/classes @switch)))

  )
