package com.here.platform.mp.pages;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.apache.commons.lang3.StringUtils;


public class ListingsListPage extends BaseMPPage {

    private final SelenideElement createListingButton = $(dataTest("create-listing-btn"));

    @Step("Listings list page is loaded")
    public ListingsListPage isLoaded() {
        $(dataTest("listing-link")).waitUntil(Condition.visible, 20000);
        return this;
    }

    @Step
    public ListingsListPage clickCreateListing() {
        createListingButton.click();
        return this;
    }

    public ListingsListPage openListingWithName(String listingName) {
        $(byText(listingName)).click();
        return this;
    }

    public String getHrnForListingByName(String listingName) {
        var hrefValue = $(byText(listingName))
                .closest("a")
                .shouldBe(Condition.visible)
                .getAttribute("href");

        return StringUtils.substringBetween(hrefValue, "listings/", "/details");
    }

}
