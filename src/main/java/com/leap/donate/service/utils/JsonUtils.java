package com.leap.donate.service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

    
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .setSerializationInclusion(Include.NON_NULL);

        /**
     * Serializes an object to a JSON string.
     *
     * @param object The object to serialize.
     * @param <T>    The type of the object being serialized.
     * @return A JSON string representing the serialized object, or null if serialization fails.
     */
    public static <T> String toJson(T object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            LOGGER.error("Convert from {} error", object, ex);
        }
        return null;
    }
}
