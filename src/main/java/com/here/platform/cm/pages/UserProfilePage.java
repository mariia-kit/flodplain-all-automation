package com.here.platform.cm.pages;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
import io.qameta.allure.Step;


    public class UserProfilePage  extends BaseCMPage{

        public UserProfilePage isLoaded() {
            $("ng-tns-c9-0").waitUntil(Condition.visible, 5000);
            return this;
        }

        @Step("Click on the 'Sign out' button on the Avatar tab")
        public DashBoardPage clickOnSignOut() {
            $(byText("Sign out")).click();
            return new DashBoardPage();
        }

}
