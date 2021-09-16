package com.here.platform.proxy.conrollers;

import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProviderResource;
import io.qameta.allure.Step;
import io.restassured.response.Response;


public class AwsS3Conroller extends BaseProxyService<AwsS3Conroller> {
    private final String basePath = "/aws/s3/buckets";

    @Step("Get AWS S3 proxy: {bucket} {path}")
    public Response getData(String identifier, String path) {
        return consentServiceClient(basePath)
                .get(identifier + "/resources?" + path + "/");
    }

    @Step("Get AWS S3 proxy: {provider.identifier} {resource.path}")
    public Response getData(ProxyProvider provider, ProxyProviderResource resource) {
        return consentServiceClient(basePath)
                .get(provider.getIdentifier() + "/resources?" + resource.getPath()+ "/");
    }
}
