package flodplain.com.common.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.yaml.snakeyaml.Yaml;


public class ConfigLoader {

    public static <T> T yamlLoadConfig(String filePath, Class<T> configuredClass) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException(String.format("File '%s' not exist", file.getPath()));
        }
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return yaml.loadAs(in, configuredClass);
        } catch (IOException ex) {
            throw new RuntimeException("Error while reading yaml config file " + file.getPath(), ex);
        }
    }

}
