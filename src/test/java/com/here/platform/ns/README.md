How to run any specific test or bunch of tests:

Add a static block to BaseNSTest and set system property env with specific value

```
static {
    System.setProperty("env", "dev");
}
```

Valid values for "env" variable: ["dev", "sit", "prod"]
