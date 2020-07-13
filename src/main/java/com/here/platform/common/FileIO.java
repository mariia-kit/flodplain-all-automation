package com.here.platform.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.SneakyThrows;


public interface FileIO {

    String basePath = "build/tmp";

    @SneakyThrows
    static void writeStringToFile(File targetFile, String stringValue) {
        new File(basePath).mkdir();
        var fileWriter = new FileWriter(targetFile);
        fileWriter.write(stringValue);
        fileWriter.close();
    }

    static String readFile(File targetFile) {
        StringBuilder resultString = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(targetFile.getAbsolutePath()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                resultString.append(line);
            }
        } catch (IOException ex) {
            System.err.format("IOException: %s%n", ex);
        }

        return resultString.toString();
    }

}
