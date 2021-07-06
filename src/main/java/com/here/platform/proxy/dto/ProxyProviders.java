package com.here.platform.proxy.dto;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.helpers.UniqueId;
import com.here.platform.proxy.dto.ProxyProvider.CredentialsAuthMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum ProxyProviders {

    REFERENCE_PROXY(new ProxyProvider(
            "REST_API",
            "Auto-testing-1",
            Conf.mpUsers().getMpProvider().getRealm(),
            "reference-data-provider.ost.solo-experiments.com",
            CredentialsAuthMethod.NONE)
            .withAuthMethod(CredentialsAuthMethod.API_KEY_IN_QUERY, "Authorization", "1f8647f3-5f86-4b5e-8687-982fd620ef78")),
    ACCUWEATHER(new ProxyProvider(
            "REST_API",
            "Accuweather", Conf.mpUsers().getMpProvider().getRealm(), "api.accuweather.com",
            CredentialsAuthMethod.NONE)
            .withAuthMethod(CredentialsAuthMethod.API_KEY_IN_QUERY, "apikey", Conf.proxy().getAccuApiKey())),
    AWS(new ProxyProvider(
            "AWS",
            "AWS_S3", Conf.mpUsers().getMpProvider().getRealm(), "s3extproxytest",
    CredentialsAuthMethod.NONE)
            .withAuthMethod(CredentialsAuthMethod.API_KEY_IN_QUERY, "apikey", Conf.proxy().getAccuApiKey()));

    private ProxyProvider proxyProvider;

    public static String getProviderNamePrefix() {
        return "Auto-Provider";
    }

    public static String getAWSProviderNamePrefix() {
        return "Auto-AWS-Provider";
    }

    public static String getProviderType(){
        return "REST_API";
    }

    public static ProxyProvider generate() {
        String id = UniqueId.getUniqueKey();
        return new ProxyProvider(
                "REST_API",
                getProviderNamePrefix() + id,
                Conf.mpUsers().getMpProvider().getRealm(),
                "someService." + id + ".mock",
                CredentialsAuthMethod.NONE);
    }
        public static ProxyProvider generateAWS() {
            String awsId = UniqueId.getUniqueKey();
            return new ProxyProvider(
                    "AWS",
                    getAWSProviderNamePrefix() + awsId,
                    Conf.mpUsers().getMpProvider().getRealm(),
                    "s3extproxytest" + awsId,
                    CredentialsAuthMethod.NONE);
    }

    public static ProxyProvider generateAWSNoProviderType() {
        String awsId = UniqueId.getUniqueKey();
        return new ProxyProvider(
                getAWSProviderNamePrefix() + awsId,
                Conf.mpUsers().getMpProvider().getRealm(),
                "s3extproxytest" + awsId,
                CredentialsAuthMethod.NONE);
    }

    public static ProxyProvider generateAWS(String providerType) {
        String awsId = UniqueId.getUniqueKey();
        return new ProxyProvider(
                providerType,
                getAWSProviderNamePrefix() + awsId,
                Conf.mpUsers().getMpProvider().getRealm(),
                "s3extproxytest" + awsId,
                CredentialsAuthMethod.NONE);
    }
}