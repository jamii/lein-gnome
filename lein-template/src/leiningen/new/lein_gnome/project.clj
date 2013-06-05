(defproject {{group}}/{{name}} "0.1.0-SNAPSHOT"
  :description "FIXME"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :plugins [[lein-gnome "0.1.0-SNAPSHOT"]
            [lein-cljsbuild "0.3.2"]]
  :cljsbuild {:builds {:main {:source-paths ["src"]
                              :notify-command ["notify-send"]
                              :compiler {:output-to "target/main.js"
                                         :optimizations :whitespace
                                         :pretty-print true}}}}
  :gnome-shell {:supported-versions ["3.8.2"]
                :extension "target/main.js"
                :stylesheet "src/stylesheet.css"})
