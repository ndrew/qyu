(ns qyu.sockets
  (:require
    [cognitect.transit :as t]))


(defn read-transit-str [s]
  (t/read (t/reader :json) s))


(defn write-transit-str [o]
  (t/write (t/writer :json ) o))


(defn socket [url onopen onmessage]
  (doto (js/WebSocket. url)
    (aset "onmessage" 
		(fn [payload]
        	(let [message (read-transit-str (.-data payload))]
        		(onmessage message))))
    (aset "onopen" onopen))
  )


(defn send! [socket message]
  (when (== 1 (.-readyState socket)) ;; WS_OPEN
    (.send socket (write-transit-str message))))

