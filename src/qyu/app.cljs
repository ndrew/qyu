(ns qyu.app
  (:require
    [rum.core :as rum]
    [cognitect.transit :as t]

    [qyu.keys :as keys]
    [qyu.db :as db]
    [qyu.sockets :as s]
    [qyu.ui :as ui]


    [datascript.core :as d]
    [datascript.transit :as dt]

    [clojure.set :as set]
    [clojure.string :as str])
  )

(enable-console-print!)

(defonce api-url (str "ws://" js/location.host "/api/websocket"))

(defonce *state (atom { 
  :db db/conn

  :count 0
  :message "Hello, world!"
                         }))


(declare socket)

(defn on-socket-open[] 
  (s/send! socket "connected"))


(defn on-socket-msg[message] 
  (swap! *state #(-> %
          (update :count inc)
          (assoc :message message)))) 


(defonce socket (s/socket api-url on-socket-open on-socket-msg))
      


(comment 
(keys/register "ctrl+enter" #(print "HELP!"))
(keys/register "g" #(print "ggg!"))
(keys/register "shift+/" #(do
  (print "?????")
  (swap! *state assoc :message "???" )
  ))
)



(defn ^:export refresh []
  (s/send! socket "refreshed")

  (swap! *state assoc :db (db/from-local-storage!))
  
  (rum/mount (ui/app *state) (js/document.querySelector "#app")))

(db/listen! :persistence
  (fn [tx-report] ;; FIXME do not notify with nil as db-report
                  ;; FIXME do not notify if tx-data is empty
    (when-let [db (:db-after tx-report)]
      (js/setTimeout #(db/persist db) 0))))


(db/listen! :render (fn [_] 
  (refresh)))
