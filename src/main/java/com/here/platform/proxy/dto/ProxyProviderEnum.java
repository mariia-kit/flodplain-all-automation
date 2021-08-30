package com.here.platform.proxy.dto;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.helpers.UniqueId;
import com.here.platform.proxy.dto.ProxyProvider.AuthMethodPlaceholder;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum ProxyProviderEnum {

    REFERENCE_PROXY(new ProxyProvider(
            "REST_API",
            "Auto-testing-1",
            Conf.mpUsers().getMpProvider().getRealm(),
            "reference-data-provider.ost.solo-experiments.com",
    new Authentication().withAuth()))

/*    ACCUWEATHER(new ProxyProvider(
            "REST_API",
            "Accuweather", Conf.mpUsers().getMpProvider().getRealm(), "api.accuweather.com",
    AuthMethodPlaceholder.NONE)
            .withAuthMethod(CredentialsAuthMethod.API_KEY_IN_QUERY, "apikey", Conf.proxy().getAccuApiKey()))*/,
    AWS(new ProxyProvider(
            "AWS",
            "AWS_S3", Conf.mpUsers().getMpProvider().getRealm(), "s3extproxytest",
            new Authentication().withAuth()));

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
                new Authentication().withAuth());
    }

    public static ProxyProvider generateNullAuth() {
        String id = UniqueId.getUniqueKey();
        return new ProxyProvider(
                "REST_API",
                getProviderNamePrefix() + id,
                Conf.mpUsers().getMpProvider().getRealm(),
                "someService." + id + ".mock",
                new Authentication().withNullAuthMethod());
    }

        public static ProxyProvider generateAWS() {
            String awsId = UniqueId.getUniqueKey();
            return new ProxyProvider(
                    "AWS",
                    getAWSProviderNamePrefix() + awsId,
                    Conf.mpUsers().getMpProvider().getRealm(),
                    "s3extproxytest" + awsId,
                    new Authentication());
    }

   /* public static AwsS3Provider generateAWSNoProviderType() {
       String awsId = UniqueId.getUniqueKey();
        return new ProxyProvider(
               getAWSProviderNamePrefix() + awsId,
                Conf.mpUsers().getMpProvider().getRealm(),
                "s3extproxytest" + awsId,
                new Authentication().withAuthMethod(null, "apikey", "774357"));*/

/*
    public static AwsS3Provider generateAWS(){
        String awsS3Id = UniqueId.getUniqueKey();
        return new AwsS3Provider(
                "AWS",
                getAWSProviderNamePrefix() + awsS3Id,
                Conf.mpUsers().getMpProvider().getRealm(),
                "s3extproxytest" + awsS3Id);*/



}