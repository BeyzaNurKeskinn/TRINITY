package com.project.Trinity.Service;

import com.project.Trinity.Entity.Status;
import com.project.Trinity.Entity.User;
import com.project.Trinity.Repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class ScheduledTasks {

    private final UserRepository userRepository;
    private final UserService userService;

    public ScheduledTasks(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // Her gün saat 00:00'da çalışır
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteFrozenAccounts() {
        List<User> inactiveUsers = userRepository.findAllByStatus(Status.INACTIVE); // INACTIVE kullanıcıları al
        for (User user : inactiveUsers) {
            if (user.getFrozenAt() != null) { // frozenAt doluysa dondurulmuş demektir
                long daysFrozen = ChronoUnit.DAYS.between(user.getFrozenAt(), LocalDateTime.now());
                if (daysFrozen >= 30) {
                    userService.deleteUser(user.getId());
                }
            }
        }
    }
}