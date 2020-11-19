package com.here.platform.common.extensions;

import com.github.javafaker.Faker;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.common.DataSubject;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.hereAccount.controllers.HereUserManagerController;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


public class FakeHEREAccountSetupExtension implements BeforeEachCallback, AfterEachCallback {

    private final UserAccountController userAccountController = new UserAccountController();
    private final HereUserManagerController hereUserManagerController = new HereUserManagerController();
    private HereUser fakeHEREUSer = null;
    private DataSubject fakeDataSubject;

    /**
     * Init data subject with HERE user account credentials And set specific VIN for DataSubject
     */
    public DataSubject getFakeDataSubjectWithVIN(String targetVIN) {
        this.fakeDataSubject = new DataSubject();
        this.fakeDataSubject.setEmail(this.fakeHEREUSer.getEmail());
        this.fakeDataSubject.setPass(this.fakeHEREUSer.getPassword());
        this.fakeDataSubject.setVin(targetVIN);
        return fakeDataSubject;
    }

    /**
     * Init data subject with HERE user account credentials And generate VIN with specific length for DataSubject
     */
    public DataSubject getFakeDataSubjectWithVINLength(int vinLength) {
        return getFakeDataSubjectWithVIN(VIN.generate(vinLength));
    }

    /**
     * Init data subject with HERE user account credentials And generate VIN with Data Provider length specific for
     * DataSubject
     */
    public DataSubject getFakeDataSubjectPerProvider(MPProviders targetProvider) {
        return getFakeDataSubjectWithVINLength(targetProvider.vinLength);
    }

    private void registerHEREUserAccount() {
        var faker = new Faker();

        this.fakeHEREUSer = new HereUser(faker.internet().emailAddress(), faker.internet().password(), "here");

        this.hereUserManagerController.createHereUser(fakeHEREUSer);
    }

    private void deleteHEREUserAccount() {
        this.userAccountController.deleteVINForUser(fakeDataSubject.getVin(), getCMBearerTokenForFakeUser());
        if (this.fakeHEREUSer != null) {
            this.hereUserManagerController.deleteHereUser(fakeHEREUSer);
        }
    }

    private String getCMBearerTokenForFakeUser() {
        return DataSubjects.getBearerToken(fakeDataSubject);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        deleteHEREUserAccount();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        registerHEREUserAccount();
    }

}
