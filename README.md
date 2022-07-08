[![SOFT](https://github.com/BananazTechnology/listing-bot-producer/actions/workflows/SOFT.yml/badge.svg?branch=develop)](https://github.com/BananazTechnology/listing-bot-producer/actions/workflows/SOFT.yml) [![RELEASE](https://github.com/BananazTechnology/listing-bot-producer/actions/workflows/RELEASE.yml/badge.svg)](https://github.com/BananazTechnology/listing-bot-producer/actions/workflows/RELEASE.yml)

# Listing Bot Producer
* Description: A @spring-projects framework project which loads items into @mysql
* Version: (Check main for release or develop for dev)
* Creator: Aaron Renner
* THIS REPO IS A LISTING EVENT PRODUCER

### Table of Contents
* [Introduction](#introduction)
* Setup *"How to"*
  * [Run Spring-Boot](#running-the-project)
* Help
  * [Setup Libraries and Examples](#libraries)
  
## Introduction

This Java application is built on the Spring-Boot framework! This producer bot utilizes external APIs like those from @ProjectOpenSea and @LooksRare to obtain market event information and creates generic event objects in a MySQL database. Unlike our original listing bot [discord-nft-listing-bot](https://github.com/Aman7123/discord-nft-listing-bot) which was configured on a per-instance basis with individual tokens, this revised setup requires only configuration to the organization DB which contains all configurations and are load balanced to these producer bots. This bot includes a small internal API for obtaining the state configuration of each contract.

THIS REPO IS A LISTING EVENT PRODUCER

## Setup
### Properties

Through environment variables or by modifying the application.properties the following values need to be set:

``` yaml
# API Config
server:
  # Listening port
  port: 8080
  # Open address
  address: 0.0.0.0
  servlet:
    # The base path after http://host:port
    context-path: /api
# MySQL Config
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://host:port/DB-Name?createDatabaseIfNotExist=true
    username: username
    password: password
```

### Running the Project

Executing the project can be done in two ways, the first is by initializing using Maven and the second produces a traditional Jar file. Before attempting to run the program some setup must be done inside of the [src/main/resources/application.properties](src/main/resources/application.yml), you can follow the guides above for setting up the properties.

### Accessing internal debug API
Assuming the `server` properties are configured as shown above.

* `https://localhost:8080/api/contracts` - This shows the current internal configuration of all contracts.
* `https://localhost:8080/api/actuator` - Different endpoints to view the status of Spring.

In a production environment, these debug ports and routes should be hardened, this [Production Monitoring](https://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/html/production-ready-monitoring.html) guide describes the settings for changing the actuator port. The `/contracts` endpoint is read-only and contains no sensitive data.

### Build with Maven

If you have Maven installed on your machine you can navigate to the root project directory with this README file and execute the following. Remember to follow the above Database setup procedures first.
```sh
mvn -B -DskipTests clean package
```
You can also use the built-in Maven wrapper and execute the project by following this command.
```sh
./mvnw -B -DskipTests clean package
```
### Setting up in IDE

Download Lombok to your IDE or VS Code Extension!
Use the IDE "Run Configuration" to set up the `-Dspring.datasource.username` (eclipse example) in the Environment Properties.

### Creating a Docker Image

To build a container that can execute the application from a safe location you can use my supplied [Dockerfile](Dockerfile) to do so. You should follow the guides first to better understand some of these arguments.

```Dockerfile
CMD [ "java", \
        "-jar", \
        "Bot.jar"]
```

## Libraries

### Jump To
* [Required Dependencies](#spring-boot-required)
* [Lombok](#lombok)
* [JSON-Smart](#json-smart)

### Spring-Boot Required
<details><summary>Lombok</summary>
* [Lombok - Automated Class Method Generation](https://projectlombok.org/features/all)
```pom
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```
</details>
<details><summary>JSON-Smart</summary>
* [JSON Parser JAVADOC](https://javadoc.io/doc/net.minidev/json-smart/latest/index.html)
```pom
<dependency>
    <groupId>net.minidev</groupId>
    <artifactId>json-smart</artifactId>
</dependency>
```
</details>
