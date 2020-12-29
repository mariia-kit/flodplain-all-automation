package com.here.platform.cm.pages;

import static com.codeborne.selenide.Selenide.$;

import io.qameta.allure.Step;


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
        public DashBoardPage openDashboardUserAvatarTab(){
            $("lui-avatar[class='tabtrigger ng-star-inserted']").click();
            return new DashBoardPage();
        }

    }

}
