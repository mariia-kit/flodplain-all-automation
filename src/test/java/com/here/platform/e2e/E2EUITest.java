package com.here.platform.e2e;

import static com.here.platform.ns.dto.Users.MP_CONSUMER;
import static com.here.platform.ns.dto.Users.MP_PROVIDER;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.cm.steps.ui.ConsentManagementFlowSteps;
import com.here.platform.cm.steps.ui.OfferDetailsPageSteps;
import com.here.platform.cm.steps.ui.SuccessConsentPageSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.annotations.CMFeatures.BMW;
import com.here.platform.common.config.Conf;
import com.here.platform.common.extensions.ConsentRequestRemoveExtension;
import com.here.platform.common.extensions.UserAccountCleanUpExtension;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.dataProviders.daimler.steps.DaimlerLoginPage;
import com.here.platform.dataProviders.reference.steps.ReferenceApprovePage;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import com.here.platform.mp.models.CreatedInvite;
import com.here.platform.mp.steps.ui.MarketplaceFlowSteps;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.helpers.TokenManager;
import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.selenide.LogType;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.stream.Stream;
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
        Container targetContainer = Containers.DAIMLER_EXPERIMENTAL_ODOMETER.getContainer();
        ConsentInfo consentRequest =
                new ConsentInfo()
                        .title(faker.company().buzzword())
                        .purpose(faker.backToTheFuture().quote())
                        .consumerName(Users.MP_CONSUMER.getUser().getName())
                        .containerName(targetContainer.getName())
                        .containerDescription(targetContainer.getDescription())
                        .resources(List.of(targetContainer.getResourceNames()))
                        .vinLabel(new VIN(targetDataSubject.getVin()).label())
                        .privacyPolicy(faker.internet().domainName());

        var listingName = "[E2E test] " + faker.company().buzzword();

        MarketplaceFlowSteps.loginDataProvider(targetDataProvider);
        MarketplaceFlowSteps.createAndSubmitListing(listingName, targetContainer, targetDataConsumer.getEmail());
        CreatedInvite createdInvite = MarketplaceFlowSteps.inviteConsumerToListing(targetDataConsumer);
        listingHrn = MarketplaceFlowSteps.getListingHrn();
        HereLoginSteps.logout(targetDataProvider);

        MarketplaceFlowSteps.loginDataConsumer(targetDataConsumer);

        MarketplaceFlowSteps.acceptInviteByConsumer(createdInvite.getId());
        MarketplaceFlowSteps.subscribeToListing(listingName);
        String consentUrl = MarketplaceFlowSteps
                .createConsentByConsumer(consentRequest, targetContainer, targetDataSubject.getVin());

        var consentRequestUrl = new AtomicReference<>(consentUrl);
        var crid = getCridFromUrl(consentRequestUrl.get());
        consentRequestRemoveExtension.cridToRemove(crid).vinToRemove(targetDataSubject.getVin());

        HereLoginSteps.logout(targetDataConsumer);

        ConsentManagementFlowSteps.openConsentLink(consentRequestUrl.get());
        HereLoginSteps.loginRegisteredDataSubject(targetDataSubject);

        new VINEnteringPage().isLoaded().fillVINAndContinue(targetDataSubject.getVin());
        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentRequest);
        DaimlerLoginPage.loginDataSubjectOnDaimlerSite(targetDataSubject);
        if (!SuccessConsentPageSteps.isLoaded()) {
            DaimlerLoginPage.approveDaimlerLegalAndSubmit();
            DaimlerLoginPage.approveDaimlerScopesAndSubmit();
        }
        SuccessConsentPageSteps.verifyFinalPage(consentRequest);

        Steps.getVehicleResourceAndVerify(crid, targetDataSubject.getVin(), targetContainer);
    }

    @Test
    @BMW
    @Tag("e2e_prod")
    @Tag("bmw_e2e")
    @DisplayName("Positive BMW E2E flow of creating consent request via UI")
    void simpleHappyPathTestBMW() {
        DataProvider provider = Providers.BMW_TEST.getProvider();
        Container targetContainer = Containers.generateNew(provider).withResourceNames("fuel");
        var validVIN = Vehicle.validVehicleId;
        ConsentInfo consentRequest =
                new ConsentInfo()
                        .title(faker.company().buzzword())
                        .purpose(faker.backToTheFuture().quote())
                        .consumerName(Users.MP_CONSUMER.getUser().getName())
                        .containerName(targetContainer.getName())
                        .containerDescription(targetContainer.getDescription())
                        .resources(List.of(targetContainer.getResourceNames()))
                        .vinLabel(new VIN(validVIN).label());

        Steps.createRegularContainer(targetContainer);
        new OnboardingSteps(provider.getName(), targetDataConsumer.getRealm())
                .onboardTestProviderApplication(
                        targetContainer.getId(),
                        Conf.ns().getBmwApp().getClientId(),
                        Conf.ns().getBmwApp().getClientSecret()
                );

        var listingName = "[E2E BMW test] " + faker.company().buzzword();

        MarketplaceFlowSteps.loginDataProvider(targetDataProvider);
        MarketplaceFlowSteps.createAndSubmitListing(listingName, targetContainer, targetDataConsumer.getEmail());
        CreatedInvite createdInvite = MarketplaceFlowSteps.inviteConsumerToListing(targetDataConsumer);
        listingHrn = MarketplaceFlowSteps.getListingHrn();
        HereLoginSteps.logout(targetDataProvider);

        MarketplaceFlowSteps.loginDataConsumer(targetDataConsumer);

        MarketplaceFlowSteps.acceptInviteByConsumer(createdInvite.getId());
        MarketplaceFlowSteps.subscribeToListing(listingName);
        String consentUrl = MarketplaceFlowSteps
                .createConsentByConsumer(consentRequest, targetContainer, targetDataSubject.getVin());

        var consentRequestUrl = new AtomicReference<>(consentUrl);
        var crid = getCridFromUrl(consentRequestUrl.get());
        consentRequestRemoveExtension.cridToRemove(crid).vinToRemove(targetDataSubject.getVin());

        HereLoginSteps.logout(targetDataConsumer);

        ConsentManagementFlowSteps.openConsentLink(consentRequestUrl.get());
        HereLoginSteps.loginRegisteredDataSubject(targetDataSubject);

        ConsentFlowSteps
                .approveConsentForVinBMW(ProviderApplications.BMW_CONS_1.container.clientId, Vehicle.validVehicleId);

        Steps.getVehicleResourceAndVerify(crid, targetDataSubject.getVin(), targetContainer);
    }

    private String getCridFromUrl(String consentRequestUrl) {
        var pathSegments = UriComponentsBuilder.fromUriString(consentRequestUrl).build().getPathSegments();
        return pathSegments.get(pathSegments.size() - 1);
    }

}
