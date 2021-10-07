package flodplain.com.common.config;

import lombok.Data;


@Data
@YamlConfUrl(propertyName = "CREDENTIAL_FILE_PROXY")
public class FlodplainConfig {
    private String host;
}
