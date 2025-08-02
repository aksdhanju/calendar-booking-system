package com.company.calendar.utils;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonDataHelper {
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @SneakyThrows
    public static String toJson(Object data) {
        return mapper.writeValueAsString(data);
    }

    @SneakyThrows
    public static <T> T fromJson(String data, Class<T> responseClass) {
        if (!StringUtils.hasText(data)) {
            return null;
        }
        try {
            return mapper.readValue(data, responseClass);
        } catch (Exception e) {
            log.warn("Failed to parse JSON for class {}: {}", responseClass.getSimpleName(), e.getMessage());
            return null;
        }
    }

    public static <T> List<T> toList(String data, Class<T[]> responseClass) {
        if (data == null) return Collections.emptyList();
        try {
            T[] array = mapper.readValue(data, responseClass);
            return array != null ? asList(array) : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to parse JSON array for class {}: {}", responseClass.getSimpleName(), e.getMessage());
            return Collections.emptyList();
        }
    }
}