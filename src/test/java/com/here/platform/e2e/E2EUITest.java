package com.here.platform.e2e;

import static com.codeborne.selenide.Selenide.open;
import static com.here.platform.ns.dto.Users.MP_CONSUMER;
import static com.here.platform.ns.dto.Users.MP_PROVIDER;
import static com.here.platform.ns.utils.NS_Config.MARKETPLACE_CALLBACK;
import static com.here.platform.ns.utils.NS_Config.URL_EXTERNAL_MARKETPLACE_UI;
import static io.qameta.allure.Allure.step;

import com.codeborne.selenide.Configuration;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.common.VinsToFile;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.hereAccount.ui.LoginPage;
import com.here.platform.hereAccount.ui.LoginSteps;
import com.here.platform.mp.controllers.ListingsController;
import com.here.platform.mp.models.CreateInvite;
import com.here.platform.mp.pages.ConsumerConsentRequestPage;
import com.here.platform.mp.pages.ConsumerListingPage;
import com.here.platform.mp.pages.ConsumerSubscriptionPage;
import com.here.platform.mp.pages.CreateListingPage;
import com.here.platform.mp.pages.CreateListingPage.ListingType;
import com.here.platform.mp.pages.ListingsListPage;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.User;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
public class E2EUITest extends BaseE2ETest {

    static {
        System.setProperty("env", "prod");
    }

    private final ListingsListPage listingsPage = new ListingsListPage();
    private final DataSubjects targetDataSubject = DataSubjects.getNext();
    private final User
            targetDataProvider = MP_PROVIDER.getUser(),
            targetDataConsumer = MP_CONSUMER.getUser();

    @BeforeEach
    void beforeEach() {
        var userAccountController = new UserAccountController();
        userAccountController.deleteVINForUser(targetDataSubject.vin, targetDataSubject.generateBearerToken())
                .prettyPrint();
        userAccountController.deleteConsumerForUser(targetDataConsumer.getRealm(), targetDataSubject.getBearerToken())
                .prettyPrint();
        Configuration.baseUrl = URL_EXTERNAL_MARKETPLACE_UI.toString().replace("marketplace", "");
        Configuration.browserSize = "1366x1000";
        open("marketplace");
    }

    @Test
    @DisplayName("Simple happy path E2E UI level")
    void simpleHappyPathTest() {
        var listingName = "[E2E test] " + faker.company().buzzword();

        step("Login Data Provider", () -> LoginSteps.loginMPUser(targetDataProvider));

        step("Create listing for daimler experimental container", () -> {
            var testManufacturer = Providers.DAIMLER_EXPERIMENTAL;

            listingsPage
                    .isLoaded()
                    .clickCreateListing();
            var createListingPage = new CreateListingPage()
                    .selectListingType(ListingType.NEUTRAL_SERVER)
                    .selectManufacturer(testManufacturer)
                    .selectDataContainer(testManufacturer.getProvider().getResources().get(1))
                    .fillListingName(listingName)
                    .fillListingDescription(faker.company().buzzword())
                    .selectAutomotiveTopic()
                    .clickNext();

            step("Select subscription option", () -> {
                createListingPage
                        .addEvaluationSubscription()
                        .describeSubscription(faker.company().catchPhrase())
                        .fillLinkForTermsAndCondiotion(faker.company().url())
                        .clickAddSubscription()
                        .clickNext();
            });

            step("Select publishing options", () -> {
                createListingPage
                        .selectSpecificCustomers()
                        .fillDataConsumerEmail(targetDataConsumer.getEmail())
                        .clickPublish();
            });
        });

        String listingHrn = listingsPage.isLoaded()
                .getHrnForListingByName(listingName);

        String providerBearerToken = listingsPage.fetchHereAccessToken();

        var listingController = new ListingsController();
        var createdInvite = listingController
                .withToken(providerBearerToken)
                .inviteConsumerToListing(getInvite(listingHrn, targetDataConsumer));

        step("Logout Data Provider", () -> {
            open("logout");
            new LoginPage().isLoaded();
        });

        step("Login Data Consumer ", () -> {
            open("marketplace/consumer/listings/");
            LoginSteps.loginMPUser(targetDataConsumer);
            listingsPage.isLoaded();
        });

        String consumerBearerToken = listingsPage.fetchHereAccessToken();

        step("Accept invite by data consumer", () -> {
            listingController
                    .withToken(consumerBearerToken)
                    .acceptInvite(createdInvite.getId());
        });

        step("Subscribe to listing Data Consumer", () -> {
            open("marketplace/consumer/listings/");
            listingsPage.isLoaded()
                    .openListingWithName(listingName);

            var consumerListingPage = new ConsumerListingPage().isLoaded();
            consumerListingPage
                    .subscribeToListing()
                    .seeTermsAndConditions()
                    .acceptTermsAndConditions()
                    .startSubscription()
                    .confirmSubscriptionActivation();
        });

        AtomicReference<String> consentRequestUrl = new AtomicReference<>("");
        step("Create consent request by Data Consumer", () -> {
            var consumerSubscriptionPage = new ConsumerSubscriptionPage().isLoaded();
            consumerSubscriptionPage
                    .createConsentRequest()
                    .fillConsentRequestTitle(faker.company().buzzword())
                    .fillConsentRequestDescription(faker.backToTheFuture().quote())
                    .attachFileWithVINs(new VinsToFile(targetDataSubject.vin).csv())
                    .saveConsentRequest();

            consentRequestUrl.set(new ConsumerConsentRequestPage().isLoaded().copyConsentRequestURLViaClipboard());
        });

        step("Logout Data Consumer", () -> {
            open("logout");
            new LoginPage().isLoaded();
        });

        step("Open consent request link by Data Subject and login", () -> {
            open(consentRequestUrl.get());
            LoginSteps.loginDataSubject(targetDataSubject);
        });

        step("Fill VIN by Data Subject", () ->
                new VINEnteringPage().isLoaded()
                        .fillVINAndContinue(targetDataSubject.vin));

        //todo fulfill ConsentInfo for further steps
        /*OfferDetailsPageSteps.verifyConsentDetailsPage(consentRequest);
        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentRequest);
        this.targetDataSubject.setBearerToken(getUICmToken());
        DLoginPages.loginDataSubjectOnDaimlerSite(targetDataSubject);
        DLoginPages.approveDaimlerScopesAndSubmit();

        SuccessConsentPageSteps.verifyFinalPage(consentRequest);

        Selenide.clearBrowserCookies();
        Selenide.clearBrowserLocalStorage();

        var accessTokenController = new AccessTokenController();
        accessTokenController.withCMToken();
        var accessTokenResponse = accessTokenController.getAccessToken(
                crid,
                targetDataSubject.vin,
                targetDataConsumer.getRealm()
        );

        new ResponseAssertion(accessTokenResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(AccessTokenResponse.class);
        */
    }

    private CreateInvite getInvite(String listingHrn, User targetUser) {
        return CreateInvite.builder()
                .listingHrn(listingHrn)
                .deliveryMethod("EMAIL")
                .emails(List.of(targetUser.getEmail()))
                .callbackUrl(MARKETPLACE_CALLBACK.toString())
                .build();
    }

}
