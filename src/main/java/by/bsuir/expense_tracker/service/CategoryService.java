package by.bsuir.expense_tracker.service;

import by.bsuir.expense_tracker.model.Category;
import by.bsuir.expense_tracker.model.User;
import java.util.List;

public interface CategoryService {
    List<Category> findAvailableForUser(User user);
    void save(Category category);
    void deleteById(Long id);
    Category findById(Long id);
}
