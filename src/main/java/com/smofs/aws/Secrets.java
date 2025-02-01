package com.smofs.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.util.Map;

public class Secrets {
    private static final SecretsManagerClient client = SecretsManagerClient.builder().region(Region.US_EAST_1).build();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final TypeReference<Map<String, String>> typeReference = new TypeReference<>() {};

    public static String getSecret(String secretName) {
        GetSecretValueRequest request = GetSecretValueRequest.builder().secretId(secretName).build();
        return client.getSecretValue(request).secretString();
    }

    public static Map<String, String> getSecretMap(String secretName) {
        try {
            return mapper.readValue(getSecret(secretName), typeReference);
        } catch (JsonProcessingException oops) {
            throw new RuntimeException(oops);
        }
    }
}
