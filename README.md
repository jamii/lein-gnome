Bringing the magic of ClojureScript to the desktop via Gnome Shell extensions.

## Template

The included template is a direct port of the example extension created by `gnome-shell-extension-tool --create-extension`.

``` bash
$ cat ~/.lein/profiles.clj
{:user {:plugins [[lein-gnome/lein-template "0.1.0"]]}}
$ lein new lein-gnome myextension example.com
$ cd myextension/
$ tree
.
├── project.clj
└── src
 ├── hello.cljs
 └── stylesheet.css
1 directory, 3 files
$ lein cljsbuild once
[...]
Compiling ClojureScript.
Compiling "/tmp/myextension/target/extension/extension.js" from "src"...
Successfully compiled "[...]/extension.js" in 6.107488105 seconds.
$ lein gnome install
Installing to /home/jamie/.local/share/gnome-shell/extensions/myextension@example.com ...
Enabling...
Reloading...
```

If all went well there should now be a new icon in your top-right panel.

## REPL

The `hello.cljs` module created by `lein-gnome/lein-template` starts a repl server:

``` clojure
(cljs.repl.gnome.server/server :js-port 6034)
```

You can connect to this using:

``` bash
rlwrap lein gnome repl :js-port 6034 :clj-port 6044
```

If you have `:repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}` in your `project.clj` you can also start the repl from inside an existing nrepl using:

``` clojure
(cljs.gnome.repl.client/run-gnome-nrepl :js-port 6034 :clj-port 6044)
```

The repl is currently somewhat limited. You may only have one client connected to a given extension at a time. If you restart the extension you must also restart the repl. Syntax errors will crash the entire repl (as far as I can tell this is the fault of cljs.repl). There is no way to interrupt evaluation since the evalution environment is single-threaded.

## Logs

Output from your extension (with the exception of cljs print functions called inside the repl) is sent to the gnome-session log. Where this is depends on your linux distribution, your version of gnome-shell and the alignment of pluto. Calling `lein gnome log` combines all of:

``` bash
tail -F .xsession-errors
tail -F .cache/gdm/session.log
journalctl -fqn 0 _COMM=gnome-session
dbus-monitor "interface='org.gnome.Shell.Extensions'"
```

The is necessarily noisy because many of the errors you can cause will not include the uuid of your extension.

Expect to see lots of errors when starting `lein gnome log` since not all of the sources will exist on your machine.

You will also see a lot of `JS ERROR: !!!   WARNING: 'variable self__ redeclares argument'`. That is because cljs generates lots of code like this:

``` javascript
cljs.core.PersistentTreeSet.prototype.apply = function(self__, args4038) {
  var self__ = this;
  ...
```

## Gotchas

As of gnome-shell 3.8.2 if your group name (example.com) does not have at least one period in it your extension will not be recognised.

The gnome-shell has a nasty habit of not logging errors thrown by your `init`, `enable` or `disable` functions when using `lein gnome reload`. In addition, if your `disable` function throws an error your extension can not be reloaded at all without restarting gnome-shell. To work around this, `cljs.gnome/defextension!` wraps these functions and catches and logs their errors. Hopefully this is a temporary fix.

## Resources

Gjs is not documented but the c libraries are. [This guide](http://mathematicalcoffee.blogspot.com/2012/09/developing-gnome-shell-extensions.html) explains the mapping between c names and gjs names. Bear in mind though that some c libs (eg libsoup) are not direct bindings in gjs but have been modified to be more idiomatic, in which case reading the [js source](https://git.gnome.org/browse/gnome-shell/tree/js) can be enlightening.

The [Looking Glass repl](https://live.gnome.org/GnomeShell/LookingGlass) that ships with gnome-shell does not support copy/paste of history and runs in a modal window. Everything but the picker works better in the cljs repl. The picker may appear in a later version of lein-gnome.

Gnome libraries in gjs are dynamically loaded on demand. This makes tab completion in Looking Glass more or less useless. Rely on the gnome docs instead. Dynamic loading also interacts strangely with cljs eg typing `js/imports` in the cljs repl will throw an exception but `js/imports.gi.Soup` will not. More on this later...

[Debugging gnome-shell](https://live.gnome.org/GnomeShell/Debugging)

[Extension faq](https://live.gnome.org/GnomeShell/Extensions/FAQ)

[Gjs examples](https://git.gnome.org/browse/gjs/tree/examples/)

[Inspect dbus interfaces](https://live.gnome.org/DFeet/)

[Trace dbus calls](http://www.willthompson.co.uk/bustle/)

[Old UI overview](http://mathematicalcoffee.blogspot.de/2012/09/gnome-shell-javascript-source.html)

[Guidelines for having extensions accepted on the official site](http://blog.mecheye.net/2012/02/requirements-and-tips-for-getting-your-gnome-shell-extension-approved/)

[Cljs <-> js cheat sheet](http://himera.herokuapp.com/synonym.html)

## License

Copyright © 2012 Phil Hagelberg, Jamie Brandon

Distributed under the Eclipse Public License, the same as Clojure.
