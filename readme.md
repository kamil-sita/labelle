# Labelle

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=kamil-sita_labelle&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=kamil-sita_labelle)[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=kamil-sita_labelle&metric=coverage)](https://sonarcloud.io/summary/new_code?id=kamil-sita_labelle) [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=kamil-sita_labelle&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=kamil-sita_labelle)

This is a **WORK IN PROGRESS** project, that aims to provide a framework application for labelling, tagging and categorizing images, with special support for processes that can consist of several steps.

## Modules

Labelle's architecture is modular (a modular monolith), and consists of several modules, that might be split to different
projects (although, ultimately, it's unlikely):

1. db-changesets, db-codegen, dd-precodegen, db-spring: these modules are responsible for database schema management and generation of code to support it. JOOQ and Liquibase are used here.
2. module-fx, module-fx-spring-boot: support modules that help modularize JavaFX screens, provide additional utilities
3. tflang will be a querying language used in Labelle
4. core-common: common classes used in 'common' modules
5. magic-scheduler: a module that helps define and run jobs that will be run in the future. In particular those jobs can depend on each other, and might run later
6. core: core functionality of labelle. In particular interesting parts is repository, in-repository API: 'repository' is a collection of images and associated labels. These two APIs are core domain of Labelle
7. gui-local: a JavaFX GUI interface for Labelle

## Stack

Labelle is written in Java, uses JavaFX, Spring, JOOQ, Liquibase and it's default database is Postgres. It's build with Maven. Tests use JUnit and TestFX.

Later, other databases might be supported (especially SQLite).

## Tests

Currently, Labelle has below-average test coverage. There are some integration tests, and pseudo E2E using TestFX.