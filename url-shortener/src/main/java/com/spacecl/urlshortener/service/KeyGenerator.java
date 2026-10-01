package com.spacecl.urlshortener.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 단축 URL 키 생성기.
 *
 * <p>키 생성 알고리즘(요구사항 3: 자유 구현):</p>
 * <ul>
 *   <li>Base62 문자 집합(0-9, a-z, A-Z)에서 무작위로 8글자를 뽑아 키를 만든다.</li>
 *   <li>{@link SecureRandom}을 사용하여 예측이 어렵고 균일하게 분포된 키를 생성한다.</li>
 *   <li>매 호출마다 무작위로 생성하므로 동일한 원본 URL이라도 매번 다른 키가 나온다
 *       (요구사항 5: 항상 새로운 단축 URL 생성).</li>
 * </ul>
 *
 * <p>키 공간은 62^8 ≈ 2.18 × 10^14 로 충분히 크며, 실제 저장 시에는
 * 저장소에서 중복 여부를 다시 확인하여 충돌을 방지한다.</p>
 */
@Component
public class KeyGenerator {

    /** 단축 키 길이 (요구사항 2: 8글자) */
    public static final int KEY_LENGTH = 8;

    private static final char[] BASE62 =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private final SecureRandom random = new SecureRandom();

    /** 8글자 Base62 키를 생성한다. */
    public String generate() {
        StringBuilder sb = new StringBuilder(KEY_LENGTH);
        for (int i = 0; i < KEY_LENGTH; i++) {
            sb.append(BASE62[random.nextInt(BASE62.length)]);
        }
        return sb.toString();
    }
}
