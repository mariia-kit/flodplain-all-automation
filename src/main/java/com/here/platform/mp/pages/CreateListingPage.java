package com.here.platform.mp.pages;

import static com.codeborne.selenide.Selenide.$;

import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import io.qameta.allure.Step;
import lombok.AllArgsConstructor;


public class CreateListingPage extends BaseMPPage {

    @Step
    public CreateListingPage selectListingType(ListingType listingType) {
        selectGalOption("listing-form-listing-type-select", listingType.typeValue);
        return this;
    }

    @Step
    public CreateListingPage selectManufacturer(Providers provider) {
        selectGalOption("listing-form-listing-ns-provider-select", provider.getName());
        return this;
    }

    @Step
    public CreateListingPage selectDataContainer(ProviderResource containerResource) {
        selectGalOption("listing-form-listing-ns-container", containerResource.getName());

        return this;
    }

    @Step
    public CreateListingPage fillListingName(String listingName) {
        $(dataTest("listing-form-listing-title")).val(listingName);

        return this;
    }

    @Step
    public CreateListingPage fillListingDescription(String listingDescription) {
        $(dataTest("listing-form-listing-description")).val(listingDescription);

        return this;
    }

    @Step
    public CreateListingPage selectAutomotiveTopic() {
        selectGalOption("listing-form-listing-topic", "Automotive");
        return this;
    }

    @Step
    public CreateListingPage clickNext() {
        $("[name='next']").click();
        return this;
    }

    public CreateListingPage addEvaluationSubscription() {
        $(dataTest("add-evaluation-subscription")).click();
        return this;
    }

    public CreateListingPage describeSubscription(String subscriptionDescription) {
        $("#description").val(subscriptionDescription);
        return this;
    }

    public CreateListingPage fillLinkForTermsAndCondiotion(String url) {
        $("#link").val(url);
        return this;
    }

    public CreateListingPage clickAddSubscription() {
        $(dataTest("submit-subscription-adding")).click();
        return this;
    }

    public CreateListingPage selectSpecificCustomers() {
        $(dataTest("listing-specific-user-lui-checkbox")).click();
        return this;
    }

    public CreateListingPage fillDataConsumerEmail(String email) {
        $(dataTest("new-listing-email-input")).val(email);
        return this;
    }

    public CreateListingPage clickPublish() {
        $(dataTest("listing-wizard-publish-btn")).click();
        return null;
    }

    @AllArgsConstructor
    public enum ListingType {

        DATA_CATALOG_BASED("Data catalog-based"), NEUTRAL_SERVER("Neutral server"), EXTERNAL_API("External API");

        String typeValue;
    }

}
