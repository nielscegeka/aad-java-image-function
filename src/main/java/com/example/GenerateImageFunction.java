package com.example;

import com.example.dto.AnimalData;
import com.example.exception.BadRequestException;
import com.example.exception.GenerationException;
import com.example.helpers.BackupLoader;
import com.example.helpers.ConfigLoader;
import com.example.dto.ImageRequestDTO;
import com.example.dto.OpenAIImageResponse;
import com.example.helpers.ResponseBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class GenerateImageFunction {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResponseBuilder responseBuilder = new ResponseBuilder();

    @FunctionName("generateImage")
    public HttpResponseMessage runFunction(
            @HttpTrigger(name = "request", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.FUNCTION)
            HttpRequestMessage<Map<String, Object>> request, ExecutionContext context) {
        try {
            String animal = request.getQueryParameters().get("animal");
            if (animal == null || animal.isBlank()) {
               throw new BadRequestException("Missing query parameter 'animal'");
            }

            final Boolean DEV_MODE = Boolean.valueOf(ConfigLoader.get("DEV_MODE"));
            if (DEV_MODE) {
                AnimalData animalWithImage = BackupLoader.getAnimal(animal);
                if (animalWithImage == null) {
                    throw new BadRequestException("Animal not found");
                }

                String base64ToDataUrl = "data:image/png;base64," + animalWithImage.getImage_base64();
                return responseBuilder.createResponse(request, HttpStatus.OK, base64ToDataUrl, true);
            }

            ImageRequestDTO animalPayload = new ImageRequestDTO(animal);
            String payloadJson = objectMapper.writeValueAsString(animalPayload);

            HttpResponse<String> generationResponse = doGenerationRequest(payloadJson);
            if (generationResponse.statusCode() != 200) {
                throw new GenerationException(generationResponse.body());
            }

            OpenAIImageResponse response = objectMapper.readValue(generationResponse.body(), OpenAIImageResponse.class);
            String base64encoded = response.data.getFirst().base64_json;
            String base64ToDataUrl = "data:image/png;base64," + base64encoded;

            return responseBuilder.createResponse(request, HttpStatus.OK, base64ToDataUrl, true);
        } catch (BadRequestException e) {
            logException(context, e);
            return responseBuilder.createResponse(request, HttpStatus.BAD_REQUEST, e.getMessage(), false);
        } catch (IOException | InterruptedException | GenerationException e) {
            logException(context, e);
            return responseBuilder.createResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), false);
        }
    }

    private HttpResponse<String> doGenerationRequest(String payload) throws IOException, InterruptedException {
        final String OPENAI_ENDPOINT = ConfigLoader.get("AZURE_OPENAI_ENDPOINT");
        final String OPENAI_KEY = ConfigLoader.get("AZURE_OPENAI_API_KEY");
        final String DEPLOYMENT_NAME = ConfigLoader.get("AZURE_OPENAI_DALLE_DEPLOYMENT");
        final String API_VERSION = ConfigLoader.get("API_VERSION");

        URI uri = URI.create(OPENAI_ENDPOINT + "/openai/deployments/" + DEPLOYMENT_NAME + "/images/generations?api-version=" + API_VERSION);
        HttpRequest request = HttpRequest.newBuilder().uri(uri)
                .header("Content-Type", "application/json")
                .header("api-key", OPENAI_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void logException(ExecutionContext context, Exception e) {
        context.getLogger().severe(e.toString());
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        context.getLogger().severe(sw.toString());
    }
}
