package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.open;

import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.pages.BaseCMPage.Header;
import com.here.platform.cm.pages.DashBoardPage;
import com.here.platform.cm.pages.LandingPage;
import com.here.platform.cm.pages.UserProfilePage;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.annotations.CMFeatures.UserAccount;
import com.here.platform.common.strings.VIN;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("CM-UserAccount")
@Disabled
@Feature("Profile info page")
@DisplayName("[UI] User Profile Tests")
public class UserProfileTests extends BaseUITests{

    @Test
    @Issue("NS-2744")
    @Feature("Profile info page")
    @DisplayName("Open Manage Account page on Profile info and verify Here Account page")
    void clickManageAccountAndVerifyHereAccountPage(){
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        UserAccountSteps.attachDataSubjectVINToUserAccount(dataSubjectIm);
        open(ConsentPageUrl.getEnvUrlRoot());
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginRegisteredDataSubject(dataSubjectIm);

        new Header().openDashboardUserAvatarTab();

        new UserProfilePage()
                .clickProfileInfo();

        new UserProfilePage()
                .clickManageAccount()
                .verifyHEREAccountLink();
    }

    @Test
    @Issue("NS-2744")
    @Feature("Profile info page")
    @DisplayName("Open Profile info page and verify user data")
    void clickProfileInfoAndVerifyUserData() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        UserAccountSteps.attachDataSubjectVINToUserAccount(dataSubjectIm);
        open(ConsentPageUrl.getEnvUrlRoot());
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginRegisteredDataSubject(dataSubjectIm);

        new DashBoardPage().isLoaded();
        new Header().openDashboardUserAvatarTab();

        new UserProfilePage()
                .clickProfileInfo();

        new UserProfilePage()
                .verifyUserProfileData(dataSubjectIm);
    }

    @Test
    @Issue("NS-2744")
    @Feature("Profile info page")
    @DisplayName("Open Profile info page and verify user vin details")
    void clickProfileInfoAndVerifyVinData() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        UserAccountSteps.attachDataSubjectVINToUserAccount(dataSubjectIm);
        open(ConsentPageUrl.getEnvUrlRoot());
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginRegisteredDataSubject(dataSubjectIm);

        new DashBoardPage().isLoaded();
        new Header().openDashboardUserAvatarTab();

        new UserProfilePage()
                .clickProfileInfo();

        new UserProfilePage()
                .verifyUserProfileVinDetails(dataSubjectIm.getVin());
    }

    @Test
    @Issue("NS-2745")
    @Feature("Profile info page")
    @DisplayName("Add vin to registered user on the 'Profile info' page")
    void addVinToNewUserFromProfilePageTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        open(ConsentPageUrl.getEnvUrlRoot());
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);

        new VINEnteringPage().isLoaded();

        new Header().openDashboardUserAvatarTab();
        new UserProfilePage()
                .isLoaded()
                .clickProfileInfo();

        new UserProfilePage()
                .verifyNoVehiclesText();

        new UserProfilePage().clickAddNewVehicle();

        new VINEnteringPage()
                .isLoaded()
                .fillVINAndContinue(dataSubjectIm.getVin());

        new UserProfilePage()
                .verifyUserProfileVinDetails(dataSubjectIm.getVin());

    }

    @Test
    @Issue("NS-2745")
    @Feature("Profile info page")
    @DisplayName("Add the second the same vin on Profile info page")
    void addSecondTheSameVinTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        UserAccountSteps.attachDataSubjectVINToUserAccount(dataSubjectIm);
        open(ConsentPageUrl.getEnvUrlRoot());
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginRegisteredDataSubject(dataSubjectIm);

        new DashBoardPage().isLoaded();
        new Header().openDashboardUserAvatarTab();

        new UserProfilePage()
                .clickProfileInfo();

        new UserProfilePage()
                .clickAddNewVehicle();

        new VINEnteringPage().verifyUsedVinError(dataSubjectIm.vin);

    }

    @Test
    @Issue("NS-3262")
    @Feature("Profile info page")
    @DisplayName("Cancel removing of added vehicle and Delete vehicle")
    void verifyCancelButtonAndDeleteVinTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        open(ConsentPageUrl.getEnvUrlRoot());
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);

        new VINEnteringPage().isLoaded();

        new Header().openDashboardUserAvatarTab();
        new UserProfilePage()
                .isLoaded()
                .clickProfileInfo();

        new UserProfilePage()
                .clickAddNewVehicle();

        new VINEnteringPage()
                .isLoaded()
                .fillVINAndContinue(dataSubjectIm.getVin());

        new UserProfilePage()
                .verifyUserProfileVinDetails(dataSubjectIm.getVin());

        new UserProfilePage()
                .clickDeleteVehicle()
                .verifyDeletePopup();

        new UserProfilePage()
                .clickCancelOnDeleteVehicle()
                .verifyUserProfileVinDetails(dataSubjectIm.getVin());

        new UserProfilePage()
                .clickDeleteVehicle()
                .clickConfirmDelete()
                .verifyNoVehiclesText();

    }

    @Test
    @Issue("NS-3262")
    @Feature("Profile info page")
    @DisplayName("Delete added vin and verify if there is no vin after sign out")
    void verifyDeletedVinIsNotShownAfterSignOutTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        UserAccountSteps.attachDataSubjectVINToUserAccount(dataSubjectIm);
        open(ConsentPageUrl.getEnvUrlRoot());
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginRegisteredDataSubject(dataSubjectIm);

        new DashBoardPage().isLoaded();
        new Header().openDashboardUserAvatarTab();

        new UserProfilePage()
                .clickProfileInfo();

        new UserProfilePage()
                .clickDeleteVehicle()
                .clickConfirmDelete()
                .verifyNoVehiclesText();

        new Header().openDashboardUserAvatarTab();

        new UserProfilePage().clickOnSignOut();

        new LandingPage().clickSignIn();

        new Header().openDashboardUserAvatarTab();

        new UserProfilePage().clickProfileInfo();

        new UserProfilePage().verifyNoVehiclesText();
    }

    @Test
    @Issue("NS-3506")
    @Feature("VIN Page")
    @Feature("Profile info page")
    @DisplayName("Verify Profile all vins are removed")
    void verifyVinPageWithRemovedAllVin() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());

        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(dataSubjectIm.getVin())
                .getId();

        open(crid);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);
        new VINEnteringPage().isLoaded().verifyIsMandatory().fillVINAndContinue(dataSubjectIm.getVin());

        new Header().openDashboardUserAvatarTab();
        UserProfilePage userProfilePage = new UserProfilePage();
        userProfilePage.clickProfileInfo();
        userProfilePage.clickDeleteVehicle(new VIN(dataSubjectIm.getVin()).label());
        userProfilePage.clickConfirmDelete();
        new Header().openDashboardAcceptedTab();
        new VINEnteringPage()
                .isLoaded()
                .verifyIsFree()
                .fillVINAndContinue(VIN.generate(17));
    }

}
