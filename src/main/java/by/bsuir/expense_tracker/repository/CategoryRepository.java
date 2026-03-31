package by.bsuir.expense_tracker.repository;

import by.bsuir.expense_tracker.model.Category;
import by.bsuir.expense_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // БЫЛО: List<Category> findByOwnerIsNull();
    // СТАЛО:
    List<Category> findByOwnerIsNullAndIsDeletedFalse();

    // БЫЛО: List<Category> findByOwnerIn(List<User> owners);
    // СТАЛО:
    List<Category> findByOwnerInAndIsDeletedFalse(List<User> owners);
}