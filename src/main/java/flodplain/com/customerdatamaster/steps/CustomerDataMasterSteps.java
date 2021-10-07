package flodplain.com.customerdatamaster.steps;

import flodplain.com.customerdatamaster.conrollers.CompanyController;
import flodplain.com.customerdatamaster.dto.Company;
import flodplain.com.customerdatamaster.helper.CompanyAssertion;
import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;
import org.apache.http.HttpStatus;


@UtilityClass
public class CustomerDataMasterSteps {

    @Step("Create company {company.name}")
    public void createCompany(Company company) {
        var response = new CompanyController()
                .withJwtToken("token")
                .addCompany(company);
        new CompanyAssertion(response)
                .expectedCode(HttpStatus.SC_OK);
        Long id = response.getBody().jsonPath().getLong("id");
        company.setSmeId(id);
    }
}
