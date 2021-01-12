package com.here.platform.cm.pages;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
import io.qameta.allure.Step;


public class ActualOffersPage extends BaseCMPage {

    public ActualOffersPage isLoaded() {
        $(".container.offers").waitUntil(Condition.visible, 5000);
        return this;
    }

    @Step("Click Add new vehicle button")
    public ActualOffersPage clickAddNewVin() {
        $(byText("Add new vehicle")).click();
        return this;
    }

    @Step("Click See other offers link")
    public ActualOffersPage clickAllOffers() {
        $(byText("See other offers")).click();
        return this;
    }
}
