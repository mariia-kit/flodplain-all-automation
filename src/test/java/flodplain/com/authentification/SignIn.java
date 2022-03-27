package flodplain.com.authentification;

import flodplain.com.customerdatamaster.conrollers.CompanyController;
import flodplain.com.customerdatamaster.dto.Company;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("Sign_In")
public class SignIn extends BaseAuthTests{

    @Test
    @DisplayName("[Authentication] Sign in as Merchant Admin")
    void verifyAdminSignIn() {
        Company company = Company.generate();
        var response = new CompanyController()
                .withMerchantToken()
                .addCompany(company);
        System.out.println("Test passed");
    }

}
