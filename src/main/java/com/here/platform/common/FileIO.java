package com.here.platform.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;


public interface FileIO {

    String basePath = "build/tmp";

    @SneakyThrows
    static void writeStringToFile(File targetFile, String stringValue) {
        new File(basePath).mkdir();
        var fileWriter = new FileWriter(targetFile);
        fileWriter.write(stringValue);
        fileWriter.close();
    }

    @SneakyThrows
    static String writeStringToLockedFile(File targetFile, Supplier<String> valueProducer) {
        String token;
        new File(basePath).mkdir();
        try (RandomAccessFile reader = new RandomAccessFile(targetFile, "rw");
                FileLock lock = reader.getChannel().lock()) {

            token = valueProducer.get();
            reader.write(token.getBytes());
            lock.release();
        }
        return token;
    }

    static String readFile(File targetFile) {
        StringBuilder resultString = new StringBuilder();

        try (RandomAccessFile channel = new RandomAccessFile(targetFile, "rw")) {
            FileLock lock = channel.getChannel().tryLock();
            int count = 10;
            while (lock == null && count > 1) {
                Thread.sleep(1000);
                count--;
                lock = channel.getChannel().tryLock();
            }
            if (lock == null) {
                throw new RuntimeException("Wait for file un lock take too long:" + targetFile.getName());
            }
            lock.release();
        } catch (IOException|InterruptedException ex) {
            System.err.format("Error waiting for unlock file: %s%n", ex);
        }
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
