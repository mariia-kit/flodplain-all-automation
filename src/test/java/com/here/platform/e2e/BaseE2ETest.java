package com.here.platform.e2e;

import com.github.javafaker.Faker;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import org.junit.jupiter.api.Tag;


@Tag("e2e")
public class BaseE2ETest {

    Faker faker = new Faker();

}
