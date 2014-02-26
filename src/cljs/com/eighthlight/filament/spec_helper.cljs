(ns com.eighthlight.filament.spec-helper
  (:require-macros [speclj.core :refer [after around before]])
  (:require [domina :as dom]
            [domina.css :as css]
            [domina.events :as event]
            [speclj.core]))

(defn with-clean-dom []
  (after
    (let [body (css/sel "body")]
      (dom/set-inner-html! (dom/single-node body) ""))))

(defn click! [node]
  (event/dispatch! node :click {}))

