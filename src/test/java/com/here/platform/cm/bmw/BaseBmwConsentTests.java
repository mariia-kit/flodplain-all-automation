package com.here.platform.cm.bmw;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.rest.model.ConsentRequestAsyncUpdateInfo;
import com.here.platform.cm.rest.model.ConsentRequestAsyncUpdateInfo.StatusEnum;
import com.here.platform.common.annotations.CMFeatures.BMW;
import io.qameta.allure.Issue;
import io.qameta.allure.Issues;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;


@BMW
@Issues({@Issue("OLPPORT-3250"), @Issue("OLPPORT-3251")})
public class BaseBmwConsentTests extends BaseCMTest {

    String testVin1 = "2AD190A6AD057824E",
            testVin2 = "2AD190A6AD0578AAA";

    protected void waitForAsyncBMWRequest(String asyncId) {
        int maxCount = 10;
        while (maxCount > 0) {
            maxCount--;
            Response resp = consentRequestController.getConsentRequestAsyncUpdateInfo(asyncId);
            ConsentRequestAsyncUpdateInfo convertedObject = resp.as(ConsentRequestAsyncUpdateInfo.class);
            if (!convertedObject.getStatus().equals(StatusEnum.IN_PROGRESS)) {
                break;
            }
            fuSleep();
        }

    }

}
