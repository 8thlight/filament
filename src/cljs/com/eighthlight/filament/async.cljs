(ns com.eighthlight.filament.async
  (:require [cljs.core.async :as async :refer [>! alts! chan put! timeout]])
  (:require-macros [cljs.core.async.macros :as async-macros :refer [go]]))

;; utilities from David Nolen's async-tests playground

(defn event-chan
  ([type] (event-chan js/window type))
  ([el type] (event-chan (chan) el type))
  ([c el type]
    (let [writer #(put! c %)]
      (.addEventListener el type writer)
      {:chan c
       :unsubscribe #(.removeEventListener el type writer)})))

(defn throttle
  ([source msecs]
    (throttle (chan) source msecs))
  ([c source msecs]
   (go
     (loop [state ::init
            last nil
            cs [source]]
       (let [[_ sync] cs]
         (let [[v sc] (alts! cs)]
           (condp = sc
             source (condp = state
                      ::init (do (>! c v)
                                 (recur ::throttling last
                                        (conj cs (timeout msecs))))
                      ::throttling (recur state v cs))
             sync (if last
                    (do (>! c last)
                        (recur state nil
                               (conj (pop cs) (timeout msecs))))
                    (recur ::init last (pop cs))))))))
   c))

(defn handle-throttled-events
  ([element event-type handler] (handle-throttled-events element event-type handler 1000))
  ([element event-type handler msecs]
   (let [event-channel (:chan (event-chan element event-type))
         throttled (throttle event-channel msecs)]
     (go
       (while true
         (let [e (<! throttled)]
           (handler e)))))))

