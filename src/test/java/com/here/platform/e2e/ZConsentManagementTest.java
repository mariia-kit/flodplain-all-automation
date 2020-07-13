package com.here.platform.e2e;

import com.here.platform.ns.BaseNSTest;
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
import com.here.platform.ns.restEndPoints.external.ConsentManagementCall;
import com.here.platform.ns.restEndPoints.neutralServer.resources.GetContainerDataByVehicleCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@DisplayName("Consent Management integration Tests: 'E2E'")
@ExtendWith({MarketAfterCleanUp.class, ConsentAfterCleanUp.class})
public class ZConsentManagementTest extends BaseE2ETest {

    @Test
    @DisplayName("Verify E2E flow positive")
    public void CMFlowPositive() {
        DataProvider provider = Providers.DAIMLER_EXPERIMENTAL.getProvider();
        Container container = Containers.DAIMLER_EXPERIMENTAL_ODOMETER.getContainer();

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleIdLong)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleIdLong,
                container.getId())
                .withHeader("X-Correlation-ID", "X-corr-12345")
                .withCampaignId(crid)
                .call()
                .expectedCode(HttpStatus.SC_OK);

    }

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

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(crid)
                .call()
                .expectedCode(HttpStatus.SC_OK);

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

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleIdLong,
                container.getId())
                .withCampaignId(crid)
                .call()
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

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(StringUtils.EMPTY)
                .call()
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

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .revokeConsent()
                .getConsentRequestId();

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(crid)
                .call()
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

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(crid)
                .call()
                .expectedSentryError(SentryErrorsList.FORBIDDEN);
    }

    @Test
    @DisplayName("Verify E2E flow with no Consent and no Subscription")
    public void CMFlowWithNoSubsNoConsent() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(StringUtils.EMPTY)
                .call()
                .expectedSentryError(SentryErrorsList.FORBIDDEN);
    }

    @Test
    @DisplayName("Verify E2E flow add Consent, revoke, add new one")
    @Tag("ignored-dev")
    public void CMFlowAddConsentAfterRevoke() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.REF_DAIMLER_ODOMETER.getContainer();

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .revokeConsent();

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(crid)
                .call()
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

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                otherContainer.getId())
                .withCampaignId(crid)
                .call()
                .expectedError(NSErrors.getTokenForConsentNotFoundError(crid));

    }

    @Test
    @DisplayName("Verify E2E flow add Consent than add another")
    public void CMFlowAddConsentAddAnother() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.REF_DAIMLER_ODOMETER.getContainer();

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

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(SecondCrid)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(firstCrid)
                .call()
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
        Assertions.assertTrue(oauthUrl.contains(newScope), "Scope " + newScope + " not detected in OAuth url:" + oauthUrl);
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

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
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
        Assertions.assertTrue(oauthUrl.contains(newScope), "Scope " + newScope + " not detected in OAuth url:" + oauthUrl);
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

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createApplicationForContainer()
                .createConsentRequest()
                .getConsentRequestId();

        String oauthUrl = new ConsentManagementCall().getOAuthState(crid).getHeader("Location");
        Assertions.assertTrue(oauthUrl.contains(scope), "Scope " + scope + " not detected in OAuth url:" + oauthUrl);

        new ConsentManagementCall().deleteCMCache();
        oauthUrl = new ConsentManagementCall().getOAuthState(crid).getHeader("Location");
        Assertions.assertTrue(oauthUrl.contains(scope), "Scope " + scope + " not detected in OAuth url:" + oauthUrl);
    }
}
