package by.bsuir.expense_tracker.service.impl;

import by.bsuir.expense_tracker.model.Category;
import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.repository.CategoryRepository;
import by.bsuir.expense_tracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> findAvailableForUser(User user) {
        // Достаем системные (не удаленные)
        List<Category> categories = categoryRepository.findByOwnerIsNullAndIsDeletedFalse();

        // Достаем личные (не удаленные)
        if (user.getFamily() != null) {
            categories.addAll(categoryRepository.findByOwnerInAndIsDeletedFalse(user.getFamily().getMembers()));
        } else {
            // Если одиночка, ищем только его личные
            categories.addAll(categoryRepository.findByOwnerInAndIsDeletedFalse(List.of(user)));
        }
        return categories;
    }

    @Override
    public void save(Category category) {
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        // 1. Находим категорию в базе
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        // 2. Вместо repository.delete(category) делаем:
        category.setDeleted(true);

        // 3. Чтобы не было конфликта имен в будущем (если захотим создать новую "Еду"),
        // переименуем старую удаленную "Еду"
        category.setName(category.getName() + "_deleted_" + System.currentTimeMillis());

        categoryRepository.save(category);
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
    }
}
