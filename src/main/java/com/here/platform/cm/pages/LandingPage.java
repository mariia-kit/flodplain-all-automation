package com.here.platform.cm.pages;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
import io.qameta.allure.Step;


public class LandingPage extends BaseCMPage {

    public LandingPage isLoaded() {
        $(".subtitle")
                .waitUntil(Condition.visible
                        .because("Landing page should be visible!"),5000)
                .shouldHave(Condition.text("Get benefits in exchange for your vehicle's data")
                        .because("Landing page should be present!"));
        return this;
    }

    @Step("Click Sign Up button on landing page")
    public LandingPage clickOnSignUp() {
        $(byText("Sign Up")).click();
        return this;
    }

    @Step("Click Sign In button on landing page")
    public LandingPage clickSignIn() {
        $(".action-button.-secondary").click();
        return this;
    }

}