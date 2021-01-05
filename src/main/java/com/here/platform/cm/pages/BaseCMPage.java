package com.here.platform.cm.pages;

import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
import io.qameta.allure.Step;
import java.util.NoSuchElementException;


public abstract class BaseCMPage {

    public static Header header = new Header();

    public static class Header {

        @Step("Click on tab 'New' in header")
        public DashBoardPage openDashboardNewTab() {
            $("lui-tab[data-cy='new']").click();
            return new DashBoardPage();
        }

        @Step("CLick on tab 'Revoked' in header")
        public DashBoardPage openDashboardRevokedTab() {
            $("lui-tab[data-cy='revoked']").click();
            return new DashBoardPage();
        }

        @Step("CLick on tab 'Accepted' in header")
        public DashBoardPage openDashboardAcceptedTab() {
            $("lui-tab[data-cy='accepted']").click();
            return new DashBoardPage();
        }

        @Step("Click on 'Avatar' tab in header")
        public DashBoardPage openDashboardUserAvatarTab() {
            $("lui-avatar[class='tabtrigger ng-star-inserted']").click();
            return new DashBoardPage();
        }

        @Step("Verify if Headers are displayed")
        public DashBoardPage verifyHeadersDislayed() {
            $("lui-tab[data-cy='new']").shouldBe(Condition.visible);
            $("lui-tab[data-cy='revoked']").shouldBe(Condition.visible);
            $("lui-tab[data-cy='accepted']").shouldBe(Condition.visible);
            return new DashBoardPage();
        }
    }
}




