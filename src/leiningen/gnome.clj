(ns leiningen.gnome
  (:refer-clojure :exclude [compile])
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [leiningen.cljsbuild :as cljs]
            [leiningen.help :as help]
            [leiningen.core.main :as main]
            [cheshire.core :as json]))

(defn uuid [project]
  (format "%s@%s" (:name project) (:group project)))

(defn out-dir [project]
  (str (io/file (:target-path project) "extension")))

(defn metadata-for [project]
  (json/encode (assoc (select-keys project [:name :description :shell-version])
                 :uuid (uuid project))))

(defn install [project & args]
  (let [install-dir (format "%s/.local/share/gnome-shell/extensions/%s"
                            (System/getProperty "user.home") (uuid project))]
    (.mkdirs (io/file install-dir))
    (doseq [file (.listFiles (io/file (out-dir project)))]
      (io/copy file (io/file install-dir (.getName file))))
    (println "Copied extension to" install-dir "directory.")
    (println "Press Alt+F2 r ENTER to reload.")))

(defn compile [project & args]
  (let [out (out-dir project)
        [js metadata stylesheet] (map (comp str (partial io/file out))
                                      ["extension.js"
                                       "metadata.json"
                                       "stylesheet.css"])
        ;; TODO: use verbose nested :builds-style for :cljsbuild config
        project (-> project
                    (update-in [:cljsbuild :builds :compiler :source-path] #(or % "src"))
                    (update-in [:cljsbuild :builds :compiler :output-to] #(or % js)))]
    (cljs/cljsbuild project "once")
    (spit metadata (metadata-for project))
    ;; TODO: honor :source-paths?
    (io/copy (io/file (:root project) "src" "stylesheet.css") (io/file stylesheet))
    (main/info "Wrote extension:" out)))

(defn gnome
  "Operate on Gnome Shell extensions.

Subtasks: compile and and install."
  [project & [task args]]
  (cond (= task "compile") (apply compile project args)
        (= task "install") (apply install project args)
        :else (help/help "gnome")))
