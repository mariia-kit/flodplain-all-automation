package com.here.platform.mp.steps.ui;

import static com.codeborne.selenide.Selenide.open;
import static io.qameta.allure.Allure.step;

import com.codeborne.selenide.WebDriverRunner;
import com.github.javafaker.Faker;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.config.Conf;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import com.here.platform.mp.controllers.ListingsController;
import com.here.platform.mp.models.CreateInvite;
import com.here.platform.mp.models.CreatedInvite;
import com.here.platform.mp.pages.ConsumerConsentRequestPage;
import com.here.platform.mp.pages.ConsumerListingPage;
import com.here.platform.mp.pages.ConsumerSubscriptionPage;
import com.here.platform.mp.pages.ConsumerSubscriptionsListPage;
import com.here.platform.mp.pages.CreateListingPage;
import com.here.platform.mp.pages.CreateListingPage.ListingType;
import com.here.platform.mp.pages.ListingsListPage;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.restEndPoints.external.MarketplaceManageListingCall;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.springframework.web.util.UriComponentsBuilder;


@UtilityClass
public class MarketplaceFlowSteps {
    Faker faker = new Faker();
    private String subscriptionId;
    private String listingHrn;

    @Step("Open Marketplace")
    public void openMarketplace() {
        open("marketplace/provider/listings");
    }

    @Step("Login Data Consumer ")
    public void loginDataProvider(User mpUser){
        open("marketplace/provider/listings");
        HereLoginSteps.loginMPUser(mpUser);
        new ListingsListPage().isLoaded();
    };

    @Step("Login Data Consumer ")
    public void loginDataConsumer(User mpUser){
        open("marketplace/consumer/listings/");
        HereLoginSteps.loginMPUser(mpUser);
        new ListingsListPage().isLoaded();
    };

    @Step("Create and Submit listing {listingName} for container {targetContainer.id} for consumer {consumerEmail}")
    public void createAndSubmitListing(String listingName, Container targetContainer, String consumerEmail) {
        createListing(listingName, targetContainer);
        selectSubsOption();
        selectPublishOption(consumerEmail);
        publishListing();
        getCurrentListingHrn(listingName);
    }

    @Step("Create listing for daimler experimental container {listingName}")
    public void createListing(String listingName, Container targetContainer) {
        new ListingsListPage()
                .isLoaded()
                .clickCreateListing();
        new CreateListingPage()
                .selectListingType(ListingType.NEUTRAL_SERVER)
                .selectManufacturer(targetContainer.getDataProviderName())
                .selectDataContainer(targetContainer.getName())
                .fillListingName(listingName)
                .fillListingDescription(faker.company().buzzword())
                .selectAutomotiveTopic()
                .clickNext();

    }

    @Step("Select subscription option")
    public void selectSubsOption() {
    new CreateListingPage()
            .addEvaluationSubscription()
            .describeSubscription(faker.company().catchPhrase())
            .fillLinkForTermsAndCondiotion(faker.company().url())
            .clickAddSubscription()
            .clickNext();
    }

    @Step("Select publishing options")
    public void selectPublishOption(String consumerEmail) {
        new CreateListingPage()
                .selectSpecificCustomers()
                .fillDataConsumerEmail(consumerEmail);
    }

    @Step("Publish Listing")
    public void publishListing() {
        new CreateListingPage()
                .clickPublish();
    }

    @Step("Invite Consumer to Listing")
    public CreatedInvite inviteConsumerToListing(User targetDataConsumer) {
        String providerBearerToken = new ListingsListPage().fetchHereAccessToken();

        return new ListingsController()
                .withBearerToken(providerBearerToken)
                .inviteConsumerToListing(getInvite(listingHrn, targetDataConsumer));
    }

    @Step("Accept invite by data consumer")
    public void acceptInviteByConsumer(String inviteId) {
        String consumerBearerToken = new ListingsListPage().fetchHereAccessToken();
        new ListingsController()
                .withBearerToken(consumerBearerToken)
                .acceptInvite(inviteId);
    }

    @Step("Subscribe to listing Data Consumer")
    public void subscribeToListing(String listingName) {
        open("marketplace/consumer/listings/");
        new ListingsListPage().isLoaded()
                .openListingWithName(listingName);

        var consumerListingPage = new ConsumerListingPage().isLoaded();
        consumerListingPage
                .subscribeToListing()
                .seeTermsAndConditions()
                .acceptTermsAndConditions()
                .startSubscription()
                .confirmSubscriptionActivation();
    }

    @Step("Create consent request by Data Consumer for {targetContainer.id} {vin}")
    public String createConsentByConsumer(ConsentInfo consentRequest, Container targetContainer, String vin) {
        new ConsumerSubscriptionsListPage().isLoaded()
                .waitSubscriptionWithName(targetContainer.getResourceNames())
                .openSubscriptionWithName(targetContainer.getResourceNames());
        var consumerSubscriptionPage = new ConsumerSubscriptionPage().isLoaded();
        subscriptionId = getSubscriptionIdFromUrl();

        consumerSubscriptionPage
                .createConsentRequest()
                .fillConsentRequestTitle(consentRequest.getTitle())
                .fillConsentRequestDescription(consentRequest.getPurpose())
                //TODO: enable after mp deploy feature
                //.fillPolicyLinks(consentRequest.getPrivacyPolicy())
                .attachFileWithVINs(new VinsToFile(vin).csv())
                .saveConsentRequest();

        return new ConsumerConsentRequestPage().isLoaded().copyConsentRequestURLViaClipboard();
    }

    private String getSubscriptionIdFromUrl() {
        var pathSegments = UriComponentsBuilder.fromUriString(WebDriverRunner.url()).build()
                .getPathSegments();
        return pathSegments.get(pathSegments.size() - 2);
    }

    private void getCurrentListingHrn(String listingName) {
        listingHrn = new ListingsListPage().isLoaded()
                .getHrnForListingByName(listingName);
    }

    private CreateInvite getInvite(String listingHrn, User targetUser) {
        return CreateInvite.builder()
                .listingHrn(listingHrn)
                .deliveryMethod("EMAIL")
                .emails(List.of(targetUser.getEmail()))
                .callbackUrl(Conf.mp().getMarketplaceCallbackUrl())
                .build();
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getListingHrn() {
        return listingHrn;
    }

    public void removeSubscriptionAndListingForListings(String listingHrn) {
        Response listing = new ListingsController()
                .withBearerToken(Users.MP_PROVIDER.getToken())
                .getListingByHrn(listingHrn);
        String subsOptionId = listing.jsonPath().getString("subscriptionOptions[0].id");
        var mpListings = new MarketplaceManageListingCall();
        mpListings.beginCancellation(subsOptionId);
        mpListings.deleteListing(listingHrn);
    }
}
