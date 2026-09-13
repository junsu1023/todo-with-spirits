package com.oow.todowithspirit.domain.auth;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.dto.auth.GoogleTokenInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class GoogleTokenValidator {

    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final RestTemplate restTemplate = new RestTemplate();

    public String validate(String accessToken, String expectedProviderUserId) {
        log.info("[validate] Validating Google access token for providerUserId: {}", expectedProviderUserId);

        GoogleTokenInfoResponse tokenInfo = callUserInfoApi(accessToken);
        String googleUserId = tokenInfo.getSub();
        String verifiedEmail = tokenInfo.isVerifiedEmail() ? tokenInfo.getEmail() : null;

        if (!googleUserId.equals(expectedProviderUserId)) {
            log.warn("[validate] Google user ID mismatch: expected = {}, actual = {}", expectedProviderUserId, googleUserId);
            throw new ApiException(ErrorCode.INVALID_PROVIDER_TOKEN);
        }

        log.info("[validate] Google token validation successful for providerUserId = {}", expectedProviderUserId);

        return verifiedEmail;
    }

    private GoogleTokenInfoResponse callUserInfoApi(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<GoogleTokenInfoResponse> userInfoResponse;
        try {
            userInfoResponse = restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, request, GoogleTokenInfoResponse.class);
        } catch (HttpClientErrorException e) {
            log.warn("[callUserInfoApi] Google API returned error status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiException(ErrorCode.INVALID_PROVIDER_TOKEN);
        } catch (Exception e) {
            log.error("[callUserInfoApi] Unexpected error while calling Google userinfo API", e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        GoogleTokenInfoResponse body = userInfoResponse.getBody();
        if (body == null || body.getSub() == null) {
            log.warn("[callUserInfoApi] Google userinfo response body is empty");
            throw new ApiException(ErrorCode.INVALID_PROVIDER_TOKEN);
        }

        return body;
    }
}