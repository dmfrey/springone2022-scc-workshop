# SpringONE 2022 - Spring Cloud Contracts Workshop

* Dan Frey - Solutions Architect, VMware Tanzu Labs
* Ellie Bahadori


## About the Sample Apps

There are 2 Spring Boot applications in this mono-repo:

1. Person App
2. Order App

Each represents a fictional domain for a generic shopping application. Both applications can standalone and exposes a REST API.
At this time, the Order App has no other interactions. The Person App, however, will always attempt to call the Orders App.

Each `Person` in the Person App has a `customerId`, as does each `Order` in the Order App. It is through this relationship that the Person App will attempt to join Order data to the `Person` domain object.

### `Diagram` the interactions between the apps

### Order App API

| HTTP Method | API                  |
|:----------|:---------------------|
| GET         | /orders/{customerId} |

**NOTE:** There is a Swagger interface available for the API

### Person App API

| HTTP Method | API                 |
| ----------- |---------------------|
| GET | /persons |
| GET | /persons/{personId} |

**NOTE:** There is a Swagger interface available for the API

Each of these APIs will attempt to call the Order App to fill in any person's order history.

## Run the tests

The applications are Spring Boot applications and built using Maven and supplies the self-contained Maven Wrapper.

All the tests across both submodules can be run as follows:
```bash
$ ./mvnw clean test
```

Since this is a mono-repo and it contains 2 submodules, each can be tested independently of one another.

*Order App Tests*
```bash
$ ./mvnw -pl :order-app clean package
```

*Person App Tests*
```bash
$ ./mvnw -pl :person-app clean package
```

### How we test our application integrations today

* No real integration between apps today
* Heavily rely on mocks, assumptions, and, hopefully, up-to-date API documentation

### Pitfalls of the current testing method


## Starting the Sample Apps

### `curl` commands for the Person App

### `curl` commands for the Order App


## Introducing Spring Cloud Contracts
