package com.spacecl.hotel.config;

import com.spacecl.hotel.domain.Room;
import com.spacecl.hotel.domain.RoomType;
import com.spacecl.hotel.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 애플리케이션 기동 시 샘플 객실 데이터를 적재한다.
 * (테스트 프로파일에서는 비활성화하여 테스트가 스스로 데이터를 구성하도록 한다.)
 */
@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final RoomRepository roomRepository;

    public DataInitializer(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public void run(String... args) {
        if (roomRepository.count() > 0) {
            return;
        }
        roomRepository.saveAll(List.of(
                new Room("101", RoomType.SINGLE, 1),
                new Room("102", RoomType.SINGLE, 1),
                new Room("201", RoomType.DOUBLE, 2),
                new Room("202", RoomType.TWIN, 2),
                new Room("301", RoomType.SUITE, 4)
        ));
    }
}
