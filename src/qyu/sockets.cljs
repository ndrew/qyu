(ns qyu.sockets
  (:require
    [cognitect.transit :as t]))


(defn read-transit-str [s]
  (t/read (t/reader :json) s))


(defn write-transit-str [o]
  (t/write (t/writer :json ) o))


(defonce api-url (str "ws://" js/location.host "/api/websocket"))

(defonce socket (js/WebSocket. api-url))

(defn on-open! [cb]
  (doto socket
    (aset "onopen" cb))
  )

(defn on-msg! [cb]
  (doto socket
    (aset "onmessage" (fn [payload]
          (let [message (read-transit-str (.-data payload))]
            (cb message))))))


(defn send! [message]
  (when (== 1 (.-readyState socket)) ;; WS_OPEN
    (.send socket (write-transit-str message))))

