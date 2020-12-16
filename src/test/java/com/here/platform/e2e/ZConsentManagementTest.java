package com.here.platform.e2e;

import static com.here.platform.ns.dto.Users.CONSUMER;
import static com.here.platform.ns.dto.Users.MP_CONSUMER;

import com.here.platform.cm.controllers.BMWController;
import com.here.platform.cm.enums.BMWStatus;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.dataProviders.reference.controllers.ReferenceProviderController;
import com.here.platform.ns.controllers.access.ContainerDataController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.ConsentManagerHelper;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.ConsentAfterCleanUp;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.ns.restEndPoints.external.ConsentManagementCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@DisplayName("Consent Management integration Tests: 'E2E'")
@ExtendWith({MarketAfterCleanUp.class, ConsentAfterCleanUp.class})
public class ZConsentManagementTest extends BaseE2ETest {

    @Test
    @DisplayName("Verify E2E flow positive Reference")
    public void CMFlowPositiveReference() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK);

    }

    @Test
    @Tag("ignored-dev")
    @DisplayName("Verify E2E flow positive Old Token")
    public void CMFlowPositiveReferenceOldToken() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .updateTokenToExpire()
                .getConsentRequestId();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_UNAUTHORIZED);

    }

    @Test
    @DisplayName("Verify E2E flow revoke consent")
    @Tag("ignored-dev")
    public void CMFlowRevoke() {
        DataProvider provider = Providers.DAIMLER_EXPERIMENTAL.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleIdLong)
                .createConsentRequestWithAppAndVin()
                .revokeConsent()
                .getConsentRequestId();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleIdLong, container);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getTokenForConsentRevokeError(crid));
    }

    @Test
    @DisplayName("Verify E2E flow with Subscription no consent")
    @Tag("ignored-dev")
    public void CMFlowNoConsent() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(StringUtils.EMPTY)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getTokenForConsentNotFoundError(StringUtils.EMPTY));
    }

    @Test
    @DisplayName("Verify E2E flow add and then revoke consent")
    @Tag("ignored-dev")
    public void CMFlowAddAndRevoke() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleIdLong)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .revokeConsent()
                .getConsentRequestId();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleIdLong, container);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getTokenForConsentRevokeError(crid));
    }

    @Test
    @DisplayName("Verify E2E flow with Consent and Deactivated Subscription")
    @Tag("ignored-dev")
    public void CMFlowWithConsentAndDeactivatedSubs() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndCanceledSubscription(container);

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleIdLong)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleIdLong, container);
        new NeutralServerResponseAssertion(response)
                .expectedSentryError(SentryErrorsList.FORBIDDEN);
    }

    @Test
    @DisplayName("Verify E2E flow with no Consent and no Subscription")
    public void CMFlowWithNoSubsNoConsent() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(StringUtils.EMPTY)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedSentryError(SentryErrorsList.FORBIDDEN);
    }

    @Test
    @DisplayName("Verify E2E flow add Consent, revoke, add new one")
    @Tag("ignored-dev")
    @Disabled("No create of consent for same container allowed")
    public void CMFlowAddConsentAfterRevoke() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .revokeConsent();

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK);

    }

    @Test
    @DisplayName("Verify E2E flow add Consent of other container")
    @Tag("ignored")
    public void CMFlowAddConsentOfOtherContainer() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);
        Container otherContainer = Containers.generateNew(provider);
        otherContainer.withResourceNames("stateofcharge");

        Steps.createRegularContainer(container);
        Steps.createRegularContainer(otherContainer);
        Steps.createListingAndSubscription(container);
        Steps.createListingAndSubscription(otherContainer);

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getTokenForConsentNotFoundError(crid));

    }

    @Test
    @Disabled("No create of consent for same container allowed")
    @DisplayName("Verify E2E flow add Consent than add another")
    public void CMFlowAddConsentAddAnother() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        String firstCrid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();
        String SecondCrid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();

        var response1 = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(SecondCrid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response1)
                .expectedCode(HttpStatus.SC_OK);

        var response2 = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(firstCrid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response2)
                .expectedCode(HttpStatus.SC_OK);

    }

    @Test
    @Tag("ignored-dev")
    @DisplayName("Verify drop of scope cache for CM")
    public void CMFlowDropCache() {
        String scope = "general:some_scope";
        String newScope = "general:new_scope";
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider)
                .withScope(scope);

        Steps.createRegularContainer(container);

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createApplicationForContainer()
                .createConsentRequest()
                .getConsentRequestId();

        String oauthUrl = new ConsentManagementCall().getOAuthState(crid).getHeader("Location");
        Assertions.assertTrue(oauthUrl.contains(scope), "Scope " + scope + " not detected in OAuth url:" + oauthUrl);

        container.setScope(newScope);
        Steps.createRegularContainer(container);

        new ConsentManagementCall().deleteCMCache();
        oauthUrl = new ConsentManagementCall().getOAuthState(crid).getHeader("Location");
        Assertions.assertTrue(oauthUrl.contains(newScope),
                "Scope " + newScope + " not detected in OAuth url:" + oauthUrl);
    }

    @Test
    @Tag("ignored-dev")
    @DisplayName("Verify drop of scope cache for CM Reference Provider Impl")
    public void CMFlowDropCacheReference() {
        String scope = "general:some_scope";
        String newScope = "general:new_scope";
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        Container container = Containers.generateNew(provider)
                .withScope(scope);

        Steps.createRegularContainer(container);

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleIdLong)
                .createApplicationForContainer()
                .createConsentRequest()
                .getConsentRequestId();

        String oauthUrl = new ConsentManagementCall().getOAuthState(crid).getHeader("Location");
        Assertions.assertTrue(!StringUtils.isEmpty(oauthUrl) && oauthUrl.contains(scope),
                "Scope " + scope + " not detected in OAuth url:" + oauthUrl);

        container.setScope(newScope);
        Steps.createRegularContainer(container);

        new ConsentManagementCall().deleteCMCache();
        oauthUrl = new ConsentManagementCall().getOAuthState(crid).getHeader("Location");
        Assertions.assertTrue(oauthUrl.contains(newScope),
                "Scope " + newScope + " not detected in OAuth url:" + oauthUrl);
    }

    @Test
    @Tag("ignored-dev")
    @DisplayName("Verify drop of scope cache for CM Empty Scope")
    public void CMFlowDropCacheEmptyScope() {
        String scope = "mb:vehicle:status:general%20mb:user:pool:reader";
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        Container container = Containers.generateNew(provider).withScope(null);

        Steps.createRegularContainer(container);
        //daimler mb:user:pool:reader%20mb:vehicle:status:general
        //reference mb:vehicle:status:general%20mb:user:pool:reader

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleIdLong)
                .createApplicationForContainer()
                .createConsentRequest()
                .getConsentRequestId();

        String oauthUrl = new ConsentManagementCall().getOAuthState(crid).getHeader("Location");
        Assertions.assertTrue(oauthUrl.contains(scope), "Scope " + scope + " not detected in OAuth url:" + oauthUrl);

        new ConsentManagementCall().deleteCMCache();
        oauthUrl = new ConsentManagementCall().getOAuthState(crid).getHeader("Location");
        Assertions.assertTrue(oauthUrl.contains(scope), "Scope " + scope + " not detected in OAuth url:" + oauthUrl);
    }


    @Test
    @DisplayName("BMW Request Data Provider vehicle data without approve")
    void getRequestVehicleDataBMW() {
        DataProvider provider = Providers.BMW_TEST.getProvider();
        Container container = Containers.generateNew(provider).withResourceNames("fuel");

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .getConsentRequestId();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("BMW Request Data Provider vehicle data after revoke")
    void getRequestVehicleDataBMWRevoke() {
        DataProvider provider = Providers.BMW_TEST.getProvider();
        Container container = Containers.generateNew(provider).withResourceNames("fuel");
        String bmwContainer = "S00I000M001OK";

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .getConsentRequestId();
        var clearanceId = new ReferenceProviderController()
                .getClearanceByVinAndContainerId(Vehicle.validVehicleId, bmwContainer)
                .jsonPath().get("clearanceId").toString();
        new BMWController().setClearanceStatusByBMW(clearanceId, BMWStatus.REVOKED.name());

        var response = new ContainerDataController()
                .withToken(MP_CONSUMER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("BMW Request Data Provider vehicle data, two consumers on same resource")
    void getRequestVehicleDoubleDataBMW() {

        DataProvider provider = Providers.BMW_TEST.getProvider();
        Container container = Containers.generateNew(provider).withResourceNames("fuel");
        String bmwContainer1 = "S00I000M001OK";
        String bmwContainer2 = "vehiclestatus";

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        ProviderApplications targetApp = ProviderApplications.BMW_CONS_1;

        String crid1 = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .getConsentRequestId();
        var clearanceId1 = new ReferenceProviderController()
                .getClearanceByVinAndContainerId(Vehicle.validVehicleId, bmwContainer1)
                .jsonPath()
                .get("clearanceId").toString();
        new BMWController().setClearanceStatusByBMW(clearanceId1, BMWStatus.REVOKED.name());

        String crid2 = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .getConsentRequestId();
        var clearanceId2 = new ReferenceProviderController()
                .getClearanceByVinAndContainerId(Vehicle.validVehicleId, bmwContainer2)
                .jsonPath()
                .get("clearanceId").toString();
        new BMWController().setClearanceStatusByBMW(clearanceId2, BMWStatus.APPROVED.name());

        OnboardingSteps onboard = new OnboardingSteps(targetApp.provider, targetApp.consumer.getRealm());
        onboard.onboardTestProviderApplication(
                container.getId(),
                bmwContainer2,
                targetApp.container.clientSecret);
        var response1 = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid1)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);

        var response2 = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid2)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response2)
                .expectedCode(HttpStatus.SC_UNAUTHORIZED);
    }

}
