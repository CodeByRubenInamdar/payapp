package com.demo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class GoogleLoginController {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    @GetMapping("/login")
    public String googleLogin() {
        // Build the Google OAuth login URL
        String url = GOOGLE_AUTH_URL +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode("profile email", StandardCharsets.UTF_8) +
                "&access_type=offline" +
                "&prompt=consent"; // Ensures a fresh login session
        return "redirect:" + url;
    }

    @GetMapping("/google/callback")
    @ResponseBody
    public ResponseEntity<String> googleCallback(@RequestParam("code") String code) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
          
            String tokenRequestBody = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                    "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                    "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) +
                    "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                    "&grant_type=authorization_code";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(tokenRequestBody, headers);

           
            ResponseEntity<String> tokenResponse = restTemplate.exchange(
                    GOOGLE_TOKEN_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (tokenResponse.getStatusCode() == HttpStatus.OK) {
                JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
                String accessToken = tokenJson.get("access_token").asText();

               
                HttpHeaders userHeaders = new HttpHeaders();
                userHeaders.setBearerAuth(accessToken);
                HttpEntity<Void> userEntity = new HttpEntity<>(userHeaders);

                ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                        GOOGLE_USER_INFO_URL,
                        HttpMethod.GET,
                        userEntity,
                        String.class
                );

                if (userInfoResponse.getStatusCode() == HttpStatus.OK) {
                    return ResponseEntity.ok(userInfoResponse.getBody());
                } else {
                    return ResponseEntity.status(userInfoResponse.getStatusCode())
                            .body("Error fetching user info: " + userInfoResponse.getStatusCode());
                }
            } else {
                return ResponseEntity.status(tokenResponse.getStatusCode())
                        .body("Error fetching access token: " + tokenResponse.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during Google Authentication: " + e.getMessage());
        }
    }
}
