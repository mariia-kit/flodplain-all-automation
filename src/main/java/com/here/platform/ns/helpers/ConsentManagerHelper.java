package com.here.platform.ns.helpers;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.restEndPoints.external.ConsentManagementCall;
import io.restassured.response.Response;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;


public class ConsentManagerHelper {

    private final Container container;
    private final List<String> vinNumbers;
    private String consentRequestId = StringUtils.EMPTY;
    private final String cmConsumer;

    public ConsentManagerHelper(Container container, String... vinNumbers) {
        this.container = container;
        this.vinNumbers = new ArrayList<>();
        this.vinNumbers.addAll(Arrays.asList(vinNumbers));
        this.cmConsumer = Users.CONSUMER.getUser().getRealm();
    }

    private static void waitForConsentInit() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void waitForActionRetry() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ConsentManagerHelper createConsentRequestWithAppAndVin() {
        if (!Conf.ns().isConsentMock()) {
            String vin =
                    vinNumbers.size() == 1 ? vinNumbers.get(0) : String.join(",", vinNumbers);
            createApplicationForContainer();
            consentRequestId = new ConsentManagementCall()
                    .initConsentRequestStrict(container, cmConsumer);
            new ConsentManagementCall().addVinNumbers(consentRequestId, vin);
            waitForConsentInit();
        }
        return this;
    }

    public ConsentManagerHelper createConsentRequest() {
        if (!Conf.ns().isConsentMock()) {
            consentRequestId = new ConsentManagementCall()
                    .initConsentRequestStrict(container, cmConsumer);
            waitForConsentInit();
        }
        return this;
    }

    public ConsentManagerHelper approveConsent() {
        if (!Conf.ns().isConsentMock()) {
            String authToken = Users.HERE_USER.getToken();

            vinNumbers.forEach(vin -> {
                Response res = new ConsentManagementCall()
                        .approveConsentRequestNew(consentRequestId, vin, authToken, container);
                Assertions.assertEquals(HttpStatus.SC_OK, res.getStatusCode(),
                        "Error during approve of consent " + consentRequestId + " for vin " + vin);
            });
        }
        return this;
    }

    public static String getSHA512(String input) {

        String toReturn = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.reset();
            digest.update(input.getBytes(StandardCharsets.UTF_8));
            toReturn = String.format("%0128x", new BigInteger(1, digest.digest()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    public ConsentManagerHelper revokeConsent() {
        if (!Conf.ns().isConsentMock()) {
            String authToken = Users.HERE_USER.getToken();
            vinNumbers.forEach(vin -> {
                Response res = new ConsentManagementCall()
                        .revokeConsentRequest(consentRequestId, vin, authToken);
                Assertions.assertEquals(200, res.getStatusCode(),
                        "Error during revoke of consent " + consentRequestId + " for vin " + vin);
            });
        }
        return this;
    }

    public String getConsentRequestId() {
        if (!Conf.ns().isConsentMock()) {
            return consentRequestId;
        } else {
            return "ANY_ID";
        }
    }

    public ConsentManagerHelper createApplicationForContainer() {
        if (!Conf.ns().isConsentMock()) {
            new ConsentManagementCall().addCMApplication(container, container.getDataProviderName());
            CleanUpHelper.addToAppsList(container.getDataProviderName(), container.getId());
        }
        return this;
    }

}
