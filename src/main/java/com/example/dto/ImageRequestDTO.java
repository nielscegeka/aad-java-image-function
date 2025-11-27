package com.example.dto;

public class ImageRequestDTO {
    public String prompt;
    public String model;
    public String size;
    public String style;
    public String quality;
    public String response_format;
    public Integer n;

    //Oorsprong van de verschillende parameters in het request:
    //https://learn.microsoft.com/en-us/azure/ai-foundry/openai/how-to/dall-e?view=foundry-classic&tabs=gpt-image-1
    public ImageRequestDTO(String animal, String modelName) {
        this.prompt = "Create me an image of this animal: " + animal;
        this.model = modelName;
        this.size = "1024x1024";
        this.response_format = "b64_json";
        this.style = "vivid";
        this.quality = "standard";
        this.n = 1;
    }
}
