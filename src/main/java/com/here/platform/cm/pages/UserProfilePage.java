package com.here.platform.cm.pages;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;

import com.codeborne.selenide.Condition;
import io.qameta.allure.Step;


    public class UserProfilePage  extends BaseCMPage{

        public UserProfilePage isLoaded() {
            $("app-sidebar").
                    waitUntil(Condition.visible.because("User Profile side bar not detected!"), 5000);
            return this;
        }

        @Step("Click on the 'Sign out' button on the Avatar tab")
        public DashBoardPage clickOnSignOut() {
            $(byText("Sign out")).click();
            return new DashBoardPage();
        }

}
