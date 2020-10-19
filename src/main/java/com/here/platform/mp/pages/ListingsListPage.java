package com.here.platform.mp.pages;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;


public class ListingsListPage extends BaseMPPage {

    private final SelenideElement createListingButton = $(dataTest("create-listing-btn"));

    @Step("Listings list page is loaded")
    @SneakyThrows
    public ListingsListPage isLoaded() {
        $(dataTest("listing-link")).waitUntil(Condition.visible, 60000);
        Thread.sleep(5000);
        if ($(byText("Got it")).isDisplayed()) {
            $(byText("Got it")).click();
        }
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
