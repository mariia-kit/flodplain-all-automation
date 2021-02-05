package com.here.platform.cm.pages;

import static com.codeborne.selenide.Selectors.byCssSelector;
import static com.codeborne.selenide.Selectors.byId;
import static com.codeborne.selenide.Selectors.byLinkText;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selectors.byTitle;
import static com.codeborne.selenide.Selectors.byXpath;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;
import static com.codeborne.selenide.Selenide.switchTo;

import com.codeborne.selenide.Condition;
import com.here.account.oauth2.HereAccount;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.common.DataSubject;
import com.here.platform.common.config.Conf;
import com.here.platform.common.strings.VIN;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import com.here.platform.hereAccount.ui.HereLoginPage;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.UserType_NS;
import com.here.platform.ns.dto.Users;
import io.qameta.allure.Step;
import org.openqa.selenium.By;


public class UserProfilePage  extends BaseCMPage {

    public UserProfilePage isLoaded() {
        $("app-sidebar").
                waitUntil(Condition.visible.because("User Profile side bar not detected!"), 5000);
        return this;
    }

    @Step("Click on the 'Sign out' button on the Avatar tab")
    public DashBoardPage clickOnSignOut() {
        $(byText("Sign out")).click();
        return new DashBoardPage();
    }

    @Step("Click on the 'Profile info' button on the Avatar tab")
    public DashBoardPage clickProfileInfo() {
        $(".lui-h3-subdued").click();
        return new DashBoardPage();
    }

    @Step("Click on the 'Manage Account' button on the User Profile tab")
    public UserProfilePage clickManageAccount() {
        $(byText("Manage account")).click();
        return new UserProfilePage();
    }

    @Step("Verify HERE Account page")
    public void verifyHEREAccountLink() {
        String userAcc = Conf.ns().getPortalUrl() + "/";
        $(byTitle("Manage account"))
                .shouldHave(Condition.attribute("href", userAcc));
        switchTo().window(1);
        $("#basicInformation")
                .shouldBe(Condition.appear);
        $("#basicInformation div.logo")
                .shouldBe(Condition.visible);
        $("#basicInformation h2:nth-child(2)")
                .shouldHave(Condition.text("Personal info"));
        $("#button-basic-information-save")
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Save"));
        $("#button-deactivate-account")
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Deactivate account"));
        $("#button-signout")
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Sign out"));
    }

    @Step("Verify user profile data")
    public void verifyUserProfileData(DataSubject dataSubject) {
        $(".info-block dd:nth-of-type(2)")
                .shouldHave(Condition.exactText(dataSubject.getEmail()));
    }

    @Step("Verify user has no vehicles")
    public void verifyNoVehiclesText() {
        $(byText("Add new vehicle"))
                .shouldBe(Condition.visible);
        $("body  app-root  lui-default-theme  div  app-user-profile  section  article  p")
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("You have no vehicles added to this account"));
    }

    @Step("Verify user profile vehicles details")
    public void verifyUserProfileVinDetails(String vinNumber) {
        $(".vin-code")
                .shouldHave(Condition.text(new VIN(vinNumber).label()));
    }

    @Step("Click on the 'Delete vehicle' icon on the User Profile tab")
    public UserProfilePage clickDeleteVehicle() {
        $("body app-root lui-default-theme div app-user-profile section article lui-table table tbody tr td:nth-child(5)").click();
        return new UserProfilePage();
    }

    @Step("Click on the 'Delete vehicle' icon on the User Profile tab for vin {vinLabel}")
    public UserProfilePage clickDeleteVehicle(String vinLabel) {
        //*********F0A18910
        $(By.xpath("//tr[//td/span[contains(text(),'*********" + vinLabel + "')]]/td[5]")).click();
        return new UserProfilePage();
    }

    @Step("Click on the 'Add new vehicle' button on the User Profile tab")
    public UserProfilePage clickAddNewVehicle() {
        $(byText("Add new vehicle")).click();
        return new UserProfilePage();
    }

    @Step("Verify if Delete vehicle popup is presented")
    public void verifyDeletePopup() {
        $("#modal-notification-negative p:nth-child(1)")
                .should(Condition.appear)
                .shouldHave(Condition.text("Delete vehicle?"));
    }

    @Step("Click on the 'Delete' button on the 'Delete vehicle' window")
    public UserProfilePage clickConfirmDelete() {
        $("#modal-notification-negative").find(byText("Delete")) .click();
        return new UserProfilePage();
    }

    @Step("Click on the 'Cancel' button on the 'Delete vehicle' window")
    public UserProfilePage clickCancelOnDeleteVehicle() {
        $("#modal-notification-negative").find(byText("Cancel")).click();
        return new UserProfilePage();
    }

}
