Some custom Javadoc taglets, that I deemed worthy of 1) writing and 2) committing to GitHub :) 

### What do you have here? ###
For now, the only taglet is `include.file`, which inserts the text content of a file in the docs. Usage:

```java
/**
 * <...>
 * 
 * Usage is as follows:
 * <pre>
 * {@include.file resources/usage.txt}
 * </pre>
 * 
 * <...>
 */
```
