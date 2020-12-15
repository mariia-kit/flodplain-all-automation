package com.here.platform.cm.pages;

import static com.codeborne.selenide.Selenide.$;

import io.qameta.allure.Step;


public abstract class BaseCMPage {

    public static Header header = new Header();

    public static class Header {

        @Step
        public DashBoardPage openDashboardNewTab() {
            $("lui-tab[data-cy='new']").click();
            return new DashBoardPage();
        }

        @Step
        public DashBoardPage openDashboardRevokedTab() {
            $("lui-tab[data-cy='revoked']").click();
            return new DashBoardPage();
        }

        @Step
        public DashBoardPage openDashboardAcceptedTab() {
            $("lui-tab[data-cy='accepted']").click();
            return new DashBoardPage();
        }

    }

}
