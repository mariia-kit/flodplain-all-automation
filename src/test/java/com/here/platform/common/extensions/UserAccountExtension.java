package com.here.platform.common.extensions;

import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.rest.model.ConsumerData;
import com.here.platform.cm.rest.model.UserAccountData;
import com.here.platform.dataProviders.daimler.DataSubjects;
import io.qameta.allure.Step;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


@Builder
public class UserAccountExtension implements BeforeEachCallback, AfterEachCallback {

    private final DataSubjects targetDataSubject;
    private final List<String> additionalVINsToRemove = new ArrayList<>();

    @Override
    public void beforeEach(ExtensionContext context) {
        dataSubjectCleanUp();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        dataSubjectCleanUp();
    }

    @Step("Remove all VINs and data consumers for the user")
    private void dataSubjectCleanUp() {
        var userAccountController = new UserAccountController();
        var cmToken = targetDataSubject.generateBearerToken();

        userAccountController.deleteVINForUser(targetDataSubject.vin, cmToken);

        var userAccountInfo = userAccountController
                .userAccountGetInfo(cmToken)
                .as(UserAccountData.class);
        System.out.println(userAccountInfo);

        for (ConsumerData consumer : userAccountInfo.getConsumersData()) {
            userAccountController.deleteConsumerForUser(consumer.getConsumerId(), cmToken);
        }

        for (String vinToRemove : additionalVINsToRemove) {
            userAccountController.deleteVINForUser(vinToRemove, cmToken);
        }
    }


}
