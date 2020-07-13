package com.here.platform.common;

import java.io.IOException;
import java.net.CookieManager;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;


@NoArgsConstructor
public class HttpClient {

    private final Builder http11 = java.net.http.HttpClient.newBuilder().version(Version.HTTP_1_1);
    private java.net.http.HttpClient client;

    public static BodyPublisher ofMimeMultipartData(Map<Object, Object> data, String boundary) throws IOException {
        var byteArrays = new ArrayList<byte[]>();
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=")
                .getBytes(StandardCharsets.UTF_8);
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            byteArrays.add(separator);

            if (entry.getValue() instanceof Path) {
                var path = (Path) entry.getValue();
                String mimeType = Files.probeContentType(path);
                byteArrays.add(("\"" + entry.getKey() + "\"; filename=\"" + path.getFileName()
                        + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                byteArrays.add(Files.readAllBytes(path));
                byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
            } else {
                byteArrays.add(("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n")
                        .getBytes(StandardCharsets.UTF_8));
            }
        }
        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
        return BodyPublishers.ofByteArrays(byteArrays);
    }

    public HttpClient basic() {
        this.client = http11.build();
        return this;
    }

    public HttpClient basicWithCookie() {
        this.client = http11
                .cookieHandler(new CookieManager())
                .followRedirects(Redirect.ALWAYS)
                .build();
        return this;
    }

    @SneakyThrows
    public HttpResponse<String> send(HttpRequest targetRequest) {
        var response = client.send(targetRequest, BodyHandlers.ofString());
        AllureExtension.logHttpRequest(targetRequest);
        AllureExtension.logHttpResponse(response);
        return response;
    }

}
