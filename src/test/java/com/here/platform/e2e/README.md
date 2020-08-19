How to run any specific test or bunch of tests:

Add a static block to BaseE2ETest and set system property env with specific value

```
static {
    System.setProperty("env", "sit");
}
```

Valid values for "env" variable: ["sit", "prod"]
P.S. there is no sense to run E2E test on DEV environment, cos of mocked some NS and CM endpoints

TBD
