package com.here.platform.common.annotations;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;


public class CMFeatures {

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("On-board data Consumer")
    public @interface OnBoardConsumer {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("On-board data Provider")
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
    public @interface GetConsentRequestStatus {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Update consent request")
    public @interface UpdateConsentRequest {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Get access token")
    public @interface GetAccessToken {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("Basic for service")
    public @interface BaseService {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Feature("User Account")
    @Tag("userAccount")
    @Issue("OLPPORT-2678")
    public @interface UserAccount {

    }

}
