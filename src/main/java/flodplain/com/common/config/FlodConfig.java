package flodplain.com.common.config;

import lombok.Data;


@Data
@YamlConfUrl(propertyName = "CREDENTIAL_FILE_NS")
public class FlodConfig {

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

}
