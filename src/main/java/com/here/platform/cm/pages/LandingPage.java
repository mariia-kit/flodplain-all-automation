package com.here.platform.cm.pages;

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
        $(".action-button").click();
        return this;
    }

    @Step("Press Sign-IN on landing page")
    public LandingPage signIn() {
        $(".action-button.-secondary").click();
        return this;
    }
}
