package com.here.platform.ns.utils;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum NS_Config {
    URL_NS("ns.base.api.url"),
    URL_PROVIDER("provider.ns.base.api.url"),
    SERVICE_ACCESS("ns.serice.url"),
    SERVICE_PROVIDER("provider.ns.service.url"),
    URL_AUTH("base.auth.server.url"),
    VALIDATE_TOKEN_PATH("validate.token.path"),
    VALIDATE_ACCESS_TOKEN("validate.access.token"),
    GET_TOKEN_PATH("get.token.path"),
    GET_PORTAL_PATH("portal.url"),
    URL_EXTERNAL_MARKETPLACE("marketplace.url"),
    URL_EXTERNAL_MARKETPLACE_UI("marketplace.ui"),
    URL_EXTERNAL_CM("consent.url"),
    APP_KEY("consumer.appkeyid"),
    APP_ID("consumer.appclientid"),
    APP_SECRET("consumer.appkeysecret"),
    AAA_SERVICE_ID("aaa.service.id"),
    AAA_ID("aaa.access.key.id"),
    AAA_SECRET("aaa.access.key.secret"),
    VERSION_PATTERN("version.pattern"),
    DAIMLER_API_LOGIN("daimler.user.name"),
    DAIMLER_API_PASS("daimler.user.pass"),
    CM_HERE_LOGIN("cm.user.name"),
    CM_HERE_PASS("cm.user.pass"),
    REFERENCE_PROV_NAME("ref.provider.name"),
    REFERENCE_PROV_URL("ref.provider.url"),
    REFERENCE_J_PROV_NAME("ref.j.provider.name"),
    REFERENCE_J_PROV_URL("ref.j.provider.url"),
    REALM("realm"),
    CONSENT_MOCK("consent.mock"),
    MARKETPLACE_MOCK("marketplace.mock"),
    EXTERNAL_USER_TOKEN("ns.non-consumer-manager.user.token");

    private String key;

    @Override
    public String toString() {
        return PropertiesLoader.getInstance().mainProperties.getProperty(key);
    }

}
