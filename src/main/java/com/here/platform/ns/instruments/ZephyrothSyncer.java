package com.here.platform.ns.instruments;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.SneakyThrows;
import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.Runner.CliConfigLoader;
import lv.ctco.zephyr.beans.TestCase;
import lv.ctco.zephyr.beans.jira.Issue;
import lv.ctco.zephyr.service.AuthService;
import lv.ctco.zephyr.service.TestCaseResolutionService;
import lv.ctco.zephyr.service.ZephyrService;


public class ZephyrothSyncer {
    public static void main(String[] args) throws Exception {
        Config config = new Config(new CliConfigLoader(args));
        execute(config);
    }

    @SneakyThrows
    public static void execute(Config config) {
        AuthService authService = new AuthService(config);
        TestCaseResolutionService testCaseResolutionService = new TestCaseResolutionService(config);
        HEREJiraService hereJiraService = new HEREJiraService(config);
        ZephyrService zephyrService = new ZephyrService(config);

        authService.authenticateInJira();

        List<TestCase> testCasesFromTestRun = testCaseResolutionService.resolveTestCases();
        List<Issue> actualTestCasesInZephyr = hereJiraService.getTestIssues();

        Map<String, String> issuesMapKeyToSummary = ZephyrothLinker.getIssuesMapKeyToSummary(actualTestCasesInZephyr);

        for (TestCase testCase : testCasesFromTestRun) {
            var issueId = issuesMapKeyToSummary.entrySet().stream()
                    .filter(entry -> testCase.getName().equals(entry.getValue()))
                    .map(Entry::getKey)
                    .findFirst().get();

            testCase.setKey(issueId);
        }
        zephyrService.updateExecutionStatuses(testCasesFromTestRun);
    }
}
