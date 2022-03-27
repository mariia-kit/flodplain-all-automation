package flodplain.com.common.config;

import java.io.File;
import java.util.Optional;


public class Conf {

    private static NsConfig nsConf;
    private static NsUserConfig nsUsers;

    public static NsConfig ns() { return getConfig(nsConf, NsConfig.class); }
    public static NsUserConfig nsUsers() { return getConfig(nsUsers, NsUserConfig.class); }


    private static <T> T getConfig(T conf, Class<T> type) {
        if (conf == null) {
            conf = loadConfig(type);
        }
        System.out.println("Conf = load");
        return conf;
    }

    private static <T> T loadConfig(Class<T> type) {
        YamlConfUrl annotation = Optional.ofNullable(type.getAnnotation(YamlConfUrl.class))
                .orElseThrow(() -> new RuntimeException(type.getName() + " not configured"));

        String environment = System.getProperty("env", "dev");
        if ("stg".equalsIgnoreCase(environment)) {
            environment = "sit";
        }
        String basePath = System.getenv("CREDENTIAL_BASE_PATH");
        String fileName = System.getenv(annotation.propertyName());

        String filePath = basePath + File.separator + environment + File.separator + fileName;

        return ConfigLoader.yamlLoadConfig(filePath, type);
    }

}
