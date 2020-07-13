package com.here.platform.ns.instruments;

import static java.lang.String.format;
import static lv.ctco.zephyr.enums.ConfigProperty.PROJECT_KEY;
import static lv.ctco.zephyr.enums.ConfigProperty.RELEASE_VERSION;
import static lv.ctco.zephyr.enums.ConfigProperty.TEST_CYCLE;
import static lv.ctco.zephyr.util.HttpUtils.getAndReturnBody;
import static lv.ctco.zephyr.util.Utils.log;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.Runner.CliConfigLoader;
import lv.ctco.zephyr.beans.TestCase;
import lv.ctco.zephyr.beans.jira.Issue;
import lv.ctco.zephyr.beans.zapi.ExecutionResponse;
import lv.ctco.zephyr.service.AuthService;
import lv.ctco.zephyr.service.JiraService;
import lv.ctco.zephyr.service.MetaInfo;
import lv.ctco.zephyr.service.MetaInfoRetrievalService;
import lv.ctco.zephyr.service.TestCaseResolutionService;
import lv.ctco.zephyr.service.ZephyrService;
import lv.ctco.zephyr.transformer.ReportTransformerFactory;
import lv.ctco.zephyr.util.CustomPropertyNamingStrategy;
import lv.ctco.zephyr.util.ObjectTransformer;
import lv.ctco.zephyr.util.Utils;


public class Zephyroth {

    public static void main(String[] args) throws Exception {
        Utils.log("Supported report types: " + ReportTransformerFactory.getInstance()
                .getSupportedReportTransformers());

        Config config = new Config(new CliConfigLoader(args));
        execute(config);
    }


    public static void execute(Config config) throws IOException, InterruptedException {

        AuthService authService = new AuthService(config);
        MetaInfoRetrievalService metaInfoRetrievalService = new MetaInfoRetrievalService(config);
        TestCaseResolutionService testCaseResolutionService = new TestCaseResolutionService(config);
        JiraService jiraService = new JiraService(config);
        ZephyrService zephyrService = new ZephyrService(config);

        ObjectTransformer.setPropertyNamingStrategy(new CustomPropertyNamingStrategy(config));

        authService.authenticateInJira();

        MetaInfo metaInfo = metaInfoRetrievalService.retrieve();

        List<TestCase> testCases = testCaseResolutionService.resolveTestCases();
        List<Issue> issues = jiraService.getTestIssues();
        zephyrService.mapTestCasesToIssues(testCases, issues);

        log(format("Link %s Test cases to cycle\n", testCases.size()));
        zephyrService.linkExecutionsToTestCycle(metaInfo, testCases);

        int max = 20;
        long uniqKeys = testCases.stream().map(test -> test.getId()).distinct().count();
        while(max > 0) {
            max--;
            int currentIssues = getAllExecutionsCount(config);
            log(format("Test cases linked to Cycle %s\n", currentIssues));
            if (currentIssues >= uniqKeys) {
                break;
            }
            Thread.sleep(60000);
        }

        zephyrService.updateExecutionStatuses(testCases);
    }

    public static int getAllExecutionsCount(Config config) throws IOException {
        log("Fetching JIRA Test Executions for the project");
        int skip = 0;
        String search = "project='" + config.getValue(PROJECT_KEY) + "'%20and%20fixVersion='"
                + URLEncoder.encode(config.getValue(RELEASE_VERSION), "UTF-8") + "'%20and%20cycleName='" + config.getValue(TEST_CYCLE) + "'";

        ExecutionResponse executionResponse = searchInZQL(config, search);
        if (executionResponse == null || executionResponse.getExecutions().isEmpty()) {
            return 0;
        }
        log(format("Retrieved %s Test executions\n", executionResponse.getTotalCount()));
        return executionResponse.getTotalCount();
    }

    private static ExecutionResponse searchInZQL(Config config, String search) throws IOException {
        String response = getAndReturnBody(config, "zapi/latest/zql/executeSearch?zqlQuery=" + search);
        return ObjectTransformer.deserialize(response, ExecutionResponse.class);
    }
}
