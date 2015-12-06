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
    [clojure.string :as str]
    ))


(defonce *state (atom { :count 0
                        :message "Hello, world!"
                        :db db/conn
                         }))

(enable-console-print!)

(comment 
(keys/register "ctrl+enter" #(print "HELP!"))
(keys/register "g" #(print "ggg!"))
(keys/register "shift+/" #(do
  (print "?????")
  (swap! *state assoc :message "???" )
  ))
)

;; ui 



(defn ^:export refresh []
  (s/send! "refreshed")
  
  (rum/mount (ui/app *state) (js/document.querySelector "#app"))
  (swap! *state assoc :db (db/from-local-storage!))
)


(db/listen! :persistence
  (fn [tx-report] ;; FIXME do not notify with nil as db-report
                  ;; FIXME do not notify if tx-data is empty
    (when-let [db (:db-after tx-report)]
      (js/setTimeout #(db/persist db) 0))))


(db/listen! :render (fn [_] 
    (refresh)))


