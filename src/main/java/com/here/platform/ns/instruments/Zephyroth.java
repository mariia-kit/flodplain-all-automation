package com.here.platform.ns.instruments;

import static com.here.platform.common.strings.SBB.sbb;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.Runner.CliConfigLoader;
import lv.ctco.zephyr.beans.TestCase;
import lv.ctco.zephyr.beans.TestStep;
import lv.ctco.zephyr.beans.jira.Issue;
import lv.ctco.zephyr.service.AuthService;
import lv.ctco.zephyr.service.MetaInfo;
import lv.ctco.zephyr.service.MetaInfoRetrievalService;
import lv.ctco.zephyr.service.TestCaseResolutionService;
import lv.ctco.zephyr.service.ZephyrService;


public class Zephyroth {

    public static void main(String[] args) throws Exception {
        Config config = new Config(new CliConfigLoader(args));
        execute(config);
    }


    public static void execute(Config config) throws IOException, InterruptedException {
        AuthService authService = new AuthService(config);
        MetaInfoRetrievalService metaInfoRetrievalService = new MetaInfoRetrievalService(config);
        TestCaseResolutionService testCaseResolutionService = new TestCaseResolutionService(config);
        HEREJiraService hereJiraService = new HEREJiraService(config);
        ZephyrService zephyrService = new ZephyrService(config);

        authService.authenticateInJira();

        List<TestCase> testCasesFromTestRun = testCaseResolutionService.resolveTestCases();
        List<Issue> actualTestCasesInZephyr = hereJiraService.getTestIssues();

        //prepare test cases to creation
        Map<String, String> targetTestCasesToCompare = getIssuesMapKeyToSummary(actualTestCasesInZephyr);
        Set<TestCase> testCasesToCreate = filterTestCasesThatAlreadyCreated(
                testCasesFromTestRun,
                targetTestCasesToCompare
        );

        //create new test cases
        for (TestCase testCase : testCasesToCreate) {
            convertTestCaseStepsToDescription(testCase);
            hereJiraService.createTestIssue(testCase);
        }

        Thread.sleep(30000);

        //todo implement description and summary updating of the test cases

        Map<String, String> issuesMapKeyToSummary = getIssuesMapKeyToSummary(actualTestCasesInZephyr);

        //instead of zephyrService.mapTestCasesToIssues(testCasesFromTestRun, actualTestCasesInZephyr);
        for (TestCase testCase : testCasesFromTestRun) {
            var issueId = issuesMapKeyToSummary.entrySet().stream()
                    .filter(entry -> testCase.getName().equals(entry.getValue()))
                    .map(Entry::getKey)
                    .findFirst().get();

            testCase.setKey(issueId);
            System.out.println(testCase.getKey());
        }

        MetaInfo metaInfo = metaInfoRetrievalService.retrieve();

        zephyrService.linkExecutionsToTestCycle(metaInfo, testCasesFromTestRun);
        zephyrService.updateExecutionStatuses(testCasesFromTestRun);
    }

    private static void convertTestCaseStepsToDescription(TestCase testCase) {
        var listOfStepsFromTestCase = testCase.getSteps().stream()
                .map(TestStep::getDescription)
                .collect(Collectors.toList());

        String testCaseDescriptionWithUpdatedSteps = "";

        //Add step as new bullet item
        for (String testCaseStep : listOfStepsFromTestCase) {
            testCaseDescriptionWithUpdatedSteps = sbb(testCaseDescriptionWithUpdatedSteps)
                    .append("*").w().append(testCaseStep).n().bld();
        }

        testCase.setDescription(testCaseDescriptionWithUpdatedSteps);
    }

    private static Set<TestCase> filterTestCasesThatAlreadyCreated(
            List<TestCase> testCases,
            Map<String, String> issuesSet)
    {
        return testCases.stream()
                .filter(testCase -> !issuesSet.containsValue(testCase.getName()))
                .collect(Collectors.toSet());
    }

    private static Map<String, String> getIssuesMapKeyToSummary(List<Issue> issues) {
        Map<String, String> issuesSet = new HashMap<>(issues.size());
        for (Issue issue : issues) {
            issuesSet.put(issue.getKey(), issue.getFields().getSummary());
        }
        return issuesSet;
    }

}