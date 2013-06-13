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

## License

Copyright © 2012 Phil Hagelberg

Distributed under the Eclipse Public License, the same as Clojure.
