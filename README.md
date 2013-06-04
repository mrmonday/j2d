# j2d: Java to D Source Code Translator

## Overview

j2d directly translates Java source code into the [D Programming
Language](http://dlang.org/).

It currently translates almost all Java 7 code in a somewhat buggy fashion - it
can't quite translate the standard library yet, so is fairly useless. It's
currently aiming towards getting Hello World to translate.

## Usage

j2d requires the following to build:
 * Java 7
 * Eclipse (Juno) with the following libraries:
    - org.eclipse.jdt.core
    - org.eclipse.equinox.common
    - org.eclipse.core.resources
    - org.eclipse.core.jobs
    - org.eclipse.core.runtime
    - org.eclipse.osgi
    - org.eclipse.core.contenttype
    - org.eclipse.equinox.preferences
   You can probably find these in Eclipses plugin directory.

You can use the following to run:
```
$ java com.octarineparrot.j2d <input directory> <output directory>
```

## Contributing

Please use the GitHub issue tracker for bugs and feature requests. Feel free to
submit pull requests.

Obvious places to start right now are grepping for TODO and attempting to
translate hello world and the parts of the standard library it depends on.
