package com.oow.todowithspirit.domain.auth;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Validates a Kakao OAuth access token
 */
@Component
@Slf4j
public class KakaoTokenValidator {

    private static final String TOKEN_INFO_URL = "https://kapi.kakao.com/v1/user/access_token_info";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 1. Call Kakao token-info endpoint with the given access token.
     * 2. Verify the returned Kakao user ID matches expectedProviderUserId
     *
     * @param accessToken            Kakao access token from the Authorization header
     * @param expectedProviderUserId providerUserId from the login request body
     * @throws com.oow.todowithspirit.common.exception.ApiException INVALID_PROVIDER_TOKEN if the token is invalid, expired, or the ID does not match
     */
    public void validate(String accessToken, String expectedProviderUserId) {
        log.info("[validate] Validating Kakao access token for providerUserId = {}", expectedProviderUserId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<KakaoTokenInfoResponse> response;
        try {
            response = restTemplate.exchange(TOKEN_INFO_URL, HttpMethod.GET, request, KakaoTokenInfoResponse.class);
        } catch (HttpClientErrorException e) {
            log.warn("[validate] Kakao API returned error status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiException(ErrorCode.INVALID_PROVIDER_TOKEN);
        } catch (Exception e) {
            log.error("[validate] Unexpected error while calling Kakao token-info API", e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        KakaoTokenInfoResponse body = response.getBody();
        if (body == null || body.getId() == null) {
            log.warn("[validate] Kakao token-info response body is empty");
            throw new ApiException(ErrorCode.INVALID_PROVIDER_TOKEN);
        }

        String kakaoUserId = String.valueOf(body.getId());
        if (!kakaoUserId.equals(expectedProviderUserId)) {
            log.warn("[validate] Kakao user ID mismatch: expected = {}, actual = {}", expectedProviderUserId, kakaoUserId);
            throw new ApiException(ErrorCode.INVALID_PROVIDER_TOKEN);
        }

        log.info("[validate] Kakao token validation successful for providerUserId = {}", expectedProviderUserId);
    }

    /**
     * 카카오 액세스 토큰으로 토큰 정보(유저 ID 등) 조회
     *
     * @param accessToken 카카오 액세스 토큰
     * @return 카카오 토큰 정보
     */
    public KakaoTokenInfoResponse getTokenInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<KakaoTokenInfoResponse> response;
        try {
            response = restTemplate.exchange(TOKEN_INFO_URL, HttpMethod.GET, request, KakaoTokenInfoResponse.class);
        } catch (HttpClientErrorException e) {
            log.warn("[getTokenInfo] Kakao API returned error status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiException(ErrorCode.INVALID_PROVIDER_TOKEN);
        } catch (Exception e) {
            log.error("[getTokenInfo] Unexpected error while calling Kakao token-info API", e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        KakaoTokenInfoResponse body = response.getBody();
        if (body == null || body.getId() == null) {
            log.warn("[getTokenInfo] Kakao token-info response body is empty");
            throw new ApiException(ErrorCode.INVALID_PROVIDER_TOKEN);
        }

        return body;
    }
}
