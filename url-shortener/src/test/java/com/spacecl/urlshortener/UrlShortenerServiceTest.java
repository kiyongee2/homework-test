package com.spacecl.urlshortener;

import com.spacecl.urlshortener.exception.UrlNotFoundException;
import com.spacecl.urlshortener.model.ShortUrl;
import com.spacecl.urlshortener.repository.UrlRepository;
import com.spacecl.urlshortener.service.KeyGenerator;
import com.spacecl.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("단축 URL 서비스 테스트")
class UrlShortenerServiceTest {

    private UrlShortenerService service;

    @BeforeEach
    void setUp() {
        service = new UrlShortenerService(new UrlRepository(), new KeyGenerator());
    }

    @Test
    @DisplayName("URL을 단축하면 8글자 키가 생성된다")
    void shortenCreates8CharKey() {
        ShortUrl result = service.shorten("https://www.example.com/page");
        assertThat(result.getKey()).hasSize(8);
        assertThat(result.getOriginalUrl()).isEqualTo("https://www.example.com/page");
        assertThat(result.getClickCount()).isZero();
    }

    @Test
    @DisplayName("같은 URL을 여러 번 단축하면 항상 새로운 키가 생성된다 (요구사항 5)")
    void sameUrlAlwaysProducesNewKey() {
        String url = "https://www.example.com/same";
        ShortUrl first = service.shorten(url);
        ShortUrl second = service.shorten(url);
        ShortUrl third = service.shorten(url);

        assertThat(first.getKey()).isNotEqualTo(second.getKey());
        assertThat(second.getKey()).isNotEqualTo(third.getKey());
        assertThat(first.getKey()).isNotEqualTo(third.getKey());
    }

    @Test
    @DisplayName("이전에 생성된 단축 URL도 계속 동작한다 (요구사항 5)")
    void previouslyCreatedUrlsStillWork() {
        String url = "https://www.example.com/keep";
        ShortUrl first = service.shorten(url);
        service.shorten(url); // 새 키 추가 생성

        // 처음 만든 키가 여전히 원본으로 해석된다.
        assertThat(service.resolveAndCount(first.getKey())).isEqualTo(url);
    }

    @Test
    @DisplayName("리다이렉트 시 클릭 수가 증가한다 (요구사항 6)")
    void clickCountIncreasesOnResolve() {
        ShortUrl created = service.shorten("https://www.example.com/count");
        String key = created.getKey();

        service.resolveAndCount(key);
        service.resolveAndCount(key);
        service.resolveAndCount(key);

        assertThat(service.getStats(key).getClickCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("통계 조회는 클릭 수를 증가시키지 않는다")
    void getStatsDoesNotIncreaseCount() {
        ShortUrl created = service.shorten("https://www.example.com/stats");
        service.getStats(created.getKey());
        assertThat(service.getStats(created.getKey()).getClickCount()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 키를 조회하면 예외가 발생한다")
    void unknownKeyThrows() {
        assertThatThrownBy(() -> service.resolveAndCount("unknown0"))
                .isInstanceOf(UrlNotFoundException.class);
    }

    @Test
    @DisplayName("스킴이 없는 URL은 https:// 가 자동으로 붙는다")
    void schemeIsAddedWhenMissing() {
        ShortUrl result = service.shorten("example.com/no-scheme");
        assertThat(result.getOriginalUrl()).isEqualTo("https://example.com/no-scheme");
    }

    @Test
    @DisplayName("빈 URL은 예외가 발생한다")
    void blankUrlThrows() {
        assertThatThrownBy(() -> service.shorten("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("동시에 여러 번 리다이렉트해도 클릭 수가 정확하게 집계된다 (thread-safe)")
    void clickCountIsThreadSafe() throws InterruptedException {
        ShortUrl created = service.shorten("https://www.example.com/concurrent");
        String key = created.getKey();

        int threads = 50;
        int perThread = 200;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                for (int i = 0; i < perThread; i++) {
                    service.resolveAndCount(key);
                }
            });
        }
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

        assertThat(service.getStats(key).getClickCount())
                .isEqualTo((long) threads * perThread);
    }
}
