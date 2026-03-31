package by.bsuir.expense_tracker.service.impl;

import by.bsuir.expense_tracker.model.Category;
import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.repository.CategoryRepository;
import by.bsuir.expense_tracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> findAvailableForUser(User user) {
        List<Category> categories = new java.util.ArrayList<>();
        categories.addAll(categoryRepository.findByOwnerIsNull()); // Все видят системные

        if (user.getFamily() != null) {
            // Если в семье - видим личные категории создателя семьи (Овнера)
            categories.addAll(categoryRepository.findByOwnerIn(List.of(user.getFamily().getOwner())));
        } else {
            categories.addAll(categoryRepository.findByOwnerIn(List.of(user)));
        }
        return categories;
    }

    @Override
    public void save(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
    }
}
