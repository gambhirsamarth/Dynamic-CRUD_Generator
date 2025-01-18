package com.hackathon.deployer.service;

import com.hackathon.deployer.dto.ResponseDto;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import net.sf.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Autowired
    private DynamicService dynamicService;

    @Value("${openai.api.key}")
    private String apiKey;

    private final String insertEndPoint = "api/crud/insert?tableName=";
    private final String fetchEndPoint = "http://localhost:8080/api/crud/fetch/";


    private static final String promptConstraint = """
            . Give me response only in a JSON in this format : {
                "tableName": "employees",
                "fields": {}
            } where fields is a JSONObject where key will be mysql column name, value will be its appropriate type supported by mysql""";

    public ResponseDto processPrompt(String prompt) {
        ResponseDto responseDto = new ResponseDto();
        String openAiResponse = callOpenAI(prompt);
        JSONObject json = JSONObject.fromObject(openAiResponse);
        JSONArray choices = json.optJSONArray("choices");
        JSONObject choice = choices.optJSONObject(0);
        JSONObject message = choice.optJSONObject("message");
        JSONObject aiResponseJson = message.optJSONObject("content");
        String tableName = aiResponseJson.optString("tableName");
        JSONObject fields = aiResponseJson.optJSONObject("fields");
        List<Map<String, Object>> tableDescription = dynamicService.describeTable(tableName);
        if (tableDescription == null) {
            dynamicService.createTable(tableName, fields);
        } else {
            // TODO
        }
        responseDto.tableName = tableName;
        responseDto.tableSchema = fields;
        responseDto.fetchEndpoint = fetchEndPoint + tableName + "/{id}";
        responseDto.insertEndpoint = insertEndPoint + tableName;
        return responseDto;
    }

    private String callOpenAI(String prompt) {
        int retries = 3;
        int backoff = 1000;
        try {
            Map<String, Object> requestBody = Map.of("model", "gpt-4", "messages", List.of(Map.of("role", "user", "content", prompt + promptConstraint)));
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
            for (int i = 0; i < retries; i++) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                            .header("Authorization", "Bearer " + apiKey)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                            .build();
                    HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        return response.body();
                    } else if (response.statusCode() == 429) {
                        System.out.println("Rate limited. Retrying...");
                        Thread.sleep(backoff);
                        backoff *= 2;
                    } else {
                        throw new RuntimeException("Unexpected response: " + response.body());
                    }
                } catch (IOException e) {
                    System.err.println("Error communicating with OpenAI API: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error constructing or sending request to OpenAI: " + e.getMessage());
        }
        throw new RuntimeException("Failed after retries due to rate-limiting or other issues.");
    }
}
