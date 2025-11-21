package com.example.helpers;

import com.example.dto.AnimalData;
import com.example.dto.AnimalWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BackupLoader {
    private static final List<AnimalData> animals = new ArrayList<>();

    static {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = BackupLoader.class.getClassLoader().getResourceAsStream("animals_with_images.json")) {
            if (in != null) {
                AnimalWrapper wrapper = mapper.readValue(in, AnimalWrapper.class);
                animals.addAll(wrapper.getAnimals());
            } else {
                System.err.println("No animals_with_images.json file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AnimalData getAnimal(String animalName) {
        return animals.stream().filter(a -> a.getName().equals(animalName)).findFirst().orElse(null);
    }
}
