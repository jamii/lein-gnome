# lein-gnome

Bringing the magic of ClojureScript to the desktop via Gnome Shell extensions.

## Usage

Put `lein-gnome/lein-template` in your profile:

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

The `hello.cljs` module is a direct port of the example extension created by `gnome-shell-extension-tool --create-extension`.

## REPL

The `hello.cljs` module created by `lein-gnome/lein-template` starts a repl server:

``` clojure
(cljs.repl.gnome.server/server :js-port 6034)
```

You can connect to this using:

``` bash
lein gnome repl :js-port 6034 :clj-port 6044
```

## Gotchas

As of Gnome Shell 3.8.2 if your group name (example.com) does not have at least one period in it your extension will not be recognised.

## Resources

Gjs is not documented but the c libraries are. [This guide](http://mathematicalcoffee.blogspot.com/2012/09/developing-gnome-shell-extensions.html) explains the mapping between c names and gjs names. Bear in mind though that some c libs (eg libsoup) are not direct bindings in gjs but have been modified to be more idiomatic.

While cljs output and stacktraces are caught and sent to the repl, printing and stacktraces from js are not. One of `tail -F .xsession-errors`, `tail -F .cache/gdm/session.log` or `journalctl -f` should work.

The [Looking Glass repl](https://live.gnome.org/GnomeShell/LookingGlass) repl that ships with gnome-shell does not support copy/paste of history and is a modal window. Everything but the picker works better in the cljs repl.

Gnome libraries in gjs are dynamically loaded on demand. This makes tab completion in Looking Glass  more or less useless. Rely on the gnome docs instead. Dynamic loading also interacts strangely with cljs eg typing `js/imports` in the cljs repl will throw an exception but `js/imports.gi.Soup` will not. More on this later...

[Debugging gnome-shell](https://live.gnome.org/GnomeShell/Debugging)

[Extension faq](https://live.gnome.org/GnomeShell/Extensions/FAQ)

[Gjs examples](https://git.gnome.org/browse/gjs/tree/examples/)

[Inspect dbus intefaces](https://live.gnome.org/DFeet/)

[Cljs <-> js cheat sheet](http://himera.herokuapp.com/synonym.html)

## License

Copyright © 2012 Phil Hagelberg

Distributed under the Eclipse Public License, the same as Clojure.
