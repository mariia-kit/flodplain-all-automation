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
    @Tag("CM-Consumer")
    public @interface OnBoardConsumer {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("On-boarding")
    @DisplayName("On-board data provider")
    @Tag("CM-Provider")
    public @interface OnBoardProvider {

    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Create consent request")
    @Tag("CM-Consent")
    public @interface CreateConsentRequest {

    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Approve consent")
    @Tag("CM-Consent Request")
    public @interface ApproveConsent {

    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Revoke consent")
    @Tag("CM-Consent Request")
    public @interface RevokeConsent {

    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Get consent request status")
    @DisplayName("Get consent request status")
    @Tag("CM-Consent Request")
    public @interface GetConsentRequestStatus {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Update consent request")
    @DisplayName("Update consent request")
    @Tag("CM-Consent")
    public @interface UpdateConsentRequest {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Get access token")
    @DisplayName("Getting of access tokens for consents")
    @Tag("CM-Access Token")
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
    @Tag("CM-User Account")
    @Issue("OLPPORT-2678")
    public @interface UserAccount {

    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("BMW")
    @Tag("CM-BMW Event Service")
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


    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ZephyrComponent {
        public String value() default "";
    }
}
