package com.here.platform.common.config;

import com.here.platform.common.config.dto.HereApplication;
import lombok.Data;


@Data
@YamlConfUrl(propertyName = "CREDENTIAL_FILE_PROXY")
public class ProxyConfig {
    private String host;
    private String accuProtocol;
    private String accuApiKey;
    private Boolean marketplaceMock;

    private HereApplication adminApp;
    private HereApplication proxyApp;
}
