package com.here.platform.e2e;

import com.github.javafaker.Faker;
import com.here.platform.common.TestResultLoggerExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


@Tag("e2e")
@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(TestResultLoggerExtension.class)
public class BaseE2ETest {

    static {
        //To run on specific environment E2E tests use following "env" values: sit, prod
        //System.setProperty("env", "sit");
    }

    Faker faker = new Faker();

}
