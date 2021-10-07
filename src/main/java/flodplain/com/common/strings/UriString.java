package flodplain.com.common.strings;

import java.net.URI;
import java.util.Map;
import org.springframework.web.util.UriComponentsBuilder;


public class UriString {

    private URI targetUri;

    public UriString(URI targetUri) {
        this.targetUri = targetUri;
    }

    public UriString addPath(String... segments) {
        this.targetUri = UriComponentsBuilder.fromUri(this.targetUri).pathSegment(segments).build().toUri();
        return this;
    }

    public UriString addQueryParams(Map<String, String> queryParams) {
        var baseURI = UriComponentsBuilder.fromUri(this.targetUri);
        queryParams.forEach(baseURI::queryParam);
        this.targetUri = baseURI.build().toUri();
        return this;
    }

    public URI build() {
        return this.targetUri;
    }

}
