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
Press Alt+F2 r ENTER to reload.
```

Note that as of Gnome Shell 3.8.2 if your group name (example.com) does not have at least one period in it your extension will not be recognised.

The `hello.cljs` file is a direct port of the example extension created by `gnome-shell-extension-tool --create-extension`.

Coming soon: a repl.

## License

Copyright © 2012 Phil Hagelberg

Distributed under the Eclipse Public License, the same as Clojure.
