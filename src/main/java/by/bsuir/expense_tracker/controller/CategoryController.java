package by.bsuir.expense_tracker.controller;

import by.bsuir.expense_tracker.model.Category;
import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.service.CategoryService;
import by.bsuir.expense_tracker.service.FamilyService;
import by.bsuir.expense_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;
    private final FamilyService familyService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("categories", categoryService.findAvailableForUser(user));
        model.addAttribute("user", user);
        return "categories/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/form";
    }

    @PostMapping("/save")
    public String save(@AuthenticationPrincipal UserDetails userDetails, @ModelAttribute Category category) {
        User user = userService.findByUsername(userDetails.getUsername());

        if (user.getRole() == by.bsuir.expense_tracker.model.enums.Role.MANAGER) {
            category.setOwner(null); // Менеджер создает только системные
        } else {
            category.setOwner(user); // Овнер создает личные для семьи
        }
        categoryService.save(category);
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.findById(id));
        return "categories/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, @ModelAttribute Category category) {
        User user = userService.findByUsername(userDetails.getUsername());
        Category existing = categoryService.findById(id);

        if (existing.getOwner() == null && user.getRole() == by.bsuir.expense_tracker.model.enums.Role.OWNER) {
            // ОВНЕР переопределяет лимит СИСТЕМНОЙ категории только для своей семьи
            if (user.getFamily() != null) {
                user.getFamily().getCustomLimits().put(existing, category.getMonthlyLimit());
                // Сохраняем семью (так как словарь лимитов хранится в ней)
                familyService.saveFamily(user.getFamily());
                return "redirect:/categories";
            }
        }

        // В остальных случаях (Менеджер редактирует системную или Овнер свою личную)
        existing.setName(category.getName());
        existing.setMonthlyLimit(category.getMonthlyLimit());
        categoryService.save(existing);
        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        categoryService.deleteById(id);
        return "redirect:/categories";
    }
}