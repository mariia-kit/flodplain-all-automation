package com.here.platform.dataProviders.reference.steps;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.sleep;

import com.codeborne.selenide.SelenideElement;
import com.here.platform.dataProviders.daimler.DataSubjects;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;


@UtilityClass
public class ReferenceApprovePage {

    @Step("Accept consent scopes")
    @SneakyThrows
    public void approveReferenceScopesAndSubmit(String vin) {
        $("[name=mb_general]").click();
        $("[name=vin]").sendKeys(vin);
        $("button").click();
    }

}
