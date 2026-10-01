package com.spacecl.urlshortener.repository;

import com.spacecl.urlshortener.model.ShortUrl;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 단축 URL 저장소.
 *
 * <p>요구사항에 따라 데이터베이스 없이 컬렉션({@link ConcurrentHashMap})만으로
 * 데이터를 보관한다. 여러 스레드가 동시에 접근해도 안전하도록
 * 동시성 컬렉션을 사용한다.</p>
 *
 * <p>애플리케이션이 재시작되면 데이터는 사라지는 인메모리 저장소이다.</p>
 */
@Repository
public class UrlRepository {

    /** key(단축 키) -> ShortUrl */
    private final ConcurrentMap<String, ShortUrl> store = new ConcurrentHashMap<>();

    /**
     * 키가 아직 사용되지 않은 경우에만 저장한다.
     *
     * @return 저장에 성공하면 true, 이미 같은 키가 존재하면 false
     */
    public boolean saveIfAbsent(ShortUrl shortUrl) {
        return store.putIfAbsent(shortUrl.getKey(), shortUrl) == null;
    }

    /** 키로 단축 URL을 조회한다. */
    public Optional<ShortUrl> findByKey(String key) {
        return Optional.ofNullable(store.get(key));
    }

    /** 해당 키가 이미 존재하는지 여부. */
    public boolean existsByKey(String key) {
        return store.containsKey(key);
    }

    /** 저장된 모든 단축 URL을 반환한다. */
    public Collection<ShortUrl> findAll() {
        return store.values();
    }

    /** 저장된 단축 URL 개수. */
    public int count() {
        return store.size();
    }
}
