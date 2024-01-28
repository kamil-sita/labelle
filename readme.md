# Labelle

This is a **WORK IN PROGRESS** project, that aims to provide a framework application for labelling, tagging and categorizing images, with special support for processes that can consist of several steps.

## Modules

Labelle's architecture is modular, and consists of several modules:

1. db-changesets, db-codegen, dd-precodegen, db-spring: these modules are responsible for database schema management and generation of code to support it. JOOQ and Liquibase are used here.
2. module-fx, module-fx-spring-boot: support library that help modularize JavaFX screens
3. tflang will be a querying language used in Labelle
4. core: core functionality of labelle. In particular interesting parts are:
    1. scheduler: a module that helps define and run jobs that will be run in the future. In particular those jobs can depend on each other, and might run later
   2. repository, in-repository API: 'repository' is a collection of images and associated labels. These two APIs are core domain of Labelle
5. gui-local: a JavaFX GUI interface for Labelle

## Stack

Labelle is written in Java, uses JavaFX, Spring, JOOQ, Liquibase and it's default database is Postgres. It's build with Maven.

Later, other databases might be supported (especially SQLite).