(defproject lein-gnome "0.1.0-SNAPSHOT"
  :description "Bringing the magic of ClojureScript to the desktop."
  :url "https://github.com/jamii/lein-gnome"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cheshire "4.0.0"]
                 [com.cemerick/piggieback "0.0.4"]
                 [http-kit "2.1.2"]]
  :plugins [[lein-cljsbuild "0.3.2"]]
  :cljsbuild {:builds {:main {:source-paths ["src"]
                              :notify-command ["notify-send"]
                              :compiler {:output-to "target/main.js"
                                         :optimizations :whitespace
                                         :pretty-print true}
                              :jar true}}}
  :eval-in-leiningen true)
