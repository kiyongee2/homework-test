package com.sudo.hotel.config;

import com.sudo.hotel.domain.Room;
import com.sudo.hotel.domain.RoomType;
import com.sudo.hotel.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {
    private final RoomRepository roomRepository;

    public DataInitializer(RoomRepository roomRepository){
        this.roomRepository = roomRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if(roomRepository.count() > 0){return;}
        roomRepository.saveAll(List.of(
            new Room("101", RoomType.SINGLE, 1),
            new Room("102", RoomType.SINGLE, 1),
            new Room("201", RoomType.DOUBLE, 2),
            new Room("202", RoomType.TWIN, 2),
            new Room("301", RoomType.SUITE, 4)
        ));
    }
}
