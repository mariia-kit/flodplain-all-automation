package com.here.platform.ns.utils;

import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.UserType_NS;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


public class PropertiesLoader {

    private final static Logger logger = Logger.getLogger(PropertiesLoader.class);
    public final Properties mainProperties;

    @Getter
    private final static String TOKEN_URL = "build/tmp/tokens.txt";

    private PropertiesLoader() {
        mainProperties = new Properties();
        String env = System.getProperty("env");
        if (StringUtils.isEmpty(env)) {
            env = "dev";
        }
        try (InputStream input = new FileInputStream(
                "src/main/resources/ns-config/" + env + "/config.properties")) {
            mainProperties.load(input);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public static PropertiesLoader getInstance() {
        return LazyHolder.instance;
    }

    public User loadUser(UserType_NS type, String prefix) {
        String fullPrefix = type.getPrefix() + "." + prefix;
        User res = new User(getInstance().mainProperties.getProperty(fullPrefix + ".email"),
                getInstance().mainProperties.getProperty(fullPrefix + ".password"),
                getInstance().mainProperties.getProperty(fullPrefix + ".realm"),
                getInstance().mainProperties.getProperty(fullPrefix + ".user.id"));
        res.setClientId(getInstance().mainProperties.getProperty(fullPrefix + ".clientId"));
        res.setClientSecret(getInstance().mainProperties.getProperty(fullPrefix + ".clientSecret"));
        res.setType(type);
        return res;
    }

    public void saveToken(String userEmail, String token) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(TOKEN_URL, true));
            writer.write(userEmail + "=" + token);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            logger.error("Error saving token for " + userEmail, e);
        }
    }

    public String loadToken(String userEmail) {
        String res = StringUtils.EMPTY;
        try {
            File tokens = new File(TOKEN_URL);
            if (!tokens.exists()) {
                return res;
            }
            Scanner scanner = new Scanner(tokens);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println(line);
                if (line.contains(userEmail)) {
                    res = line.split("=")[1];
                    //break;
                    //not break to read last line also
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            logger.error("Error loading token for " + userEmail, e);
        }
        return res;
    }


    private static class LazyHolder {

        static final PropertiesLoader instance = new PropertiesLoader();

    }

}
