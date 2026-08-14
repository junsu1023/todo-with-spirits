package com.oow.todowithspirit.domain.auth;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.dto.auth.KakaoTokenInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class KakaoTokenValidator {

    private static final String TOKEN_INFO_URL = "https://kapi.kakao.com/v1/user/access_token_info";

    private final RestTemplate restTemplate = new RestTemplate();
    private final String clientSecret;

    public KakaoTokenValidator(@Value("${kakao.client-secret:}") String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void validate(String accessToken, String expectedProviderUserId) {
        log.info("[validate] Validating Kakao access token for providerUserId = {}", expectedProviderUserId);

        KakaoTokenInfoResponse tokenInfo = callTokenInfoApi(accessToken);
        String kakaoUserId = String.valueOf(tokenInfo.getId());

        if (!kakaoUserId.equals(expectedProviderUserId)) {
            log.warn("[validate] Kakao user ID mismatch: expected = {}, actual = {}", expectedProviderUserId, kakaoUserId);
            throw new ApiException(ErrorCode.INVALID_PROVIDER_TOKEN);
        }

        log.info("[validate] Kakao token validation successful for providerUserId = {}", expectedProviderUserId);
    }

    public KakaoTokenInfoResponse getTokenInfo(String accessToken) {
        return callTokenInfoApi(accessToken);
    }

    private KakaoTokenInfoResponse callTokenInfoApi(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<KakaoTokenInfoResponse> response;
        try {
            response = restTemplate.exchange(TOKEN_INFO_URL, HttpMethod.GET, request, KakaoTokenInfoResponse.class);
        } catch (HttpClientErrorException e) {
            log.warn("[callTokenInfoApi] Kakao API returned error status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiException(ErrorCode.INVALID_PROVIDER_TOKEN);
        } catch (Exception e) {
            log.error("[callTokenInfoApi] Unexpected error while calling Kakao token-info API", e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        KakaoTokenInfoResponse body = response.getBody();
        if (body == null || body.getId() == null) {
            log.warn("[callTokenInfoApi] Kakao token-info response body is empty");
            throw new ApiException(ErrorCode.INVALID_PROVIDER_TOKEN);
        }

        return body;
    }
}
