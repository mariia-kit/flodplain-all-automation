package com.here.platform.cm.pages;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;


import com.codeborne.selenide.Condition;
import io.qameta.allure.Step;


public class LandingPage extends BaseCMPage {

    public LandingPage isLoaded() {
        $(".subtitle").waitUntil(Condition.visible, 5000);
        return this;
    }

    @Step("Press Sign-Up on landing page")
    public LandingPage signUp() {
        $(byText("Sign Up")).click();
        return this;
    }

    @Step("Press Sign-IN on landing page")
    public LandingPage signIn() {
        $(".action-button.-secondary").click();
        return this;
    }

    @Step("Verify is Sign-Up on landing page is not present")
    public LandingPage isSignUpNotPresent() {
        $(byText("Sign Up"))
                .shouldHave(Condition.hidden.because("Sign Up button should be not present!"));
        return this;
    }

    @Step("Verify is Sign-Up on landing page is present")
    public LandingPage isSignUpPresent() {
        $(byText("Sign Up"))
                .shouldHave(Condition.visible.because("Sign Up button should be present!"));
        return this;
    }
}
