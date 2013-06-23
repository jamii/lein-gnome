(ns leiningen.gnome
  (:refer-clojure :exclude [compile])
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [leiningen.help :as help]
            [leiningen.core.main :as main]
            [cheshire.core :as json]
            [cljs.repl.gnome.client :as client]))

(defn shell-version [project]
  (get-in project [:gnome-shell :supported-versions]))

(defn uuid [project]
  (format "%s@%s" (:name project) (:group project)))

(defn metadata [project]
  (json/encode {:name (:name project)
                :description (:description project)
                :shell-version (shell-version project)
                :uuid (uuid project)}))

(defn dbus-send [command & args]
  (let [{:keys [out err]} (apply sh/sh "dbus-send" "--session" "--type=method_call" "--dest=org.gnome.Shell" "/org/gnome/Shell" command args)]
    (when (seq out) (println out))
    (when (seq err) (println err))))

(defn eval-in-shell [code]
  (dbus-send "org.gnome.Shell.Eval" (str "string:" code)))

(defn reload [project]
  (println "Reloading...")
  (dbus-send "org.gnome.Shell.Extensions.ReloadExtension" (str "string:" (uuid project))))

(defn enable [project]
  (println "Enabling...")
  (eval-in-shell (str "Main.ExtensionSystem.enableExtension('" (uuid project) "')")))

(defn disable [project]
  (println "Disabling...")
  (eval-in-shell (str "Main.ExtensionSystem.disableExtension('" (uuid project) "')")))

;; TODO figure out why this doesnt work
(defn check-errors [project]
  (println "Checking for errors... (this doesnt actually work yet)")
  (dbus-send "org.gnome.Shell.Extensions.GetExtensionErrors" (str "string:" (uuid project))))

(defn install [project]
  (let [install-dir (format "%s/.local/share/gnome-shell/extensions/%s"
                            (System/getProperty "user.home") (uuid project))]
    (println "Installing to" install-dir "...")
    (.mkdirs (io/file install-dir))
    (spit (io/file install-dir "metadata.json") (metadata project))
    (io/copy (io/file (get-in project [:gnome-shell :extension]))
             (io/file install-dir "extension.js"))
    (io/copy (io/file (get-in project [:gnome-shell :stylesheet]))
             (io/file install-dir "stylesheet.css"))
    (enable project)
    (reload project)
    (check-errors project)))

(defn uninstall [project]
  (println "Uninstalling...")
  (dbus-send "org.gnome.Shell.Extensions.UninstallExtension" (str "string:" (uuid project))))

(defn restart []
  (println "Restarting...")
  (eval-in-shell "global.reexec_self()"))

(defn repl [project & args]
  (apply client/run-gnome-repl args))

(defn print-stream [filter stream]
  (doseq [line (line-seq (io/reader stream))]
    (when (or (nil? filter) (.contains line filter))
      (println line))))

(defn print-output-of [filter & commands]
  (let [process (.. Runtime getRuntime (exec (into-array String commands)))]
    (future (print-stream filter (.getErrorStream process)))
    ;; .getInputStream returns the stdout stream :(
    (future (print-stream filter (.getInputStream process)))))

(defn log [project]
  (print-output-of nil "journalctl" "-q" "-f" "-n" "0" "_COMM=gnome-session")
  (print-output-of nil "tail" "-F" ".xsession-errors")
  (print-output-of nil "tail" "-F" ".cache/gdm/session.log")
  (print-output-of (uuid project) "dbus-monitor" "interface='org.gnome.Shell.Extensions'")
  (.join (Thread/currentThread)))

(defn gnome
  "Operate on Gnome Shell extensions.

Subtasks:
  install
  uninstall
  enable
  disable
  reload
  restart
  repl
  log"
  [project & [task args]]
  (condp = task
    "install" (install project)
    "uninstall" (uninstall project)
    "enable" (enable project)
    "disable" (disable project)
    "reload" (reload project)
    "restart" (restart)
    "repl" (apply repl project args)
    "log" (log project)
    (help/help "gnome")))
