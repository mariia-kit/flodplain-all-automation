package flodplain.com.common.config;

import flodplain.com.customerdatamaster.dto.User;
import lombok.Data;


@Data
@YamlConfUrl(propertyName = "CREDENTIAL_FILE_NS_USER")
public class NsUserConfig {

    //private User adminG2G;

    private User consumer;
    private User provider;

    private User nonConsumerManager;

    private String consumerGroupId;
    private String providerGroupId;
    private String providerPolicyId;

}
