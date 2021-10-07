package flodplain.com.customerdatamaster.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import flodplain.com.common.helpers.UniqueId;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class Company {
    @JsonProperty("smeId")
    Long smeId;
    @JsonProperty("name")
    String name;
    @JsonProperty("type")
    String type;
    @JsonProperty("registeredOn")
    String registeredOn;
    @JsonProperty("applicationId")
    String applicationId;
    @JsonProperty("institutionId")
    Integer institutionId;

    @Override
    public String toString() {
        return "Company [id=" + smeId + ", name=" + name + ", type=" + type
                +  ", registeredOn=" + registeredOn + ", applicationId=" + applicationId + ", institutionId=" + institutionId + "]";
    }

    public Company(String name, String type, String registeredOn, String applicationId, Integer institutionId) {
        this.name = name;
        this.type = type;
        this.registeredOn = registeredOn;
        this.applicationId = applicationId;
        this.institutionId = institutionId;
    }

    public static Company generate() {
        String id = UniqueId.getUniqueKey();
        return new Company(
                "Company test" + id,
                "company",
                "2021-10-06T11:55:05.610Z",
                "1",
                1);
    }

}
