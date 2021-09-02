How to run any specific test, or a bunch of tests:

Add a static block to BaseCMTest and set system property env with specific value

```
static {
    System.setProperty("env", "dev");
}
```

Valid values for "env" variable: ["dev", "sit", "prod"]

###Swagger documentations for environments:
- DEV - https://dev.consent.api.platform.in.here.com/consent-service/swagger-ui.html
- SIT - https://consent.platform.sit.here.com/consent-service/swagger-ui.html
- PROD - https://consent.api.platform.here.com/consent-service/swagger-ui.html

###Structure of the CM tests (packages):
- cm/bmw - tests for BMW data provider implementation
- consentRequests - tests for Consent Request controller
  
P.S. Based on controllers in the Swagger documentation
