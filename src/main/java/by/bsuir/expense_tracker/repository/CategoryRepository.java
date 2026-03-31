package by.bsuir.expense_tracker.repository;

import by.bsuir.expense_tracker.model.Category;
import by.bsuir.expense_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // системные категории (owner = null) + личные пользователя
    List<Category> findByOwnerIsNullOrOwner(User owner);
}