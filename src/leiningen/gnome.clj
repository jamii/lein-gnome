(ns leiningen.gnome
  (:refer-clojure :exclude [compile])
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [leiningen.help :as help]
            [leiningen.core.main :as main]
            [cheshire.core :as json]
            [cljs.repl.gnome.client :as client]))

(defn uuid [project]
  (get-in project [:gnome-shell :uuid]))

(defn metadata [project]
  (json/encode {:name (get-in project [:gnome-shell :name])
                :description (:description project)
                :shell-version (get-in project [:gnome-shell :supported-versions])
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
                            (System/getProperty "user.home") (uuid project))
        {:keys [extension stylesheet]} (:gnome-shell project)]
    (println "Installing to" install-dir "...")
    (assert (and extension (.exists (io/file extension))) (str "Could not find extension at " (prn-str extension)))
    (assert (and stylesheet (.exists (io/file stylesheet))) (str "Could not find stylesheet at " (prn-str stylesheet)))
    (.mkdirs (io/file install-dir))
    (spit (io/file install-dir "metadata.json") (metadata project))
    (io/copy (io/file extension) (io/file install-dir "extension.js"))
    (io/copy (io/file stylesheet) (io/file install-dir "stylesheet.css"))
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

(defn print-stream [stream]
  (doseq [line (line-seq (io/reader stream))] (println line)))

(defn print-output-of [commands]
  (let [process (.. Runtime getRuntime (exec (into-array String commands)))]
    (future (print-stream (.getErrorStream process)))
    ;; .getInputStream returns the stdout stream :(
    (future (print-stream (.getInputStream process))))

(defn log [project]
  (print-output-of "journalctl" "-q" "-f" "-n" "0" "_COMM=gnome-session")
  (print-output-of "tail" "-F" ".xsession-errors")
  (print-output-of "tail" "-F" ".cache/gdm/session.log")
  (print-output-of "dbus-monitor" "interface='org.gnome.Shell.Extensions'")
  ;; wait for C-c
  (.join (Thread/currentThread)))

(defn gnome
  "Operate on Gnome Shell extensions.

Subtasks:
  install
  uninstall
  enable
  disable
  reload (restarts your extension)
  restart (restarts gnome-shell)
  repl :js-host localhost :js-port 6034 :clj-host localhost :clj-port 6044
  log (tails the gnome-session logs)
  metadata"
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
    "metadata" (println (metadata project))
    (println (help/help-for "gnome"))))
