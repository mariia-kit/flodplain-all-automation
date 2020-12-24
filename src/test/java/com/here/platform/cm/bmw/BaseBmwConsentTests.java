package com.here.platform.cm.bmw;

import com.github.javafaker.Faker;
import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.ConsentRequestController;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.dataAdapters.ConsentContainerToNsContainer;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.Consents;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequestAsyncUpdateInfo;
import com.here.platform.cm.rest.model.ConsentRequestAsyncUpdateInfo.StatusEnum;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps2;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.CMFeatures.BMW;
import com.here.platform.common.config.Conf;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.dataProviders.reference.controllers.ReferenceProviderController;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import io.qameta.allure.Issue;
import io.qameta.allure.Issues;
import io.restassured.response.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


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
