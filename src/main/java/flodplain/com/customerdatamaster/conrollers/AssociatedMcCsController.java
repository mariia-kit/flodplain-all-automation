package flodplain.com.customerdatamaster.conrollers;

import flodplain.com.customerdatamaster.dto.Mcc;
import io.qameta.allure.Step;
import io.restassured.response.Response;


public class AssociatedMcCsController extends BaseService<AssociatedMcCsController> {

    private final String basePath = "/api/companies/associated-mcc";

    @Step("Get associated-mcc")
    public Response getAssociatedMcc() {
        return serviceClient(basePath)
                .get();
    }
    @Step("Add associated MCC")
    public Response addAssociatedMcc(Mcc mcc) {
        Response response = serviceClient(basePath)
                .body(mcc)
                .post();
        return response;
    }
}
