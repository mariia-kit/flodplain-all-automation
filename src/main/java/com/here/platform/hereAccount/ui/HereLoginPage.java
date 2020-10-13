package com.here.platform.hereAccount.ui;

import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
import io.qameta.allure.Step;


public class HereLoginPage {

    @Step
    public HereLoginPage isLoaded() {
        $("#sign-in-form").waitUntil(Condition.visible, 10000);
        return this;
    }

    @Step
    public HereLoginPage fillRealm(String realm) {
        $("#realm-input").val(realm);
        return this;
    }

    @Step
    public HereLoginPage fillUserEmail(String userEmail) {
        $("#sign-in-email").val(userEmail);
        return this;
    }

    @Step
    public HereLoginPage fillUserPassword(String userPassword) {
        $("#sign-in-password-encrypted").val(userPassword);
        return this;
    }

    public HereLoginPage clickNextEmail() {
        $("#nextEmailBtn").click();
        return this;
    }

    public HereLoginPage clickNextRealm() {
        $("#nextRealmBtn").click();
        return this;
    }

    public HereLoginPage clickRememberMeCheckbox() {
        $("[for='sign-in-remember-me']").click();
        return this;
    }

    public HereLoginPage clickSignIn() {
        $("#signInBtn").click();
        return this;
    }

}
