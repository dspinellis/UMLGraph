# UMLGraph - Declarative Drawing of UML Diagrams

[![Build Status](https://travis-ci.org/dspinellis/UMLGraph.svg?branch=master)](https://travis-ci.org/dspinellis/UMLGraph)

*UMLGraph* allows the declarative specification and drawing of UML diagrams.
You can browse the system's documentation
through [this link](http://www.spinellis.gr/umlgraph/doc/index.html),
or print it through [this link](http://www.spinellis.gr/umlgraph/doc/indexw.html).

In order to run *UMLGraph*, you need to have [GraphViz](https://www.graphviz.org/)
installed in your system path. On most Linux distributions, this can be easily
installed using the regular package manager.

To compile the Java doclet from the source code run *ant* on the
*build.xml* file.

If you change the source code, you can run regression tests by
executing *ant test*.

Visit the project's [home page](http://www.spinellis.gr/umlgraph) for more information.

## Compatibility

Currently, only Java 8 is supported by the Doclet.

In Java 9 the JavaDoc Doclet API changed substantially, and the doclet therefore
needs to be largely rewritten.

Sorry, this has not happened yet - volunteers for this task are welcome.

## Development versions

In order to use development versions, you can use [JitPack](https://jitpack.io/#dspinellis/UMLGraph/master-SNAPSHOT).
Note that as this is compiled on demand, you may sometimes see a "Read timed out" when the package is recompiled,
and it should be fine a few seconds later. And because the master branch can change any time, you may want to use
a versioned snapshot instead (see the Jitpack documentation for details).

**Gradle**:
```
repositories { maven { url 'https://jitpack.io' } }
configurations { umlgraph }
dependencies { umlgraph 'com.github.dspinellis:UMLGraph:master-SNAPSHOT' }
javadoc {
  doclet = 'org.umlgraph.doclet.UmlGraphDoc'
  docletpath = configurations.umlgraph.files.asType(List)
  tags("hidden:X", "opt:X", "has:X", "navhas:X", "assoc:X", "navassoc:X",
       "composed:X", "navcomposed:X", "stereotype:X", "depend:X")
}
```

**Maven**:
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
...
<dependency>
    <groupId>com.github.dspinellis</groupId>
    <artifactId>UMLGraph</artifactId>
    <version>master-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```

**Jar download**: <https://jitpack.io/com/github/dspinellis/UMLGraph/master-SNAPSHOT/UMLGraph-master-SNAPSHOT.jar>

