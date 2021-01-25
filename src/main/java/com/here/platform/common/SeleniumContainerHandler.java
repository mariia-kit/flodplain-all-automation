package com.here.platform.common;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;


public class SeleniumContainerHandler {
    private static Map<String, String> containerMap = new HashMap<>();


    public static void add(String testName, String containerName) {
        containerMap.put(testName, containerName);
    }

    @SneakyThrows
    public static String get(String testName) {
        if (containerMap.containsKey(testName)) {
            return containerMap.get(testName);
        }
        String basePath = "build/tmp";
        for (int i=0; i<=29; i++) {
            File marker = new File(basePath + "/selenium" + i + ".txt");
            if (marker.exists()) {
                continue;
            } else {
                marker.createNewFile();
                add(testName, "selenium" + i);
                return "selenium" + i;
            }
        }
        throw new RuntimeException("No free selenium containers detected!");
    }

    public static void close(String testName) {
        String containerName = containerMap.get(testName);
        String basePath = "build/tmp";
        File marker = new File(basePath + "/" + containerName + ".txt");
        marker.delete();
        containerMap.remove(testName);
    }
}

