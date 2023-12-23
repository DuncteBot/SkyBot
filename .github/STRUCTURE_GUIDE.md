# How does SkyBot stay consistent in its code style and structure?

On this page we will try to concentrate all structure and code style guides that we ourself
use in SkyBot to ensure consistency and readability.

## Indentation and Brackets

In SkyBot we use a specific brackets placement that is common for Java development.
We put each opening `{` curly bracket after the closing parenthese `)`:

We also surround if statements, try/catch/finally and loops with whitespace
```java
public void someMethod() {
    this.works.well();
    
    for (Each element : from()) {
        System.out.println(element);
    }
    
    if (this.hasApples()) {
        System.out.println("We have apples");
    } else {
        System.out.println("We don't have apples");
    }
}
```
> Note: This rule goes for every curly bracket (open `{`/close `}`) and every scope usage such as try/catch/finally, loops, methods, lambda expressions, class scopes...

With try-catch blocks we like to have the catch block on a new line:
```java
public void someMethod() {
    this.works.well();
    
    try {
        String element = this.mightTrhow();
        System.out.println(element);
    }
    catch (Exception e) {
        e.printStackTrace();
    }
}
```

### Indentation

We only use indentation of **4 spaces** consistently throughout SkyBot.
<br>If a Pull Request does not use this indentation we will not accept it.

## Class Structure

In this section we guide you through a logically ordered and structure class under SkyBots's point of view.

### Command constructor
To maintain consistency between the constructors of the commands the following order has been given to the properties
1. shouldLoadMembers
2. requiresArgs
3. requiredArgCount
4. displayAliasesInHelp
5. category
6. name
7. aliases
8. help
9. usage
10. extraInfo
11. userPermissions
12. botPermissions
13. flags
14. cooldown
15. cooldownScope
16. cooldownKey
17. overridesCooldown

### Access Modifiers

Access Modifiers are the keywords such as `public`, `protected` and `private`. They restrict other members from
accessing these fields, methods or classes from locations throughout the library.

When trying to order your fields, methods or nested classes we recommend using this logical order:
1. Public Members
2. Protected Members
3. Private Members
4. Package Private Members (no access modifier)

In addition it is recommended to always put `static` fields and methods (not [[nested classes|Structure-Guide#nested-classes]]) first in your class.
<br>Fields marked with the `final` keyword should come first and should be separated from other fields.
<br>For better structure it is suggested to group fields by their declared types.

### Methods

Methods are always defined after fields and the constructor of your class.

### Nested Classes

Nested classes no matter if `static` or member should always be placed at the very bottom of your class.
<br>These include enums and other class types. It is also recommended following the access modifier (see above sections) order here again.

### Imports and Copyright

Every class in SkyBot has a **Copyright Header** ([see this example](https://github.com/DuncteBot/SkyBot/blob/master/src/main/java/ml/duncte123/skybot/SkyBot.java)).
<br>Imports use wildcard `*` when they import 5+ classes from the same package.

## JavaDoc

We put JavaDoc on the following targets:
- Public Methods that can be used by multiple classes
- Any method that you think might be confusing to other people

### Paragraphs

SkyBot has a specific JavaDoc structure that is very basic.
We use a style in which we encapsulate each important part of the doc in a "block" for itself.
<br>Each `<br>` tag is placed in-front of the new line: 
```java
/**
 * This is my first line
 * <br>And this is my second line
 */
```
> Note: `<br>` tags are not to be closed

A new paragraph starts with `<p>` (not `<br><br>`!).
The `<p>` tag is supposed to be placed either between the previous and following paragraph or like the `<br>` tag directly
in-front of the first line of that following paragraph:
```java
/**
 * Either do this
 * 
 * <p>Or do this
 * <p>
 * You can decide.
 */
```
> Note: Do not close a paragraph tag! It is unnecessary and redundant.

### Escaping

When you want to use characters that are not available in JavaDoc source most people tend to go with escaped characters such as `&tm;`.
<br>In SkyBot we usually stick -if possible- to `{@literal ™}` for readability sake. When using this JavaDoc tag you must remember
that it does not allow other nested tags since it will replace them with their literal characters instead!
<br>Another important tag you can use to achieve something that `<code>true</code>` does you can use `{@code true}`.
If you however want to also have nested links or other JavaDoc tags in your code snippet you can fallback to using the `<code>` tag:
```java
/**
 * SkyBot {@literal >} All other bots
 * <br>Because we can do `{@code channel.sendMessage("hey").queue()}` AND `{@code channel.sendMessage("hey").complete()}`!
 * 
 * <p>Or even do <code>MessageUtils.{@link me.duncte123.botcommons.messaging.MessageUtils#sendMsg sendMsg(channel, "Hey")}</code>!!
 */
```
> Note: If you are using a tag like this, you **have** to close it!

### Linking

Always use the fully qualified name when you are linking to something through JavaDoc!
<br>Bad: `{@link RestAction}`
<br>Good: `{@link net.dv8tion.jda.api.requests.RestAction RestAction}`
> Note: We also highly recommend setting an alias name as you can see in the 2nd example snippet.

When you link to an external resource (such as the official api docs) you can use the `<a>` tag to create
a hyperlink.
<br>It is recommended to use the `target="_blank"` tag.
<br>Example: `<a href="https://travis-ci.org/DuncteBot/SkyBot" target="_blank">View SkyBot CI</a>`

**Hint**: Sometimes it helps to link other methods in the description and then also including an `@see #otherMethod` at the very bottom.

### Example Template
```java
    /**
     * This description should inform the user about the basic function of the method (or class)
     * that is being documented.
     * <br>A line break should be placed at the beginning of the following line.
     *
     * <p>This description is optional and should contain additional / notable information about
     * this method (or class)
     *
     * <p>All additional description paragraphs should start with the paragraph tag
     * at the beginning of the new paragraph and should be separated from the previous
     * paragraph by (at least) one line.
     *
     * @param  var0
     *         The Description should be at the same level as the parameter name
     * @param  var1
     *         Multiple parameters are to be documented in one "block"
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.Role Role}
     *         <br>The response type of the RestAction can be described further here.
     *
     * @throws javax.security.auth.login.LoginException
     *         The same goes for descriptions of throwables
     * @throws net.dv8tion.jda.api.exceptions.RateLimitedException
     *         Multiple throwables are to be documented in one "block"
     *
     * @see    Void
     * @see    me.duncte123.skybot.SkyBot
     *
     * @since  3.0
     *
     * @serialData
     *         If a tag is not specified here it should be at the bottom of the documentation.
     *         <br>If the tag name is too long to follow the proper indentation formatting
     *         it should start the block in the next line with the correct indentation.
     */
```
> Note: We align each block with whitespace as you can see how `@param` is separated with 2 space characters from the actual parameter name
