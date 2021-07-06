package com.here.platform.proxy.conrollers;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.helpers.UniqueId;


public enum AwsS3Providers {

    AWS(new AwsS3Provider(
            "AWS",
            "AWS_S3", Conf.mpUsers().getMpProvider().getRealm(), "s3extproxytest"));

    private AwsS3Provider awsS3Provider;

    AwsS3Providers(AwsS3Provider withAuthMethod) {
    }

    public static String getAWSProviderNamePrefix() {
        return "Auto-AWS-Provider";
    }

    public static String getProviderType(){ return "AWS"; }

    public static AwsS3Provider generateAwsProvider() {
        String awsId = UniqueId.getUniqueKey();
        return new AwsS3Provider(
                "AWS",
                getAWSProviderNamePrefix() + awsId,
                Conf.mpUsers().getMpProvider().getRealm(),
                "s3extproxytest" + awsId);
    }

    public static AwsS3Provider generateAwsProviderNoProviderType() {
        String awsId = UniqueId.getUniqueKey();
        return new AwsS3Provider(
                getAWSProviderNamePrefix() + awsId,
                Conf.mpUsers().getMpProvider().getRealm(),
                "s3extproxytest" + awsId);
    }

    public static AwsS3Provider generateAwsProvider(String providerType) {
        String awsId = UniqueId.getUniqueKey();
        return new AwsS3Provider(
                providerType,
                getAWSProviderNamePrefix() + awsId,
                Conf.mpUsers().getMpProvider().getRealm(),
                "s3extproxytest" + awsId);
    }
}