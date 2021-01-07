package com.here.platform.ns.access.vehicleResources;

import static com.here.platform.common.strings.SBB.sbb;
import static com.here.platform.ns.dto.Users.CONSUMER;
import static com.here.platform.ns.dto.Users.EXTERNAL_USER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.common.annotations.CMFeatures.BMW;
import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.access.ContainerDataController;
import com.here.platform.ns.controllers.provider.ContainerController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.ContainerResources;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.helpers.RegularSubsAndConsent;
import com.here.platform.ns.helpers.RegularSubsAndConsent.RegularFlowData;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag("NS-Data provider")
@DisplayName("Get resources by vehicle Id and container Id")
@ExtendWith({MarketAfterCleanUp.class})
class GetResourcesByVehicleAndContainerTest extends BaseNSTest {

    @Test
    @DisplayName("Verify get resources by vehicle Id and container Id Successful")
    void verifyGetContainersDataRetrieved() {
        RegularFlowData regularFlowData = RegularSubsAndConsent.useRegularDaimlerFlow();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(regularFlowData.getConsentId())
                .withXCorrelationId(sbb("X-corr-1-").append(regularFlowData.getConsentId()).bld())
                .getContainerForVehicle(regularFlowData.getProvider(), regularFlowData.getVehicleId(), regularFlowData.getContainer());
        new NeutralServerResponseAssertion(response)
                .expectedEqualsISOContainerData(
                        Vehicle.odometerResource,
                        "Provider content not as expected!");
    }

    @Test
    @Tag("ignored-dev")
    @Tag("bmw_ns")
    @BMW
    @Issue("OLPPORT-3252")
    @DisplayName("Positive Verify get BMW resources by vehicle Id and container Id")
    void verifyGetContainersDataRetrievedBMW() {
        DataProvider provider = Providers.BMW_TEST.getProvider();
        Container container = Containers.generateNew(provider).withResourceNames("fuel");

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        ConsentObject consentObj = new ConsentObject(container);
        String crid = new ConsentRequestSteps(consentObj)
                .onboardApplicationForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(Vehicle.validVehicleId)
                .getId();

        ConsentFlowSteps
                .approveConsentForVinBMW(ProviderApplications.BMW_CONS_1.container.clientId, Vehicle.validVehicleId);

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedEqualsISOContainerData(
                        Vehicle.fuelResource,
                        "Provider content not as expected!");
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id and container Id bad token Expected")
    void verifyGetContainersDataRetrievedBadToken() {
        RegularFlowData regularFlowData = RegularSubsAndConsent.useRegularDaimlerFlow();
        var response = new ContainerDataController()
                .withToken(EXTERNAL_USER)
                .withConsentId(regularFlowData.getConsentId())
                .getContainerForVehicle(regularFlowData.getProvider(), regularFlowData.getVehicleId(), regularFlowData.getContainer());
        new NeutralServerResponseAssertion(response)
                .expectedSentryError(SentryErrorsList.FORBIDDEN);
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id and container Id forbidden Expected")
    void verifyGetContainersDataRetrievedBadWrong() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);

        ConsentObject consentObj = new ConsentObject(container);
        String crid = new ConsentRequestSteps(consentObj)
                .onboardApplicationForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(Vehicle.validVehicleId)
                .getId();
        var response = new ContainerDataController()
                .withToken(PROVIDER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedSentryError(SentryErrorsList.FORBIDDEN);
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id and container Id: Invalid Resource")
    void verifyGetContainersDataRetrievedResourceInvalid() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider).withResourceNames(ContainerResources.oil.getName());

        Steps.createRegularProvider(provider);
        new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        Steps.createListingAndSubscription(container);

        ConsentObject consentObj = new ConsentObject(container);
        String crid = new ConsentRequestSteps(consentObj)
                .onboardApplicationForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(Vehicle.validVehicleId)
                .getId();
        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id and container Id: Capital")
    void verifyGetContainersDataRetrievedResourceCapital() {
        RegularFlowData regularFlowData = RegularSubsAndConsent.useRegularDaimlerFlow();
        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(regularFlowData.getConsentId())
                .getContainerForVehicle(Providers.DAIMLER_CAPITAL.getProvider(), regularFlowData.getVehicleId(), regularFlowData.getContainer());
        new NeutralServerResponseAssertion(response)
                .expectedSentryError(SentryErrorsList.FORBIDDEN);
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id and container Id: Invalid Vehicle")
    @Tag("ignored-dev")
    void verifyGetContainersDataRetrievedResourceInvalidVehicle() {
        RegularFlowData regularFlowData = RegularSubsAndConsent.useRegularReferenceFlow();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(regularFlowData.getConsentId())
                .getContainerForVehicle(regularFlowData.getProvider(), Vehicle.invalidVehicleId, regularFlowData
                        .getContainer());
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getCMInvalidVehicleError(regularFlowData.getConsentId()));
    }

    @Test
    @Tag("ignored-dev")
    @DisplayName("Verify get resources by vehicle Id and container Id with no Campaign Id")
    void verifyGetContainersDataNoCampaignId() {
        RegularFlowData regularFlowData = RegularSubsAndConsent.useRegularReferenceFlow();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .getContainerForVehicle(regularFlowData.getProvider(), regularFlowData.getVehicleId(), regularFlowData
                        .getContainer());
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_NOT_FOUND)
                .expectedHeaderIsPresent("X-Correlation-ID");

    }

    @Test
    @DisplayName("Verify get resources by vehicle Id and container Id for empty response Reference")
    void verifyGetContainersDataRetrievedEmptyReference() {
        RegularFlowData regularFlowData = RegularSubsAndConsent.useRegularReferenceFlow();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(regularFlowData.getConsentId())
                .withQueryParam("empty", "on")
                .getContainerForVehicle(regularFlowData.getProvider(), regularFlowData.getVehicleId(), regularFlowData
                        .getContainer());
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_NO_CONTENT)
                .expectedBody(StringUtils.EMPTY, "Expected empty body!");
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id no-PII")
    void verifyGetContainersDataRetrievedNoPII() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        Container container = Containers.generateNew(provider)
                .withResourceNames("fuel")
                .withConsentRequired(false);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK);
    }

    @Test
    @Tag("ignored-dev")
    @DisplayName("Verify get resources by vehicle Id and container Id no consentId")
    void verifyGetContainersDataRetrievedNoConsentId() {
        RegularFlowData regularFlowData = RegularSubsAndConsent.useRegularReferenceFlow();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(null)
                .getContainerForVehicle(regularFlowData.getProvider(), regularFlowData.getVehicleId(), regularFlowData
                        .getContainer());
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getCMNoConsentIdProvided("null"));
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id and container Id no Token")
    void verifyGetContainersDataRetrievedNoToken() {
        RegularFlowData regularFlowData = RegularSubsAndConsent.useRegularReferenceFlow();

        var response = new ContainerDataController()
                .withToken(StringUtils.EMPTY)
                .withConsentId(null)
                .getContainerForVehicle(regularFlowData.getProvider(), regularFlowData.getVehicleId(), regularFlowData
                        .getContainer());
        new NeutralServerResponseAssertion(response)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());

    }

}
