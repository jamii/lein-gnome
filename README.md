Bringing the magic of ClojureScript to the desktop via Gnome Shell extensions.

Note: currently the version of lein-gnome on clojars is from technomancy's repo. To use this fork you must do:

```
git clone https://github.com/jamii/lein-gnome.git
cd lein-gnome
lein install
```

## Template

``` bash
$ cat ~/.lein/profiles.clj
{:user {:plugins [[lein-gnome/lein-template "0.1.0-SNAPSHOT"]]}}
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
Copied extension to ~/.local/share/gnome-shell/extensions/myextension@example.com directory.
Use `lein gnome restart` to pick up changes
```

The template is a direct port of the example extension created by `gnome-shell-extension-tool --create-extension`.

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
dbus-monitor "interface='org.gnome.Shell.Extensions'" | grep $MY-PROJECT-UUID
```

The first three are not filtered because many of the errors you can cause will not include the uuid of your extension.

Expect to see lots of errors when starting `lein gnome log` since not all of the sources will exist on your machine.

You will also see a lot of `JS ERROR: !!!   WARNING: 'variable self__ redeclares argument'`. That is because cljs generates lots of code like this:

``` javascript
cljs.core.PersistentTreeSet.prototype.apply = function(self__, args4038) {
  var self__ = this;
  ...
```

## Gotchas

As of Gnome Shell 3.8.2 if your group name (example.com) does not have at least one period in it your extension will not be recognised.

If an exception is thrown in your `init` or `enable` functions, your extension will not start. If this happens when (re)starting gnome-shell then the error will be logged and will also appear in Looking Glass under the extensions tab. If this happens when reloading the extension the error will be silently discarded.

If an exception is thrown in your `disable` function, reloading your extension will silently fail. The error will not even be logged. Try calling the disable function manually in the repl to be sure.

There is a GetExtensionErrors dbus method exposed by gnome-shell but it never returns anything.

In general gjs seems happy to throw away exceptions. Please let me know if you find more cases like the above and I will start adding wrappers to deal with them.

## Resources

Gjs is not documented but the c libraries are. [This guide](http://mathematicalcoffee.blogspot.com/2012/09/developing-gnome-shell-extensions.html) explains the mapping between c names and gjs names. Bear in mind though that some c libs (eg libsoup) are not direct bindings in gjs but have been modified to be more idiomatic, in which case reading the [js source](https://git.gnome.org/browse/gnome-shell/tree/js) can be enlightening.

The [Looking Glass repl](https://live.gnome.org/GnomeShell/LookingGlass) that ships with gnome-shell does not support copy/paste of history and runs a modal window. Everything but the picker works better in the cljs repl. The picker may appear in a later version of lein-gnome.

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
