package com.here.platform.common.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.yaml.snakeyaml.Yaml;


public class ConfigLoader {

    private static final String RES_FOLDER = "src/main/resources/";

    public static <T> T yamlLoadConfig(String configPath, Class<T> configuredClass) {
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(new File(RES_FOLDER + configPath).toPath())) {
            return yaml.loadAs(in, configuredClass);
        } catch (IOException ex) {
            throw new RuntimeException("Error reading yml config " + configPath, ex);
        }
    }

    public static boolean isConfigExist(String configPath) {
        return new File(RES_FOLDER + configPath).exists();
    }

}
