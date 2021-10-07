package flodplain.com.customerdatamaster.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class Document {
    @JsonProperty("documentId")
    Long documentId;
    @JsonProperty("documentType")
    String documentType;
    @JsonProperty("documentNumber")
    Integer documentNumber;
    @JsonProperty("issuer")
    String issuer;
    @JsonProperty("issuerOn")
    String issuerOn;
    @JsonProperty("issuerCountry")
    String issuerCountry;
    @JsonProperty("expiryDate")
    String expiryDate;
    @JsonProperty("documentUrl")
    String documentUrl;
    @JsonProperty("institutionId")
    Integer institutionId;

    @Override
    public String toString() {
        return "Company [id=" + documentId + ", documentType=" + documentType + ", documentNumber=" + documentNumber
                +  ", issuer=" + issuer + ", issuerOn=" + issuerOn + ", issuerCountry=" + issuerCountry + ", issuerCountry=" + issuerCountry
                + ", expiryDate=" + expiryDate +  ", documentUrl=" + documentUrl + ", institutionId=" + institutionId + "]";
    }

}
