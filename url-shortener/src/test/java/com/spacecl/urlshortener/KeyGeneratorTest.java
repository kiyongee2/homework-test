package com.spacecl.urlshortener;

import com.spacecl.urlshortener.service.KeyGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("키 생성기 테스트")
class KeyGeneratorTest {

    private final KeyGenerator keyGenerator = new KeyGenerator();

    @Test
    @DisplayName("생성된 키는 8글자이다 (요구사항 2)")
    void keyLengthIs8() {
        for (int i = 0; i < 1000; i++) {
            assertThat(keyGenerator.generate()).hasSize(8);
        }
    }

    @Test
    @DisplayName("생성된 키는 Base62(0-9a-zA-Z) 문자로만 구성된다")
    void keyUsesBase62Charset() {
        for (int i = 0; i < 1000; i++) {
            assertThat(keyGenerator.generate()).matches("[0-9a-zA-Z]{8}");
        }
    }

    @Test
    @DisplayName("연속 생성 시 키가 (사실상) 중복되지 않는다")
    void keysAreEffectivelyUnique() {
        Set<String> keys = new HashSet<>();
        int count = 100_000;
        for (int i = 0; i < count; i++) {
            keys.add(keyGenerator.generate());
        }
        // 62^8 공간에서 10만 개 생성 시 충돌은 극히 드물다.
        assertThat(keys).hasSize(count);
    }
}
