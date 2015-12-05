(ns qyu.app
  (:require
    [rum.core :as rum]
    [cognitect.transit :as t]))


(enable-console-print!)



(defonce conn (atom {})) 

(rum/defc aaa [db]
  [:.container 
    [:.toolbar
      [:.logo "qyu"]


      [:ul 
        [:li [:input.search {:type "search"}] ]
        [:li [:button "?"]]
        ]

    ]
    [:.app
      
      [:.links
        [:.header "Today"]
        [:.link 
          [:a {:href "#"} "A sample link!"] [:span.status "opened 1 day ago"] ]
      ]
    ]]
)


(defn read-transit-str [s]
  (t/read (t/reader :json) s))


(defn write-transit-str [o]
  (t/write (t/writer :json ) o))


(defonce *state (atom { :count 0
                        :message "Hello, world!" }))

(declare send!)


(defonce socket
  (doto (js/WebSocket. (str "ws://" js/location.host "/api/websocket"))
    (aset "onmessage"
      (fn [payload]
        (let [message (read-transit-str (.-data payload))]
          (swap! *state #(-> %
                           (update :count inc)
                           (assoc :message message))))))
    (aset "onopen"
      #(send! "connected"))))


(defn send! [message]
  (when (== 1 (.-readyState socket)) ;; WS_OPEN
    (.send socket (write-transit-str message))))


(defonce conn (atom {})) ;; 

(rum/defc app < rum/reactive []
  (let [state (rum/react *state)]
  [:.container 
    [:.toolbar
      [:.logo "qyu"]

      [:ul 
        [:li [:input.search {:type "search"}] ]
        [:li [:button "?"]]
        ]
    ]

    [:.app
      [:.links
        [:.header "Today"]
        [:.link 
          [:a {:href "#"} "A sample link!"] [:span.status "opened 1 day ago"] ]
      ]

  ;;   
     [:hr] 
      [:div "[" (:count state) "] " (pr-str (:message state)) ]


    ]])
)


(defn ^:export refresh []
  (send! "refreshed")
  (rum/mount (app) (js/document.querySelector "#app")))

