package com.here.platform.common.config;

import lombok.Data;


@Data
@YamlConfUrl(propertyName = "CREDENTIAL_FILE_CM")
public class CmConfig {

    private String serviceUrl;

    private String consentPageUrl;

    private String consentPageUrlDynamic;

    private String qaTestDataMarker;

    private String bmwClearanceSecret;

    private String referenceProviderName;

}
