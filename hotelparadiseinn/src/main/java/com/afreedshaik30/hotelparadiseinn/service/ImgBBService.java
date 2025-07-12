package com.afreedshaik30.hotelparadiseinn.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.afreedshaik30.hotelparadiseinn.exception.OurException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Base64;

@Service
public class ImgBBService {

    // Injects the API key from application.properties or environment variables
    @Value("${imgbb.api.key}")
    private String imgbbApiKey;

    // Method to upload an image to ImgBB and return the public image URL
    public String uploadImage(MultipartFile photo) {
        try {
            // Optional validation: Check image size (limit 5MB)
            if (photo.getSize() > 5 * 1024 * 1024) {
                throw new OurException("Image size exceeds the allowed limit (5MB)");
            }

            // Convert the uploaded MultipartFile into a byte array
            byte[] imageBytes = photo.getBytes();

            // Encode the byte array to a Base64 string (ImgBB expects base64 image data)
            String encodedImage = Base64.getEncoder().encodeToString(imageBytes);

            // Construct the ImgBB upload URL with the API key
            URI uri = new URI("https://api.imgbb.com/1/upload?key=" + imgbbApiKey);

            // Prepare the HTTP headers (content type = form-urlencoded)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Prepare the request body with the base64 image string
            String body = "image=" + encodedImage;

            // Wrap headers and body in an HttpEntity
            HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

            // Use RestTemplate to make the POST request to ImgBB
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);

            // Check if the response is not 200 OK, then throw a custom exception
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new OurException("ImgBB upload failed with status: " + response.getStatusCode());
            }

            // Parse the response JSON to extract the public image URL
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());

            // Return the uploaded image URL from the response JSON
            return json.get("data").get("url").asText();

        } catch (Exception e) {
            // Catch and wrap all exceptions into a custom application-level exception
            throw new OurException("Failed to upload image to ImgBB: " + e.getMessage());
        }
    }
}
