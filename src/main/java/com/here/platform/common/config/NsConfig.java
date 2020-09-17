package com.here.platform.common.config;

import com.here.platform.common.DaimlerApp;
import lombok.Data;


@Data
@YamlConfUrl(configUrl = "{env}/ns.yaml")
public class NsConfig {

    private String nsUrlBaseAccess;
    private String nsUrlBaseProvider;
    private String nsUrlAccess;
    private String nsUrlProvider;

    private String authUrlBase;
    private String authUrlValidate;
    private String authUrlToken;
    private String authUrlAccess;
    private String authUrlGetToken;

    private String portalUrl;
    private String versionPattern;
    private String realm;

    private boolean marketplaceMock;

    private String refProviderName;
    private String refProviderUrl;

    private boolean consentMock;
    private String consentUrl;

    private DaimlerApp daimlerApp;
    private DaimlerApp referenceApp;
    private DaimlerApp bmwApp;

}
