package com.here.platform.hereAccount;

import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
import io.qameta.allure.Step;


public class LoginPage {

    @Step
    public LoginPage isLoaded() {
        $("#sign-in-form").waitUntil(Condition.visible, 10000);
        return this;
    }

    @Step
    public LoginPage fillRealm(String realm) {
        $("#realm-input").val(realm);
        return this;
    }

    @Step
    public LoginPage fillUserEmail(String userEmail) {
        $("#sign-in-email").val(userEmail);
        return this;
    }

    @Step
    public LoginPage fillUserPassword(String userPassword) {
        $("#sign-in-password-encrypted").val(userPassword);
        return this;
    }

    public LoginPage clickNext() {
        $("#nextRealmBtn").click();
        return this;
    }

    public LoginPage clickRememberMeCheckbox() {
        $("[for='sign-in-remember-me']").click();
        return this;
    }

    public LoginPage clickSignIn() {
        $("#signInBtn").click();
        return this;
    }

}
