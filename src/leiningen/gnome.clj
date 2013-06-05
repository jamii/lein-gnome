(ns leiningen.gnome
  (:refer-clojure :exclude [compile])
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [leiningen.help :as help]
            [leiningen.core.main :as main]
            [cheshire.core :as json]))

(defn shell-version [project]
  (get-in project [:gnome-shell :supported-versions]))

(defn uuid [project]
  (format "%s@%s" (:name project) (:group project)))

(defn metadata [project]
  (json/encode {:name (:name project)
                :description (:description project)
                :shell-version (shell-version project)
                :uuid (uuid project)}))

;; TODO add 'uninstall/clean' command
;; TODO add 'link' command for development

(defn install [project & args]
  (let [install-dir (format "%s/.local/share/gnome-shell/extensions/%s"
                            (System/getProperty "user.home") (uuid project))]
    (.mkdirs (io/file install-dir))
    (spit (io/file install-dir "metadata.json") (metadata project))
    (io/copy (io/file (get-in project [:gnome-shell :extension]))
             (io/file install-dir "extension.js"))
    (io/copy (io/file (get-in project [:gnome-shell :stylesheet]))
             (io/file install-dir "stylesheet.css"))
    (println "Installed extension to" install-dir)
    (println "Press Alt+F2 r ENTER to reload.")))

(defn gnome
  "Operate on Gnome Shell extensions.

Subtasks: install"
  [project & [task args]]
  (cond (= task "install") (apply install project args)
        :else (help/help "gnome")))
