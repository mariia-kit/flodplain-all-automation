# Neutral Server All Automation project

**It is a repository with automated tests for Neutral Server project.**

### Tech stack:
- Gradle 6.7
- Java 11
- JUnit 5
- AssertJ (Assert library)
- OpenAPI codegen (Code generator)
- Rest-assured 4 (API client wrapper)
- Selenide 5 (Selenium wrapper)
- Allure 2 (Test reports)
- Lombok 1.18 (Annotation processor)
- JavaFaker (Data generator)

#### Glossary:
- Marketplace - customer facing application that provides possibilities to create Listings, Subscriptions and Consent request creation.
- Neutral Server - proxy between Data Consumer and Data Provider's resources
- Consent Management - client facing application that provides possibilities to store Data Subjects'(DS) consent accesses
- Hyper Ledger - block-chain infrastructure that provides safe possibility to store DS's consents
- Automotive Applications - in context of Neutral Server project provides possibility to store politics(authorization) for listings and subscriptions

#### Acronyms:  
- MP - Marketplace
- NS - Neutral Server
- CM - Consent Management
- HL - HyperLedger
- HA - HERE Account
- AA - Automotive Applications


#### Neutral Server project consists from (components):
- Neutral Server - Access BE, Provider BE;
- Consent Management - CM service, Token-Refresher service, UI;
- Chaincode - mediator API between CM and HL  
- Hyper Ledger - Ordering service (assembles transactions into blocks), peers(accepts blocks), ledger, Explorer UI(shows blockchain data) 

#### In this repository covered following testing levels:
- Component based tests for NS and CM
- Integration tests
- E2E tests for full flow MP & NS & CM

#### Covered integrations between systems:
- MP & NS
- AA & NS
- MP & CM


### Pre-condition to run tests from Intellij IDEA
To start working with the project, first-of-all you have to generate API models based on the environments

Run any one task in the Gradle tool tab:
- build -> generateSwaggerCode
- other -> compileTestJava

OR

Run in a Terminal tool tab following command:

```commandline
gradle generateSwaggerCode
```
or
```commandline
gradle compileTestJava
```

### To run tests - use gradle tasks
Gradle tasks located in files:
- gradle/testTasksCM.gradle - to run tests for CM service and UI
- gradle/testTasksE2E.gradle - to run UI E2E tests for MP & NS & CM
- gradle/testTasksNS.gradle - to run tests for NS Access and NS Provider services
