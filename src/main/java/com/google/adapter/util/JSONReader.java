package com.google.adapter.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JSONReader {
    private final static ObjectMapper mapper = new ObjectMapper();

    public static JsonNode readJson(String filePath) throws IOException {
        JsonNode jsonNode = mapper.readValue(new File(filePath), JsonNode.class);
        return jsonNode;
    }

    public static <T> T readJson(String filePath, Class<T> clazz) throws IOException {
        InputStream inputStream = JSONReader.class.getResourceAsStream("/" + filePath);
        T jsonNode = mapper.readValue(inputStream, clazz);
        return jsonNode;
    }

    public static JsonNode readData(String filePath, String key) throws IOException {
        JsonNode jsonNode = readJson(filePath);
        JsonNode dataNode = jsonNode.get(key);
        return dataNode;
    }

    public static <T> List<T> readDataArray(String filePath, String key, Class<T[]> typeReference) throws IOException {
        JsonNode jsonNode = readData(filePath, key);
        List<T> list = Arrays.asList(mapper.convertValue(jsonNode, typeReference));
        return list;
    }

    public static <T> List<T> filterData(List<T> data, Class<T> clazz, String... criteria) {
        if (Objects.isNull(data) || data.isEmpty()) {
            throw new NullPointerException("Provided list is empty or null");
        }

        List<T> filteredList = new ArrayList<>();
        JsonNode jsonNode = mapper.convertValue(data, JsonNode.class);
        Stream.of(criteria).forEach(s -> {
            filteredList.addAll(StreamSupport.stream(jsonNode.spliterator(), false)
                    .filter(node -> node.toString().contains(s)).map(node -> mapper.convertValue(node, clazz))
                    .collect(Collectors.toList()));

        });

        return filteredList;
    }
}

