package com.oow.todowithspirit.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Unity(Windows·Android)는 key로 번들된 로컬/원격 에셋을 찾고, 웹은 url로 직접 렌더링한다.
 * 웹용 에셋이 아직 업로드되지 않았으면 url·fileName은 null일 수 있다.
 */
@Getter
@AllArgsConstructor
public class AssetResponse {

    private final String key;
    private final String url;
    private final String fileName;

    public static AssetResponse of(String key, String url, String fileName) {
        return new AssetResponse(key, url, fileName);
    }
}
