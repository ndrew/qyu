(defproject qyu "0.1.0-SNAPSHOT"
  :dependencies [
    [org.clojure/clojure        "1.7.0"]
    [org.clojure/clojurescript  "1.7.189"]
    [rum                        "0.6.0"]
    [http-kit                   "2.1.19"]
    [compojure                  "1.4.0" :exclusions [commos-codec]]
    [com.cognitect/transit-clj  "0.8.285"]
    [com.cognitect/transit-cljs "0.8.232"]

    [datascript "0.13.0"]
    [datascript-transit "0.2.0"]

    [javax.servlet/servlet-api "2.5"]

    [ring/ring-devel "1.1.8"]
    [me.raynes/fs "1.4.6"]
  ]

  :plugins [
    [lein-cljsbuild "1.1.1"]
    [lein-figwheel  "0.5.0-2"]
  ]

  :aliases      { "package" ["do"
                             "cljsbuild" "once" "advanced,"
                             "uberjar"] }
  :aot          [ qyu.server ]
  :uberjar-name "qyu.jar"
  :uberjar-exclusions [#"public/js/out"]


  :main         qyu.server
  :figwheel     { :ring-handler  "qyu.server/app"
                  :css-dirs     ["resources/public"]
                  :server-port   8080
                  :repl          false }

  :cljsbuild {
    :builds [
      { :id           "none"
        :source-paths ["src"]
        :figwheel     { :on-jsload      "qyu.app/refresh" }
        :compiler     { :optimizations  :none
                        :main           qyu.app
                        :asset-path     "/js/out"
                        :output-to      "resources/public/js/main.js"
                        :output-dir     "resources/public/js/out"
                        :source-map     true
                        :compiler-stats true } }

      { :id           "advanced"
        :source-paths ["src"]
        :compiler     { :optimizations  :advanced
                        :main           qyu.app
                        :output-to      "resources/public/js/main.js"
                        :compiler-stats true
                        :pretty-print   false
                        :pseudo-names   false } }
  ]}
)
