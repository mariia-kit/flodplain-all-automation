package com.here.platform.mp.pages;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.WebDriverRunner;


abstract class BaseMPPage {

    public String dataTest(String targetValue) {
        return String.format("[data-test='%s']", targetValue);
    }

    public void selectGalOption(String baseDataTestElement, String targetOption) {
        var targetElement = String.join(" ", dataTest(baseDataTestElement), "gal-dropdown", ".container");

        $(targetElement).click();
        $(targetElement).find(byText(targetOption)).click();
    }

    public String fetchHereAccessToken() {
        var hereAccessCookieName = System.getProperty("env").equals("prod") ? "here_access" : "here_access_st";
        return WebDriverRunner.getWebDriver().manage().getCookieNamed(hereAccessCookieName).getValue();
    }


}
