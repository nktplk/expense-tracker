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
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.MANAGER)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
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