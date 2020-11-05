package com.here.platform.common.extensions;

import com.here.platform.cm.controllers.HERETokenController;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.common.DataSubject;
import io.qameta.allure.Step;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


@Builder
public class UserAccountExtension implements BeforeEachCallback, AfterEachCallback {

    private final DataSubject targetDataSubject;
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
        var cmToken = new HERETokenController().loginAndGenerateCMToken(targetDataSubject.getEmail(), targetDataSubject.getPass());

        userAccountController.deleteVINForUser(targetDataSubject.getVin(), cmToken);

        for (String vinToRemove : additionalVINsToRemove) {
            userAccountController.deleteVINForUser(vinToRemove, cmToken);
        }
    }


}
