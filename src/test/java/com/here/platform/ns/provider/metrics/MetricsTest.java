package com.here.platform.ns.provider.metrics;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.ConsentManagerHelper;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.ConsentAfterCleanUp;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.neutralServer.resources.GetContainerDataByVehicleCall;
import com.here.platform.ns.restEndPoints.provider.container_info.AddContainerCall;
import com.here.platform.ns.restEndPoints.provider.technicalAccountingService.taMetricsCall;
import com.here.platform.ns.restEndPoints.provider.technicalAccountingService.taMetricsStatistics;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@DisplayName("Verify Provider Technical Accounting Service")
@ExtendWith({MarketAfterCleanUp.class, ConsentAfterCleanUp.class})
public class MetricsTest extends BaseNSTest {

    @Test
    @DisplayName("Verify Provider TA metrics")
    void verifyProviderTAMetrics() {
        new taMetricsCall("2019-08-29")
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("records[0].providerId", "daimler",
                        "TA Provider metrics not ok");
    }

    @Test
    @DisplayName("Verify Provider TA metrics statistics")
    void verifyProviderTAMetricsStatistics() {
        new taMetricsStatistics("2019-08-29")
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("records[0].apiCallCount", "1",
                        "TA Provider metrics statistics not ok");
    }

    @Test
    @DisplayName("Verify Provider TA metrics statistics add new record")
    void verifyProviderTAMetricsStatisticsNewRecord() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        new taMetricsStatistics()
                .call()
                .expectedJsonContains("records.find {it.containerId == '" + container.getId()
                                + "'}.apiCallCount",
                        StringUtils.EMPTY, "Metric Statistic field value not as expected!");

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(crid)
                .withQueryParam("resource=distancesincereset")
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new taMetricsStatistics()
                .call()
                .expectedJsonContains("records.find {it.containerId == '" + container.getId()
                                + "'}.apiCallCount",
                        "1", "Metric Statistic field value not as expected!");

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(crid)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new taMetricsStatistics()
                .call()
                .expectedJsonContains("records.find {it.containerId == '" + container.getId()
                                + "'}.apiCallCount",
                        "2", "Metric Statistic field value not as expected!");
    }

    @Test
    @DisplayName("Verify Provider TA metrics statistics failed request")
    void verifyProviderTAMetricsStatisticsFailedReq() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider).withResourceNames("oil");

        Steps.createRegularProvider(provider);
        new AddContainerCall(container)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);
        Steps.createListingAndSubscription(container);

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(ConsentManagerHelper.getValidConsentId())
                .call()
                .expectedCode(HttpStatus.SC_NOT_FOUND);

        new taMetricsStatistics()
                .call()
                .expectedJsonContains("records.find {it.containerId == '" + container.getId()
                                + "'}.apiCallCount",
                        StringUtils.EMPTY, "Metric Statistic field value not as expected!");
    }

    @Test
    @DisplayName("Verify Provider TA metrics statistics deep")
    void verifyProviderTAMetricsStatisticDeep() {
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


        new taMetricsStatistics()
                .call()
                .expectedJsonContains("records.find {it.containerId == '" + container.getId()
                                + "'}.apiCallCount",
                        "1", "Metric Statistic field value not as expected!");
    }

    @Test
    @DisplayName("Verify Provider TA metrics statistics add new record empty response")
    void verifyProviderTAMetricsStatisticsNewRecordEmptyResponse() {
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
                .withQueryParam("additional-fields=empty&additional-values=on")
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        new taMetricsStatistics()
                .call()
                .expectedJsonContains("records.find {it.containerId == '" + container.getId()
                                + "'}.apiCallCount",
                        StringUtils.EMPTY, "Metric Statistic field value not as expected!");


    }
}
