# j2d: Java to D Source Code Translator

## Overview

j2d directly translates Java source code into the [D Programming
Language](http://dlang.org/).

It currently translates almost all Java code in a somewhat buggy fashion - it
can't quite translate the standard library yet, so is fairly useless. It's
currently aiming towards getting Hello World to translate.

## Usage

    $ java com.octarineparrot.j2d <input directory> <output directory>

## Contributing

Please use the GitHub issue tracker for bugs and feature requests. Feel free to
submit pull requests.

Obvious places to start right now are grepping for TODO and attempting to
translate hello world and the parts of the standard library it depends on.
