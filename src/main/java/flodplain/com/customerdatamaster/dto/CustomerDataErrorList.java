package flodplain.com.customerdatamaster.dto;


public class CustomerDataErrorList {

    public static CustomerDataMasterError getUpdateCompanyNotFoundError(Long smeId) {
        return new CustomerDataMasterError(
                "",
                404,
                "Not Found",
                "//api/companies/" + smeId);
    }
}