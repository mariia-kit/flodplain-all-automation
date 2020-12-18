package com.here.platform.ns.helpers;

import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.Vehicle;
import io.qameta.allure.Step;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;


public class RegularSubsAndConsent {
    private static RegularFlowData regularFlowData = null;
    private static RegularFlowData regularDaimlerData = null;

    @Step("Use regular Container, Subscription and consent for Reference provider.")
    public synchronized static RegularFlowData useRegularReferenceFlow() {
        if (Objects.isNull(regularFlowData)) {
            DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
            Container container = Containers.generateNew(provider);
            String vehicleId = Vehicle.validVehicleId;
            String consentId = prepareFlowData(container, vehicleId);
            regularFlowData = new RegularFlowData(provider, container, consentId, vehicleId);
        }
        return regularFlowData;
    }

    @Step("Use regular Container, Subscription and consent for Daimler provider.")
    public synchronized static RegularFlowData useRegularDaimlerFlow() {
        if (Objects.isNull(regularDaimlerData)) {
            DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
            Container container = Containers.generateNew(provider);
            String vehicleId = Vehicle.validVehicleId;
            String consentId = prepareFlowData(container, vehicleId);
            regularDaimlerData = new RegularFlowData(provider, container, consentId, vehicleId);
        }
        return regularDaimlerData;
    }

    private static String prepareFlowData(Container container, String vehicleId) {
        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);
        return new ConsentManagerHelper(container, vehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();
    }

    @AllArgsConstructor
    @Data
    public static class RegularFlowData {
        private DataProvider provider;
        private Container container;
        private String consentId;
        private String vehicleId;
    }
}
