Some custom Javadoc taglets, that I deemed worthy of 1) writing and 2) committing to GitHub :) 

### What do you have here? ###
For now, the only taglet is `include.file`, which inserts the text content of a file in the docs. Usage:

```java
/**
 * <...yadda yadda yadda...>
 * 
 * Usage is as follows:
 * <pre>
 * {@include.file resources/usage.txt}
 * </pre>
 * 
 * <...blang bling blong...>
 */
```

### How do I use it? ###

1. Download the source code (yup...);
2. Build it (as you may have noticed, I'm using Maven); and
3. Add it when you run `javadoc` (be it at [the command line](http://docs.oracle.com/javase/7/docs/technotes/tools/solaris/javadoc.html#taglet) or via [Maven plugin](https://maven.apache.org/plugins/maven-javadoc-plugin/examples/taglet-configuration.html), which is what I did...). 

I may someday publish this to Maven Central, but no promises :)

### Docs ###

* [1.0-SNAPSHOT](http://hanjos.github.io/javadoc-taglets/index.html)
