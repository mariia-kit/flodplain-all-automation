package com.here.platform.e2e;

import com.github.javafaker.Faker;
import com.here.platform.cm.steps.remove.ConsentRequestRemoveExtension2;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


@Tag("e2e")
@Execution(ExecutionMode.CONCURRENT)
public class BaseE2ETest {
    @RegisterExtension
    ConsentRequestRemoveExtension2 consentRequestRemoveExtension = new ConsentRequestRemoveExtension2();

    Faker faker = new Faker();

}
