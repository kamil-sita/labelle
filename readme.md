# Labelle

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=kamil-sita_labelle&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=kamil-sita_labelle) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=kamil-sita_labelle&metric=coverage)](https://sonarcloud.io/summary/new_code?id=kamil-sita_labelle) [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=kamil-sita_labelle&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=kamil-sita_labelle)

This is a **WORK IN PROGRESS** project, that aims to provide a framework application for labelling, tagging and categorizing images, with special support for processes that can consist of several steps.

## Modules

Labelle's architecture is modular (a modular monolith), and consists of several modules, that might be split to different
projects (although, ultimately, it's unlikely):

1. db-changesets, db-codegen, db-pre-codegen, db-spring: these modules are responsible for database schema management and generation of code to support it. JOOQ and Liquibase are used here:
   1. db-changesets: contains Liquibase changesets
   2. db-pre-codegen: updates build database using Liquibase to prepare for JOOQ codegen
   3. db-codegen: generates JOOQ code based on build database
   4. db-spring: allows updating actual database without starting the app
2. module-fx, module-fx-spring-boot: support modules that help modularize JavaFX screens, provide additional utilities
3. TFLang is a limited in scope, simplified querying and modification language used in Labelle
4. core-common: common classes used in 'common' modules
5. magic-scheduler: a module that helps define and run jobs that will be run in the future. In particular those jobs can depend on each other, and might run later
6. core: core functionality of labelle. In particular interesting parts is repository, in-repository API: 'repository' is a collection of images and associated labels. These two APIs are core domain of Labelle
7. gui-local: a JavaFX GUI interface for Labelle

## Stack

Labelle is written in Java, uses JavaFX, Spring, JOOQ, Liquibase and it's default database is Postgres. It's build with Maven. Tests use JUnit and TestFX.

Later, other databases might be supported (especially SQLite).

## Tests

Labelle has some integration tests, and pseudo E2E using TestFX. Coverage report and analysis is published on SonarCloud as below:

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=kamil-sita_labelle&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=kamil-sita_labelle) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=kamil-sita_labelle&metric=coverage)](https://sonarcloud.io/summary/new_code?id=kamil-sita_labelle) [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=kamil-sita_labelle&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=kamil-sita_labelle)

## TFLang

TFLang stands for ``The Filtering LANGuage``.

TFLang is a limited in scope, simplified querying and modification language used in Labelle.

### TFLang basic search context

For example, let's say that you are in a context that exposes one string variable called ``tag``. To find all entries with a value ``foobar``, you would write:

```sql
tag = "foobar"
```

To find all entries with a value that contains ``foo``, you would write:

```sql
tag LIKE ".*foo.*"
```

To find all entries with a value that contains ``foo`` and ``bar``, you would write:

```sql
tag LIKE ".*foo.*" AND tag LIKE ".*bar.*"
```

To find all entries with a value that contains ``foo`` or ``bar``, you would write:

```sql
tag LIKE ".*foo.*" OR tag LIKE ".*bar.*"
```

To find all entries with a value that contains ``foo`` but not ``bar``, you would write:

```sql
tag LIKE ".*foo.*" AND NOT (tag LIKE ".*bar.*)"
```

Matching tuples is as creative as TFLang can currently get, let's say that you are searching a ``category``/``tag`` tuple, and you're feeling very specific today:

```sql
(category, tag) IN (("foo", "bar"), ("baz", "qux"))
```

### TFLang sub-entity search context
Let's say, today you're really adamant on finding out whether this repository has two tags, ``foo`` and ``bar``. This is a bit harder, as we're not filtering by this entity itself, but within some bigger context - let's call it ``tags``:

```sql
IN tags EXISTS (tag = "foo") and IN tags EXISTS (tag = "bar")
```

### TFLang modification context

You're looking through your photos, exploring tags, and you see that you've made a mistake. You've tagged a photo with ``foo`` instead of ``bar``. You want to fix it:

```sql
IF category="category" AND tag = "foo" THEN REMOVE, ADD ("category", "bar")
```

But you can do it in a better way:

```sql
IF category="category" AND tag = "foo" THEN REPLACE WITH ("category", "bar")
```

But wait, the category can be also anything, not just ``category``. Let's fix that:

```sql
IF tag = "foo" THEN REPLACE WITH (MATCHED, "bar")
```

Sometimes you have to admit, the logic gets too hard. You can reference functions as well:

```sql
IF tag = "foo" THEN REMOVE USING foobar;
IF tag = "bar" THEN ADD USING foobar;
IF tag = "baz" THEN REPLACE USING foobar;
```

### TFLang sub-entity modification context

You can also modify a sub-entity:
```sql
IF IN tags EXISTS (tag = "foo") THEN IN tags DO (IF tag="foo" THEN REMOVE "foo")
```

Which might be more useful if you're trying to do something based on multiple tags:
```sql
IF IN tags EXISTS (tag = "foo") AND IN tags EXISTS (tag = "bar") THEN IN tags DO (ADD "baz")
```

Since ``MATCHED`` keyword only refers to the current level, using it in sub-entity context will not work.
