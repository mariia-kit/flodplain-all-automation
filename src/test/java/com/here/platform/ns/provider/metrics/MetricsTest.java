package com.here.platform.ns.provider.metrics;

import static com.here.platform.ns.dto.Users.APPLICATION;
import static com.here.platform.ns.dto.Users.CONSUMER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.access.ContainerDataController;
import com.here.platform.ns.controllers.provider.ContainerController;
import com.here.platform.ns.controllers.provider.TAMetricsController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.ConsentManagerHelper;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.ConsentAfterCleanUp;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        var response = new TAMetricsController()
                .withToken(APPLICATION)
                .getTaMetrics("2019-08-29");
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("records[0].providerId", "daimler",
                        "TA Provider metrics not ok");
    }

    @Test
    @DisplayName("Verify Provider TA metrics statistics")
    void verifyProviderTAMetricsStatistics() {
        var response = new TAMetricsController()
                .withToken(APPLICATION)
                .getTaMetricsStatistics("2019-08-29");
        new NeutralServerResponseAssertion(response)
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

        var response = new TAMetricsController()
                .withToken(APPLICATION)
                .getTaMetricsStatistics(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        new NeutralServerResponseAssertion(response)
                .expectedJsonContains("records.find {it.containerId == '" + container.getId()
                                + "'}.apiCallCount",
                        StringUtils.EMPTY, "Metric Statistic field value not as expected!");

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();

        var getContainer = new ContainerDataController()
                .withToken(CONSUMER)
                .withCampaignId(crid)
                .withQueryParam("resource", "distancesincereset")
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(getContainer)
                .expectedCode(HttpStatus.SC_OK);

        var response2 = new TAMetricsController()
                .withToken(APPLICATION)
                .getTaMetricsStatistics(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        new NeutralServerResponseAssertion(response2)
                .expectedJsonContains("records.find {it.containerId == '" + container.getId()
                                + "'}.apiCallCount",
                        "1", "Metric Statistic field value not as expected!");

        var getContainer1 = new ContainerDataController()
                .withToken(CONSUMER)
                .withCampaignId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(getContainer1)
                .expectedCode(HttpStatus.SC_OK);

        var response3 = new TAMetricsController()
                .withToken(APPLICATION)
                .getTaMetricsStatistics(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        new NeutralServerResponseAssertion(response3)
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
        var addContainer = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(addContainer)
                .expectedCode(HttpStatus.SC_OK);
        Steps.createListingAndSubscription(container);

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();

        var getContainer = new ContainerDataController()
                .withToken(CONSUMER)
                .withCampaignId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(getContainer)
                .expectedCode(HttpStatus.SC_NOT_FOUND);

        var response = new TAMetricsController()
                .withToken(APPLICATION)
                .getTaMetricsStatistics(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        new NeutralServerResponseAssertion(response)
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

        var getContainer = new ContainerDataController()
                .withToken(CONSUMER)
                .withCampaignId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(getContainer)
                .expectedCode(HttpStatus.SC_OK);

        var response = new TAMetricsController()
                .withToken(APPLICATION)
                .getTaMetricsStatistics(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        new NeutralServerResponseAssertion(response)
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

        var getContainer = new ContainerDataController()
                .withToken(CONSUMER)
                .withCampaignId(crid)
                .withQueryParam("empty", "on")
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(getContainer)
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        var response = new TAMetricsController()
                .withToken(APPLICATION)
                .getTaMetricsStatistics(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        new NeutralServerResponseAssertion(response)
                .expectedJsonContains("records.find {it.containerId == '" + container.getId()
                                + "'}.apiCallCount",
                        StringUtils.EMPTY, "Metric Statistic field value not as expected!");


    }

}
