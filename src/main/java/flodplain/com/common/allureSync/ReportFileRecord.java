package flodplain.com.common.allureSync;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class ReportFileRecord {
    String file_name;
    String content_base64;
}
