package com.here.platform.cm.pages;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
import io.qameta.allure.Step;


public class OopsPage extends BaseCMPage {

    public OopsPage isLoaded() {
        $(".error-img").waitUntil(Condition.visible.because("Error page should be present!"), 5000);
        return this;
    }

    @Step("Click GoBack button on error page")
    public OopsPage clickOnSignUp() {
        $(byText("Go back")).click();
        return this;
    }

}
