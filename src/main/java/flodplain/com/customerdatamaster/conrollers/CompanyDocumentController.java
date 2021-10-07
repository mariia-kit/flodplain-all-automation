package flodplain.com.customerdatamaster.conrollers;

import flodplain.com.customerdatamaster.dto.Document;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;


public class CompanyDocumentController extends BaseService<CompanyDocumentController> {

    private final String basePath = "/api/companies/documents";
    @Step("Get company documents")
    public Response getCompanyDocuments() {
        return serviceClient(basePath)
                .get();
    }

    @Step("Add company document")
    public Response addCompanyDocument(Document document) {
        Response response = serviceClient(basePath)
                .body(document)
                .post();
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            Long documentId = response.getBody().jsonPath().getLong("documentId");
        }
        return response;
    }

    @Step("Update company document")
    public Response updateCompanyDocumentById(Long documentId, Document newDocument) {
        return serviceClient(basePath)
                .body(newDocument)
                .put("/{documentId}", documentId);
    }
}
