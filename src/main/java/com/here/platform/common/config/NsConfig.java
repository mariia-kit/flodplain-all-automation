package com.here.platform.common.config;

import static com.here.platform.common.strings.SBB.sbb;

import com.here.platform.common.config.dto.DaimlerApp;
import lombok.Data;


@Data
@YamlConfUrl(propertyName = "CREDENTIAL_FILE_NS")
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

    public String getReferenceProviderAuthUrl() {
        return sbb(getRefProviderUrl()).append("/auth/oauth/v2/authorize").bld();
    }

    public String getReferenceProviderTokenUrl() {
        return sbb(getRefProviderUrl()).append("/auth/oauth/v2/token").bld();
    }

}
