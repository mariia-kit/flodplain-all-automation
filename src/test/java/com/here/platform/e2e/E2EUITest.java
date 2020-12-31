package com.here.platform.e2e;

import static com.here.platform.ns.dto.Users.MP_CONSUMER;
import static com.here.platform.ns.dto.Users.MP_PROVIDER;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.pages.LandingPage;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.ui.ConsentManagementFlowSteps;
import com.here.platform.cm.steps.ui.OfferDetailsPageSteps;
import com.here.platform.cm.steps.ui.SuccessConsentPageSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.annotations.CMFeatures.BMW;
import com.here.platform.common.config.Conf;
import com.here.platform.common.extensions.ConsentRequestRemoveExtension;
import com.here.platform.common.extensions.UserAccountCleanUpExtension;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.dataProviders.daimler.steps.DaimlerLoginPage;
import com.here.platform.dataProviders.reference.steps.ReferenceApprovePage;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import com.here.platform.mp.models.CreatedInvite;
import com.here.platform.mp.steps.ui.MarketplaceFlowSteps;
import com.here.platform.ns.controllers.access.ContainerDataController;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.selenide.LogType;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
@Tag("e2e_ui")
@Execution(ExecutionMode.SAME_THREAD)
public class E2EUITest extends BaseE2ETest {

    static {
        Configuration.baseUrl = Conf.mp().getMarketplaceUiBaseUrl();
        Configuration.driverManagerEnabled = true;
        Configuration.pollingInterval = 400;
        Configuration.browserSize = "1366x1000";
        SelenideLogger.addListener("AllureListener", new AllureSelenide().enableLogs(LogType.BROWSER, Level.ALL));
    }

    private final DataSubject targetDataSubject = DataSubjects.getNextBy18VINLength().dataSubject;
    private final DataSubject targetDataSubject17 = DataSubjects.getNextBy17VINLength().dataSubject;
    private final User
            targetDataProvider = MP_PROVIDER.getUser(),
            targetDataConsumer = MP_CONSUMER.getUser();


    @org.testcontainers.junit.jupiter.Container
    public BrowserWebDriverContainer chrome =
            new BrowserWebDriverContainer()
                    .withCapabilities(new ChromeOptions().addArguments("--no-sandbox"))
                    .withRecordingMode(VncRecordingMode.RECORD_FAILING, new File("build/video"));
    @RegisterExtension
    ConsentRequestRemoveExtension consentRequestRemoveExtension = new ConsentRequestRemoveExtension();
    @RegisterExtension
    UserAccountCleanUpExtension userAccountCleanUpExtension = UserAccountCleanUpExtension.builder()
            .targetDataSubject(targetDataSubject)
            .build();
    @RegisterExtension
    UserAccountCleanUpExtension userAccountCleanUpExtension17 = UserAccountCleanUpExtension.builder()
            .targetDataSubject(targetDataSubject17)
            .build();

    private String listingHrn;

    @BeforeEach
    void setUpBrowserContainer() {
        RemoteWebDriver driver = chrome.getWebDriver();
        WebDriverRunner.setWebDriver(driver);
        WebDriverRunner.getWebDriver().manage().window().setSize(new Dimension(1366, 1000));
    }

    @AfterEach
    void afterEach() {
        MarketplaceFlowSteps.removeSubscriptionAndListingForListings(listingHrn);
    }

    @Test
    @Tag("e2e_prod")
    @DisplayName("Simple happy path E2E UI level")
    void simpleHappyPathTest() {
        MPProviders provider = MPProviders.DAIMLER_EXPERIMENTAL;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.DAIMLER_EXPERIMENTAL_CHARGE.getConsentContainer();
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);

        new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest();

        var listingName = "[E2E test] " + faker.company().buzzword();

        MarketplaceFlowSteps.loginDataProvider(targetDataProvider);
        MarketplaceFlowSteps.createAndSubmitListing(listingName, consentObj.getNSContainer(), targetDataConsumer.getEmail());
        CreatedInvite createdInvite = MarketplaceFlowSteps.inviteConsumerToListing(targetDataConsumer);
        listingHrn = MarketplaceFlowSteps.getListingHrn();
        HereLoginSteps.logout(targetDataProvider);

        MarketplaceFlowSteps.loginDataConsumer(targetDataConsumer);

        MarketplaceFlowSteps.acceptInviteByConsumer(createdInvite.getId());
        MarketplaceFlowSteps.subscribeToListing(listingName);
        String consentUrl = MarketplaceFlowSteps
                .createConsentByConsumer(consentObj, targetDataSubject.getVin());

        var crid = getCridFromUrl(consentUrl);
        consentRequestRemoveExtension.cridToRemove(crid).vinToRemove(targetDataSubject.getVin());

        HereLoginSteps.logout(targetDataConsumer);

        ConsentManagementFlowSteps.openConsentLink(consentUrl);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginRegisteredDataSubject(targetDataSubject);

        new VINEnteringPage().isLoaded().fillVINAndContinue(targetDataSubject.getVin());
        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentObj.getConsent());
        DaimlerLoginPage.loginDataSubjectOnDaimlerSite(targetDataSubject);
        if (!SuccessConsentPageSteps.isLoaded()) {
            DaimlerLoginPage.approveDaimlerLegalAndSubmit();
            DaimlerLoginPage.approveDaimlerScopesAndSubmit();
        }
        SuccessConsentPageSteps.verifyFinalPage(consentObj.getConsent());

        Steps.getVehicleResourceAndVerify(crid, targetDataSubject.getVin(), consentObj.getNSContainer());
    }

    @Test
    @BMW
    @Tag("e2e_prod")
    @Tag("bmw_e2e")
    @DisplayName("Positive BMW E2E flow of creating consent request via UI")
    void simpleHappyPathTestBMW() {
        MPProviders provider = MPProviders.BMW_TEST;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer testContainer = ConsentRequestContainers.generateNew(provider)
                .withResources(List.of("fuel"));
        String validVIN = Vehicle.validVehicleId;

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, testContainer);
        var step= new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest();

        var listingName = "[E2E BMW test] " + faker.company().buzzword();

        MarketplaceFlowSteps.loginDataProvider(targetDataProvider);
        MarketplaceFlowSteps.createAndSubmitListing(listingName, consentObj.getNSContainer(), targetDataConsumer.getEmail());
        CreatedInvite createdInvite = MarketplaceFlowSteps.inviteConsumerToListing(targetDataConsumer);
        listingHrn = MarketplaceFlowSteps.getListingHrn();
        HereLoginSteps.logout(targetDataProvider);

        MarketplaceFlowSteps.loginDataConsumer(targetDataConsumer);

        MarketplaceFlowSteps.acceptInviteByConsumer(createdInvite.getId());
        MarketplaceFlowSteps.subscribeToListing(listingName);
        String consentUrl = MarketplaceFlowSteps
                .createConsentByConsumer(consentObj, validVIN);
        var crid = getCridFromUrl(consentUrl);
        consentRequestRemoveExtension.cridToRemove(crid).vinToRemove(validVIN);

        HereLoginSteps.logout(targetDataConsumer);

        ConsentManagementFlowSteps.openConsentLink(consentUrl);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginRegisteredDataSubject(targetDataSubject);

        ConsentFlowSteps
                .approveConsentForVinBMW(testContainer.getClientId(), validVIN);

        Steps.getVehicleResourceAndVerify(crid, validVIN, consentObj.getNSContainer());
    }

    @Test
    //@Disabled("Used for flow verification using reference provider")
    @DisplayName("Simple happy path E2E UI level Reference")
    void simpleHappyPathTestReference() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);

        new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest();

        var listingName = "[E2E test] " + faker.company().buzzword();

        MarketplaceFlowSteps.loginDataProvider(targetDataProvider);
        MarketplaceFlowSteps.createAndSubmitListing(listingName, consentObj.getNSContainer(), targetDataConsumer.getEmail());
        CreatedInvite createdInvite = MarketplaceFlowSteps.inviteConsumerToListing(targetDataConsumer);
        listingHrn = MarketplaceFlowSteps.getListingHrn();
        HereLoginSteps.logout(targetDataProvider);

        MarketplaceFlowSteps.loginDataConsumer(targetDataConsumer);

        MarketplaceFlowSteps.acceptInviteByConsumer(createdInvite.getId());
        MarketplaceFlowSteps.subscribeToListing(listingName);
        String consentUrl = MarketplaceFlowSteps
                .createConsentByConsumer(consentObj, targetDataSubject17.getVin());

        var crid = getCridFromUrl(consentUrl);
        consentRequestRemoveExtension.cridToRemove(crid).vinToRemove(targetDataSubject17.getVin());

        HereLoginSteps.logout(targetDataConsumer);

        ConsentManagementFlowSteps.openConsentLink(consentUrl);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginRegisteredDataSubject(targetDataSubject17);

        new VINEnteringPage().isLoaded().fillVINAndContinue(targetDataSubject17.getVin());
        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentObj.getConsent());
        ReferenceApprovePage.approveReferenceScopesAndSubmit(targetDataSubject17.getVin());
        SuccessConsentPageSteps.verifyFinalPage(consentObj.getConsent());

        var response = new ContainerDataController()
                .withBearerToken(Users.MP_CONSUMER.getToken())
                .withConsentId(crid)
                .getContainerForVehicle(
                        targetContainer.getProvider().getName(),
                        targetDataSubject17.getVin(),
                        consentObj.getContainer().getId()
                );
        new NeutralServerResponseAssertion(response).expectedCode(404);
    }

    private String getCridFromUrl(String consentRequestUrl) {
        var pathSegments = UriComponentsBuilder.fromUriString(consentRequestUrl).build().getPathSegments();
        return pathSegments.get(pathSegments.size() - 1);
    }

}
