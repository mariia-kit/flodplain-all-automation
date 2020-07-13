package com.here.platform.ns.restEndPoints.provider.technicalAccountingService;

import static com.here.platform.ns.dto.Users.APPLICATION;

import com.here.platform.ns.dto.CallsUrl;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.restassured.response.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;


public class taMetricsStatistics extends BaseRestControllerNS<taMetricsStatistics> {

    public taMetricsStatistics(String date) {
        callMessage = String
                .format("Call metrics statistics for date '%s'", date);
        setDefaultUser(APPLICATION);
        endpointUrl = CallsUrl.GET_PROVIDER_METRICS_STATISTICS.builder()
                .getUrl() + "?date=" + date;
    }

    public taMetricsStatistics() {
        this(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.get(callMessage, endpointUrl, getToken());
    }

}
