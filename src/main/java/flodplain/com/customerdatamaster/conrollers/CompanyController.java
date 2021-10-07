package flodplain.com.customerdatamaster.conrollers;

import flodplain.com.customerdatamaster.dto.Company;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;


public class CompanyController extends BaseService<CompanyController> {

    private final String basePath = "/api/companies";

    @Step("Add company")
    public Response addCompany(Company company) {
        Response response = serviceClient(basePath)
                .body(company)
                .post();
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            Long id = response.getBody().jsonPath().getLong("id");
        }
        return response;
    }

    @Step("Update company")
    public Response updateCompanyById(Long companyId, Company newCompany) {
        return serviceClient(basePath)
                .body(newCompany)
                .put("/{companyId}", companyId);
    }
}
