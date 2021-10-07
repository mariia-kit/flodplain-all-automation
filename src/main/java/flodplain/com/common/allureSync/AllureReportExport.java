package flodplain.com.common.allureSync;

import flodplains.com.common.strings.JConvert;
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
        String project_id = System.getProperty("allure_project");
        String executionName = System.getenv("CI_PIPELINE_ID");
        System.out.println("Current allure proj: " + project_id);
        boolean isCi = !StringUtils.isEmpty(System.getenv("CI"));

        if (!StringUtils.isEmpty(project_id) && isCi) {
            AllureSyncController allureSyncController = new AllureSyncController();
            Response removeFirst = allureSyncController.clearReportData(project_id);
            System.out.println("Remove old data:" + removeFirst.getStatusCode());

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
                Response resp = allureSyncController.uploadReportData(project_id, body, executionName);
                if (resp.getStatusCode() != HttpStatus.SC_OK) {
                    System.out.println("Error uploading report:" + resp.getStatusCode());
                    System.out.println("Error uploading report http response status:" + resp.getStatusCode());
                    System.out.println("Error uploading report http response body:");
                    resp.prettyPrint();
                    System.exit(resp.getStatusCode());
                }
                caret = caret + 100;
                if (caret%500 == 0) {
                    System.out.println("Proceed " + caret + " files for report.");
                }
                Thread.sleep(500);
            }
            System.out.println("Init report render for:" + report.size() + " files.");
            Response res = allureSyncController.initReportGeneration(project_id, executionName);
            System.out.println("Report render complete with code:" + res.getStatusCode());
            System.out.println("Report render complete with response code:" + res.getStatusCode());
            System.out.println("Report render complete with response body:");
            res.prettyPrint();
            if (res.getStatusCode() == 200) {
                System.out.println("Report URL: " + res.getBody().jsonPath().getString("data.report_url"));
            }
            Thread.sleep(5000);
            Response remove = allureSyncController.clearReportData(project_id);
            System.out.println("Remove old data:" + remove.getStatusCode());
        }
    }
}

