package com.here.platform.ns.helpers;

import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.authentication.AuthController;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


public class TokenManager {

    private final static Logger logger = Logger.getLogger(TokenManager.class);
    @Getter
    private final static String TOKEN_URL = "build/tmp/tokens.txt";

    public static void saveToken(String userEmail, String token) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(TOKEN_URL, true));
            writer.write(userEmail + "=" + token);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            logger.error("Error saving token for " + userEmail, e);
        }
    }

    public static String loadToken(String userEmail) {
        String res = StringUtils.EMPTY;
        try {
            File tokens = new File(TOKEN_URL);
            if (!tokens.exists()) {
                return res;
            }
            Scanner scanner = new Scanner(tokens);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
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

    public static void resetUserLogins() {
        for (Users user : Users.values()) {
            AuthController.deleteToken(user.getUser());
            user.getUser().setToken("");
        }
    }

}
