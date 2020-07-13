package com.here.platform.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;


public class JConvert {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Object targetObject;

    public JConvert(Object targetObject) {
        this.targetObject = targetObject;
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @SneakyThrows
    public String toJson() {
        return objectMapper.writeValueAsString(this.targetObject);
    }

    public BodyPublisher toBodyPublisher() {
        return BodyPublishers.ofString(toJson());
    }

    public <T> T toObject(Class<T> jsonToObject) throws IOException {
        return objectMapper.readValue(String.valueOf(targetObject), jsonToObject);
    }

    @SneakyThrows
    public <T> List<T> toListOfObjects(Class<T[]> jsonToObject) {
        return Arrays.asList(objectMapper.readValue(String.valueOf(targetObject), jsonToObject));
    }

    @SneakyThrows
    public <T> T responseBodyToObject(Class<T> jsonToObject) {
        if (this.targetObject instanceof HttpResponse) {
            return objectMapper.readValue((String.valueOf(((HttpResponse) this.targetObject).body())), jsonToObject);
        } else {
            return null;
        }
    }

}
