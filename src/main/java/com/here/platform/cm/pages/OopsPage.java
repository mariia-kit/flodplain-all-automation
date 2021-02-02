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

    @Step
    public OopsPage verifyError(String code, String title, String description) {
        $(".container-content h1").shouldHave(Condition
                .text(code).because("Error code is not as expected!"));
        $(".container-content h2").shouldHave(Condition
                .text(title).because("Error title is not as expected!"));
        $(".container-content .container-content-details").shouldHave(Condition
                .text(description).because("Error description is not as expected!"));
        return this;
    }

    @Step
    public OopsPage verifyRetryButton(boolean isPresent) {
        if (isPresent) {
            $(byText("Go back")).shouldBe(Condition.visible.because("Back button expected to be present!"));
        } else {
            $(byText("Go back"))
                    .shouldBe(Condition.not(Condition.visible.because("Back button is not expected to be present!")));
        }
        return this;
    }

}
