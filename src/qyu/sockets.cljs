(ns qyu.sockets
  (:require
    [cognitect.transit :as t]))


(defn read-transit-str [s]
  (t/read (t/reader :json) s))


(defn write-transit-str [o]
  (t/write (t/writer :json ) o))


(declare send!)


(defonce socket
  (doto (js/WebSocket. (str "ws://" js/location.host "/api/websocket"))
    (aset "onmessage"
      (fn [payload]
        (let [message (read-transit-str (.-data payload))]
          ;(swap! *state #(-> %
          ;                 (update :count inc)
          ;                 (assoc :message message)))

          )))
    (aset "onopen"
      #(send! "connected"))))


(defn send! [message]
  (when (== 1 (.-readyState socket)) ;; WS_OPEN
    (.send socket (write-transit-str message))))

