package com.here.platform.common;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.jupiter.params.ParameterizedTest;


public class TestResultLoggerExtension implements TestWatcher {

    private final String
            projectKey = "NS",
            correspondingTestId = "customfield_13980",
            componentName = "Neutral Server";
    private final Long testType = 138L;
    private JiraRestClient restClient;

    private JiraRestClient getJiraRestClient() {
        if (isNotBlank(System.getProperty("jira_sync")) && restClient == null) {
            String login = System.getProperty("jira_login");
            String pass = System.getProperty("jira_pass");
            if (!StringUtils.isEmpty(login) && !StringUtils.isEmpty(pass)) {
                restClient = new AsynchronousJiraRestClientFactory()
                        .createWithBasicHttpAuthentication(URI.create("https://saeljira.it.here.com"), login, pass);
                return restClient;
            } else {
                return null;
            }
        } else {
            return restClient;
        }
    }

    private TestResult getAllureTest(ExtensionContext context, boolean isParametrised) {
        List<TestResult> resList = new LinkedList<>();
        Allure.getLifecycle().getCurrentTestCase().orElseThrow(() -> new RuntimeException(
                "Allure context not detected for current test: " + context.getDisplayName()));


        Allure.getLifecycle().updateTestCase(resList::add);

        if (isParametrised && context.getDisplayName().contains("[")) {
            resList.get(0).setName(context.getParent().get()
                    .getDisplayName() + " " + resList.get(0).getName()
                    .replace("[", "")
                    .replace("]", ""));
        }
        return resList.get(0);
    }

    private String createOrUpdateIssue(String summary,
            String reference,
            String description,
            boolean isParametrised
    ) {
        Issue jiraIssue = searchIssue(summary, reference, isParametrised);
        if (jiraIssue == null) {
            return createIssue(summary, reference, description);
        } else {
            updateIssue(jiraIssue, summary, reference, description);
            return jiraIssue.getKey();
        }
    }

    private String createIssue(String summary,
            String reference,
            String description
    ) {
        IssueRestClient issueClient = restClient.getIssueClient();
        IssueInput newIssue = new IssueInputBuilder(projectKey, testType, summary)
                .setComponentsNames(List.of(componentName))
                .setDescription(description)
                .setFieldValue(correspondingTestId, reference)
                .build();
        return issueClient.createIssue(newIssue).claim().getKey();
    }

    private void updateIssue(Issue issue,
            String summary,
            String reference,
            String description
    ) {
        IssueInput input = new IssueInputBuilder()
                .setSummary(summary)
                .setDescription(description)
                .setFieldValue(correspondingTestId, reference)
                .build();
        restClient.getIssueClient()
                .updateIssue(issue.getKey(), input)
                .claim();
    }

    private String generateDescription(TestResult test, boolean isComplete) {
        return test.getName() + "\r\n" +
                "Suite: " + test.getLabels().stream().filter(l -> l.getName().equals("suite"))
                .map(Label::getValue).findFirst().orElse("") + "\r\n" +
                "{color:#c1c7d0}(jql:" + test.getName().replace(" ", "_") + "){color}\r\n" +
                "(Test Reference:" + test.getFullName() + ")\r\n\r\n" +
                "- " + test.getSteps().stream()
                .map(StepResult::getName)
                .collect(Collectors.joining("\r\n- ")) +
                (isComplete ? StringUtils.EMPTY : "- not all steps present due failed test.\r\n");
    }

    private String generateDisabledDescription(String summary,
            String reference,
            String reason
    ) {
        return summary + "\r\n" +
                "{color:#c1c7d0}(jql:" + summary.replace(" ", "_") + "){color}\r\n" +
                "(Test Reference:" + reference + ")\r\n\r\n" +
                "Currently disabled: " + reason;
    }

    private Issue searchIssue(String name, String reference, boolean isParametrised) {
        //First search by marker in description, cause most accurate.
        //than - by reference field, accurate but not for parametrised tests.
        //last by name - not accurate, but can show that issue not exist at all.
        Issue jiraIssue = searchIssueByDesctiption(name.replace(" ", "_"));
        if (jiraIssue == null && !isParametrised) {
            jiraIssue = searchIssueByReference(reference);
        }
        if (jiraIssue == null && !isParametrised) {
            jiraIssue = searchIssueBySummary(name);
        }
        return jiraIssue;
    }

    private Issue searchIssueBySummary(String query) {
        return searchIssue("' AND summary ~ '", query);
    }

    private Issue searchIssueByDesctiption(String query) {
        return searchIssue("' AND description ~ '", query);
    }

    private Issue searchIssueByReference(String query) {
        return searchIssue("' AND 'Corresponding Test Case(s)' ~ '", query);
    }

    private Issue searchIssue(String searchType, String query) {
        String finalQuery = query.replace("(", "\\\\(").replace(")", "\\\\)");
        SearchResult res = restClient.getSearchClient()
                .searchJql("issuetype = Test AND project = '" + projectKey + searchType + finalQuery + "'")
                .claim();
        if (res.getTotal() > 0) {
            return res.getIssues().iterator().next();
        } else {
            return null;
        }
    }

    private void linkIssue(String issueKey1, String issueKey2) {
        LinkIssuesInput linkIssuesInput = new LinkIssuesInput(issueKey1, issueKey2,
                "Test");
        restClient.getIssueClient()
                .linkIssue(linkIssuesInput)
                .claim();
    }

    private List<String> getStory(TestResult test) {
        return test.getLabels().stream().filter(l -> l.getName().equals("story"))
                .map(Label::getValue).collect(Collectors.toList());
    }

    private void linkToStory(TestResult test, String issueKey) {
        getStory(test).forEach(storyKey -> linkIssue(issueKey, storyKey));
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        if (getJiraRestClient() != null) {
            boolean isParametrised = context.getTestMethod()
                    .map(method -> method.isAnnotationPresent(ParameterizedTest.class)).orElse(false);
            TestResult currentTest = getAllureTest(context, isParametrised);
            String issueKey = createOrUpdateIssue(currentTest.getName(), currentTest.getFullName(),
                    generateDescription(currentTest, true), isParametrised);
            linkToStory(currentTest, issueKey);
        }
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        if (getJiraRestClient() != null) {
            boolean isParametrised = context.getTestMethod()
                    .map(method -> method.isAnnotationPresent(ParameterizedTest.class)).orElse(false);
            TestResult currentTest = getAllureTest(context, isParametrised);
            String issueKey = createOrUpdateIssue(currentTest.getName(), currentTest.getFullName(),
                    generateDescription(currentTest, false), isParametrised);
            linkToStory(currentTest, issueKey);
        }
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        if (getJiraRestClient() != null) {
            String summary = context.getDisplayName();
            String reference = context.getTestMethod().get().toString()
                    .replace("void ", "");
            boolean isParametrised = context.getTestMethod()
                    .map(method -> method.isAnnotationPresent(ParameterizedTest.class)).orElse(false);
            createOrUpdateIssue(summary, reference,
                    generateDisabledDescription(summary, reference, reason.orElse("Reason Unknown!")), isParametrised);
        }
    }

}
