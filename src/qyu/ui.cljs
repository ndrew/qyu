(ns qyu.ui
  (:require
    [rum.core :as rum]

    [qyu.keys :as keys]
    [qyu.db :as db]
    [qyu.sockets :as s]

    [clojure.set :as set]
    [clojure.string :as str]
    ))


(defn el [id] (js/document.getElementById id))


(rum/defc header [state]
  [:.toolbar 
      [:.logo "qyu"]
      [:ul 
        ;[:li.search [:input {:type "search"}]]
        
        [:li 
          [:button {:on-click (fn[] 
            ;(db/preload-db!)            
            (s/send! {:load-db true})

            false
            )} "load db"]]


        [:li 
          [:button {:on-click (fn[] 
            (let [serialized (db/get-serialized-db)]
              (s/send! {:db serialized})
              )
            false
            )} "save db"]]


        [:li 
          [:button {:on-click (fn[] 
            (swap! state assoc :db (db/clear-localstorage!))
            (swap! state assoc :links [])

            )} "clean local db"]]


        [:li 
          [:button {:on-click (fn[] 
            (swap! state assoc :current-view :batch-add)
            )} "batch add"]]
        ]
    ]
  )

(rum/defc batch-add [state]
  [:.batch-add
    [:header 
      [:button {:on-click (fn[]
        (let [raw-links (.-value (el "batch-links"))]
          (swap! state merge 
              {:raw-links raw-links
               :current-view :links})

          )
        )} "Add"]
      [:h3 "Add links (in batch)"]
      ]
    [:textarea#batch-links]
    
          ]
  )

(rum/defc links [state]

    [:.app
      [:h3 "Links"]
      (let [v (get @state :current-view :links)]
        (if (= :batch-add v)
          (batch-add state)

          (into [:.links]
            (map #(do
              [:.link
                [:a {
                  :href (:url %)

                  :target "blank"
                  ;:on-click (fn[e]
                  ;  (println e)
                  ;  (.preventDefault e)
                  ;  )
                  } (if (str/blank? (:title %)) (:url %) (:title %)) ]
                ]
              ) (get @state :links []))
            )
          )
          
        )

      
  ;;   
      ;[:hr]
      [:h4.debug "debug-debug-debug-debug-debug-debug-debug"] 
        [:div "[" (:count @state) "] " (pr-str (:message @state)) ]
    ]
)


(rum/defc debug [state]
    [:.state {:key "state"} 
      (pr-str 
        @state
        ;(dissoc state :db)
        )
    ]
)

(rum/defc footer < rum/static [] 
  [:p.footer "Authors: ndrew, aigor"]
  )

(rum/defc app < rum/reactive [*state]
  ;(let [state (rum/react *state)]

  [:.container 
    (header *state)

    [:.info
      [:p 
        "qyu — is a datascript based tool for organizing (kinda) links in localstorage and on the server. Maybe in future it will work. Have fun %)"
      ]

    ]
    
    (links *state)
    (debug *state)
    (footer)
    ]
    ;)
)
