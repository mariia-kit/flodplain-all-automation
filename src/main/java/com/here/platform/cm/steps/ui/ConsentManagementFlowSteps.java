package com.here.platform.cm.steps.ui;

import static com.codeborne.selenide.Selenide.open;

import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;


@UtilityClass
public class ConsentManagementFlowSteps {

    @Step("Open consent request link {consentUrl}")
    public void openConsentLink(String consentUrl) {
        open(consentUrl);
    }

}
