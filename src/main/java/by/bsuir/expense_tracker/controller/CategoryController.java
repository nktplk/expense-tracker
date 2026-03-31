package by.bsuir.expense_tracker.controller;

import by.bsuir.expense_tracker.model.Category;
import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.service.CategoryService;
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
    public String save(@AuthenticationPrincipal UserDetails userDetails,
                       @ModelAttribute Category category,
                       @RequestParam(defaultValue = "false") boolean isSystem) {
        User user = userService.findByUsername(userDetails.getUsername());

        // Если галочка нажата, владелец = null (категория доступна всем). Иначе — текущий юзер.
        category.setOwner(isSystem ? null : user);
        categoryService.save(category);
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.findById(id));
        return "categories/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         @ModelAttribute Category category,
                         @RequestParam(defaultValue = "false") boolean isSystem) {
        User user = userService.findByUsername(userDetails.getUsername());
        category.setId(id);

        // Позволяем менять статус "системная/личная" при редактировании
        category.setOwner(isSystem ? null : user);
        categoryService.save(category);
        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        categoryService.deleteById(id);
        return "redirect:/categories";
    }
}