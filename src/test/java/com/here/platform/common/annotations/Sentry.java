package com.here.platform.common.annotations;

import io.qameta.allure.Feature;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;


@Target(ElementType.METHOD)
@Feature("Sentry")
@Tag("sentry")
public @interface Sentry {

}
