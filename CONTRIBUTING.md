# Contributing

First, join the [support server](https://discord.gg/hrfQaD7) and request the "Developer" role to get up-to-date on what is currently being added.

## Setting Up the Environment

To get started, clone the repository with `git clone https://github.com/Tisawesomeness/Minecord.git` and open in your favorite IDE
([IntelliJ](https://www.jetbrains.com/idea/) is recommended, though any proper IDE will work).
Make sure to install the [Lombok](https://projectlombok.org/) plugin to add support for its annotations.

Running can be done from your IDE. To build an executable JAR file, use `gradle shadowJar`.

To contribute your changes, make a pull request to (usually) the `dev` branch. Note that all tests must pass before your changes can be accepted, use `gradle check` to run the tests.

## Conventions

### Formatting

This project 4 spaces for indentation and the One True Brace Style (1TBS), meaning that every control statement should have braces as shown below.

```java
if (condition) {
    statement;
}
statement;
```

### Annotations

You are **highly encouraged** to use the `@lombok.NonNull` and `@javax.annotation.Nullable` on class fields and method parameters.
This ensures that code elsewhere in the codebase knows exactly when a null check is necessary and prevents unexpected NullPointerExceptions.
Note that `@NonNull` should not be used on implicitly non-null types, such as Collections, Optionals, `Validation`, `Verification`, and `Either`.

Note that Minecord uses a few annotation conventions not common to other projects:

- `@Getter/@Setter/@With` is placed directly before the access modifier, since a getter/setter can be seen as modifying the access level of a field.
- `@NonNull/@Nullables` is placed directly before the type, since they can be seen as modifying the nullability of the type.
- Other annotations go on previous lines, grouped topically.

`@Getter private final @NonNull Locale locale` is an example of proper annotation, which correctly communicates (and enforces) that `locale` is a read-only never-null constant.

Do not use Lombok `var` or `val`.

### Variable Names

Five short variable names are reserved for specific purposes:

- `e` event
- `ex` exception
- `g` guild
- `c` channel
- `u` user

Otherwise, variable names should be descriptive and do not need to be short.

### Documentation

Public methods should usually have Javadocs, which must have all assumptions, parameters, thrown exceptions, and return values documented.

### Code Quality

Do your best to follow SOLID principles and heed the advice of Effective Java (3rd edition) in the pursuit of clean, readable code. In general,

- Prefer a well-named method to a comment.
- Prefer immutability to mutability.
- Prefer private to public.
- **Use your best judgement.**

# Translation

Instructions are in the `#translation` channel in the support server.
