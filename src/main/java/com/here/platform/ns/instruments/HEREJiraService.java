package com.here.platform.ns.instruments;

import static com.here.platform.common.strings.SBB.sbb;
import static lv.ctco.zephyr.enums.ConfigProperty.PROJECT_KEY;
import static lv.ctco.zephyr.util.HttpUtils.ensureResponse;
import static lv.ctco.zephyr.util.HttpUtils.getAndReturnBody;
import static lv.ctco.zephyr.util.HttpUtils.post;
import static lv.ctco.zephyr.util.Utils.log;
import static lv.ctco.zephyr.util.Utils.readInputStream;

import java.io.IOException;
import java.util.List;
import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.beans.Metafield;
import lv.ctco.zephyr.beans.TestCase;
import lv.ctco.zephyr.beans.jira.Issue;
import lv.ctco.zephyr.beans.jira.SearchResponse;
import lv.ctco.zephyr.transformer.TestCaseToIssueTransformer;
import lv.ctco.zephyr.util.ObjectTransformer;
import org.apache.http.HttpResponse;


public class HEREJiraService {

    private Config config;

    public HEREJiraService(Config config) {
        this.config = config;
    }

    public List<Issue> getTestIssues() throws IOException {
        log("Fetching JIRA Test issues for the project");
        var searchQuery = sbb("project=").sQuoted(config.getValue(PROJECT_KEY)).append("%20and%20issueType=Test").bld();
        SearchResponse searchResults = searchInJQL(searchQuery);

        List<Issue> issues = searchResults.getIssues();

        log(sbb("Retrieved").w().sQuoted(issues.size()).w().append("Test issues").n().bld());
        return issues;
    }

    SearchResponse searchInJQL(String searchQuery) throws IOException {
        String response = getAndReturnBody(config,
                sbb("api/2/search?jql=").append(searchQuery).append("&maxResults=1000").bld());
        return ObjectTransformer.deserialize(response, SearchResponse.class);
    }

    public void createTestIssue(TestCase testCase) throws IOException {
        log(sbb("INFO: Creating JIRA Test:").w().sQuoted(testCase.getName()).bld());
        Issue issue = TestCaseToIssueTransformer.transform(config, testCase);

        //clean up broken fields for updated ZAPTi and Jira
        issue.getFields().setPriority(null);
        issue.getFields().setVersions(null);

        HttpResponse response = post(config, "api/2/issue", issue);
        ensureResponse(response, 201, "ERROR: Could not create JIRA Test item");

        String responseBody = readInputStream(response.getEntity().getContent());
        Metafield result = ObjectTransformer.deserialize(responseBody, Metafield.class);
        if (result != null) {
            testCase.setId(Integer.valueOf(result.getId()));
            testCase.setKey(result.getKey());
        }
        log(sbb("INFO: Created. JIRA Test item Id is:").w().append("https://saeljira.it.here.com/browse/")
                .append(testCase.getKey()).bld());
    }

}