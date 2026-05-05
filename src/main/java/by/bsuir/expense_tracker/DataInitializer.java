package by.bsuir.expense_tracker;

import by.bsuir.expense_tracker.model.Category;
import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.model.enums.Role;
import by.bsuir.expense_tracker.repository.CategoryRepository;
import by.bsuir.expense_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 1. Обработка АДМИНА (с проверкой и восстановлением роли)
        userRepository.findByUsername("admin").ifPresentOrElse(
                admin -> {
                    // Если админ найден, проверяем его роль
                    if (admin.getRole() != Role.MANAGER) {
                        admin.setRole(Role.MANAGER);
                        userRepository.save(admin);
                        System.out.println("Role for 'admin' restored to ADMIN");
                    }
                },
                () -> {
                    // Если админа нет в базе, создаем его
                    User admin = User.builder()
                            .username("admin")
                            .email("admin@example.com")
                            .password(passwordEncoder.encode("admin123"))
                            .role(Role.MANAGER) // Тут мы исправили MANAGER на ADMIN
                            .enabled(true)
                            .build();
                    userRepository.save(admin);
                    System.out.println("Default user 'admin' created");
                }
        );

// 2. Создание OWNER (если его нет)
        if (userRepository.findByUsername("owner").isEmpty()) {
            User owner = User.builder()
                    .username("owner")
                    .email("owner@example.com")
                    .password(passwordEncoder.encode("owner123"))
                    .role(Role.OWNER) // Убедись, что у тебя есть Role.OWNER или Role.MANAGER
                    .enabled(true)
                    .build();
            userRepository.save(owner);
            System.out.println("Default user 'owner' created");
        }

// 3. Создание обычного USER (если его нет)
        if (userRepository.findByUsername("user").isEmpty()) {
            User user = User.builder()
                    .username("user")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .role(Role.CLIENT)
                    .enabled(true)
                    .build();
            userRepository.save(user);
            System.out.println("Default user 'user' created");
        }

        if (categoryRepository.count() == 0) {
            List<Category> categories = List.of(
                    Category.builder().name("Продукты").monthlyLimit(new BigDecimal("500")).build(),
                    Category.builder().name("Транспорт").monthlyLimit(new BigDecimal("200")).build(),
                    Category.builder().name("Развлечения").monthlyLimit(new BigDecimal("300")).build(),
                    Category.builder().name("Здоровье").monthlyLimit(new BigDecimal("150")).build()
            );
            categoryRepository.saveAll(categories);
        }
    }
}