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
    (println "Use `lein gnome restart` to pick up changes")))

(defn eval-in-shell [code]
  (sh/sh "dbus-send" "--session" "--type=method_call" "--dest=org.gnome.Shell" "/org/gnome/Shell" "org.gnome.Shell.Eval" (str "string:" code)))

(defn restart [project & args]
  (eval-in-shell "global.reexec_self()"))

(defn gnome
  "Operate on Gnome Shell extensions.

Subtasks: install restart"
  [project & [task args]]
  (cond (= task "install") (apply install project args)
        (= task "restart") (apply restart project args)
        :else (help/help "gnome")))
