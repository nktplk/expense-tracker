package by.bsuir.expense_tracker.repository;

import by.bsuir.expense_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    // Найти всех пользователей без семьи с определенной ролью (чтобы не пригласить Менеджера)
    List<User> findByFamilyIsNullAndRole(by.bsuir.expense_tracker.model.enums.Role role);
}
