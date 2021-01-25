package com.here.platform.common.allureSync;

import com.here.platform.common.strings.JConvert;
import io.restassured.response.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;


public class AllureReportExport {

    private static String pattern_allure_results_directory = "build/allure-results/";

    @SneakyThrows
    public static void main(String[] args) {
        String env = System.getProperty("env");
        System.out.println("Current env: " + env);
        System.out.println("Current ci: " + System.getProperty("ci"));
        String project_id = System.getProperty("allure_project");
        System.out.println("Current allure proj: " + project_id);
        System.out.println("Current CI: " + System.getenv("CI"));
        boolean isCi = !StringUtils.isEmpty(System.getenv("CI"));

        if (!StringUtils.isEmpty(project_id) && isCi) {
            AllureSyncController allureSyncController = new AllureSyncController();

            File files = new File(pattern_allure_results_directory);

            List<ReportFileRecord> report = Arrays.stream(files.listFiles())
                    .map(f -> {
                        try {
                            return new ReportFileRecord(f.getName(), Base64.getEncoder().encodeToString(Files.readAllBytes(f.toPath())));
                        } catch (IOException e) {
                            e.printStackTrace();
                            return new ReportFileRecord(f.getName(), StringUtils.EMPTY);
                        }
                    }).collect(Collectors.toList());

            int caret = 0;
            System.out.println("Going to report " + report.size() + " files.");
            while(caret <= report.size()) {
                int nextStep =  caret + 100 > report.size() ? report.size() : caret + 100;
                String body = new JConvert(new ReportFileContainer(report.subList(caret, nextStep))).toJson();
                Response resp = allureSyncController.uploadReportData(project_id, body);
                if (resp.getStatusCode() != HttpStatus.SC_OK) {
                    System.out.println("Error uploading report:" + resp.getStatusCode());
                    System.exit(resp.getStatusCode());
                }
                caret = caret + 100;
                if (caret%500 == 0) {
                    System.out.println("Proceed " + caret + " files for report.");
                }
                Thread.sleep(500);
            }

            allureSyncController.initReportGeneration(project_id);
        }
    }
}
