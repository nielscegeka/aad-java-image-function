package com.example.helpers;

public class ImageRequestDTO {
    public String prompt;
    public String model;
    public String size;
    public String response_format;
    public Integer n;

    public ImageRequestDTO(String animal) {
        this.prompt = "Create me an image of this animal: " + animal;
        this.model = "gpt-image-1";
        this.size = "512x512";
        this.response_format = "base64_json";
        this.n = 1;
    }
}
