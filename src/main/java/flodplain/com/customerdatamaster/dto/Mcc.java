package flodplain.com.customerdatamaster.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Mcc {
    Long id;
    String mcc;

    public Mcc() {
    };

    public Mcc(String mcc) {
        this.mcc = mcc;
    };

    public Mcc withId(Long id) {
        this.id = id;
        return this;
    }
}
