package com.here.platform.common.annotations;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;


public class CMFeatures {

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("On-boarding")
    @DisplayName("On-board data consumer")
    public @interface OnBoardConsumer {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("On-boarding")
    @DisplayName("On-board data provider")
    public @interface OnBoardProvider {

    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Create consent request")
    public @interface CreateConsentRequest {

    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Approve consent")
    public @interface ApproveConsent {

    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Revoke consent")
    public @interface RevokeConsent {

    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Get consent request status")
    @DisplayName("Get consent request status")
    public @interface GetConsentRequestStatus {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Update consent request")
    @DisplayName("Update consent request")
    public @interface UpdateConsentRequest {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Get access token")
    @DisplayName("Getting of access tokens for consents")
    public @interface GetAccessToken {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Base for service end-points")
    public @interface BaseService {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("User Account")
    @DisplayName("User Account")
    @Tag("userAccount")
    @Issue("OLPPORT-2678")
    public @interface UserAccount {

    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("BMW")
    public @interface BMW {

    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Epic("ASYNC")
    public @interface ASYNC {

    }

    /**
     * Daimler's requirement
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Purpose")
    public @interface Purpose {

    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Dashboard")
    @DisplayName("Dashboard")
    public @interface Dashboard {

    }

}
