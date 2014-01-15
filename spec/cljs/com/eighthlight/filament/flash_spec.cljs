(ns com.eighthlight.filament.flash-spec
  (:require-macros [hiccup.core :as h]
                   [specljs.core :refer [describe context it should= should-not= with before around should-contain should-not-contain]])
  (:require [com.eighthlight.filament.flash :as flash]
            [com.eighthlight.filament.spec-helper :as helper]
            [domina :as dom]
            [domina.css :as css]
            [hiccup.core]
            [specljs.core]))

(describe "Flash"

  (helper/with-clean-dom)

  (it "adds a notice flash"
    (flash/flash-notice! "first notice")
    (let [container (dom/single-node (css/sel "body #flash-container"))]
      (should-not= nil container)
      (let [notices (dom/single-node (css/sel container "#flash-notices"))]
        (should-not= nil notices)
        (should= 2 (count (dom/children notices)))
        (should= "X" (dom/text (first (dom/children notices))))
        (should= "first notice" (dom/text (second (dom/children notices)))))))

  (it "adds an error flash"
    (flash/flash-error! "first error")
    (let [errors (flash/errors-node)]
      (should= (flash/flash-container-node) (.-parentElement errors))
      (should= "flash-errors" (dom/attr errors "id"))
      (should= 2 (count (dom/children errors)))
      (should= "X" (dom/text (first (dom/children errors))))
      (should= "first error" (dom/text (second (dom/children errors))))))

  (it "adds an success flash"
    (flash/flash-success! "first success")
    (let [successes (flash/successes-node)]
      (should= (flash/flash-container-node) (.-parentElement successes))
      (should= "flash-successes" (dom/attr successes "id"))
      (should= 2 (count (dom/children successes)))
      (should= "X" (dom/text (first (dom/children successes))))
      (should= "first success" (dom/text (second (dom/children successes))))))

  (it "closes errors"
    (flash/flash-error! "an error")
    (helper/click! (first (dom/children (flash/errors-node))))
    (should= nil (dom/by-id "flash-errors")))

  (it "closes notices"
    (flash/flash-error! "a notice")
    (helper/click! (first (dom/children (flash/notices-node))))
    (should= nil (dom/by-id "flash-notices")))

  (it "closes successes"
    (flash/flash-error! "a success")
    (helper/click! (first (dom/children (flash/successes-node))))
    (should= nil (dom/by-id "flash-successes")))

  (it "adds multiple notices"
    (flash/flash-notice! "first notice")
    (flash/flash-notice! "second notice")
    (should= 3 (count (dom/children (flash/notices-node))))
    (should= ["X" "first notice" "second notice"] (map dom/text (dom/children (flash/notices-node)))))

  (it "clears all flash"
    (flash/flash-error! "error")
    (flash/flash-notice! "notice")
    (flash/flash-success! "success")

    (flash/clear-flash!)

    (should= nil (dom/by-id "flash-container"))
    (should= nil (dom/by-id "flash-errors"))
    (should= nil (dom/by-id "flash-notices"))
    (should= nil (dom/by-id "flash-successes")))
  )
