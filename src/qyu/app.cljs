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


(defonce *state (atom { 
  :db db/conn

  :logged-in false

  :count 0
  :message "Hello, world!"
                         }))




(comment 
(keys/register "ctrl+enter" #(print "HELP!"))
(keys/register "g" #(print "ggg!"))
(keys/register "shift+/" #(do
  (print "?????")
  (swap! *state assoc :message "???" )
  ))
)



(defn ^:export refresh []
  
  (s/send! "refreshed")

  (swap! *state assoc :db (db/from-local-storage!))
  
  (rum/mount (ui/app *state) (js/document.querySelector "#app")))


;; init sockets

(defn on-socket-open[] 
  (s/send! "connected"))

(defn on-socket-msg[message] 
  

  (swap! *state #(-> %
          (update :count inc)
          (assoc :message message)))) 

(s/on-open! on-socket-open)
(s/on-msg! on-socket-msg)

;; init listeners 

(db/listen! :persistence
  (fn [tx-report] ;; FIXME do not notify with nil as db-report
                  ;; FIXME do not notify if tx-data is empty
    (when-let [db (:db-after tx-report)]
      (js/setTimeout #(db/persist db) 0))))


(db/listen! :render (fn [_] 
  (refresh)))
