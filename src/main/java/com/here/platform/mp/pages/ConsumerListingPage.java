package com.here.platform.mp.pages;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;


public class ConsumerListingPage extends BaseMPPage {

    private final SelenideElement listingTitle = $(dataTest("listing-details-listing-title"));

    public ConsumerListingPage isLoaded() {
        listingTitle.waitUntil(Condition.visible, 20000);
        return this;
    }

    public ConsumerListingPage listingNameShouldBe(String listingName) {
        listingTitle.shouldHave(Condition.text(listingName));
        return this;
    }

    public ConsumerListingPage subscribeToListing() {
        $(dataTest("olp-listing-detail-action-btn")).click();
        return this;
    }

    public ConsumerListingPage seeTermsAndConditions() {
        $(dataTest("accept-terms-component-link")).click();
        switchTo().window(0);
        return this;
    }

    public ConsumerListingPage acceptTermsAndConditions() {
        $(dataTest("accept-terms-component-checkbox")).click();
        return this;
    }

    public ConsumerListingPage startSubscription() {
        $(dataTest("listing-subscribe-start-subscription")).click();
        return this;
    }

    public ConsumerListingPage confirmSubscriptionActivation() {
        $(dataTest("listing-wizard-confirm-subscription-cancel")).click();
        return this;
    }

}
