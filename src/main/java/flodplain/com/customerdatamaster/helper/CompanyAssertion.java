package flodplain.com.customerdatamaster.helper;

import flodplain.com.customerdatamaster.dto.Company;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.Getter;
import org.assertj.core.api.Assertions;


public class CompanyAssertion {

    @Getter
    private final Response response;

    public CompanyAssertion(Response response) {
        this.response = response;
    }

    @Step("Expected response code equals to '{responseCode}'")
    public CompanyAssertion expectedCode(int responseCode) {
        Assertions.assertThat(response.getStatusCode())
                .withFailMessage("Response code not as expected! Expected "+ responseCode + " but found " + response.getStatusCode())
                .isEqualTo(responseCode);
        return this;
    }

    @Step("Expected response value equals to Company: '{expected.name}'")
    public CompanyAssertion expectedEqualsCompany(Company expected) {
        var actual = response.getBody().as(Company.class);
        Assertions.assertThat(actual).isEqualTo(expected);
        return this;
    }
}
