package by.bsuir.expense_tracker.controller;

import by.bsuir.expense_tracker.model.Category;
import by.bsuir.expense_tracker.model.Transaction;
import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.model.enums.Role;
import by.bsuir.expense_tracker.service.CategoryService;
import by.bsuir.expense_tracker.service.FamilyService;
import by.bsuir.expense_tracker.service.TransactionService;
import by.bsuir.expense_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;
    private final FamilyService familyService;
    private final TransactionService transactionService; // Нужно для прогресс-баров

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Category> categories = categoryService.findAvailableForUser(user);

        // --- ЛОГИКА ДЛЯ ПРОГРЕСС-БАРОВ ---
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        List<Transaction> monthTransactions;

        if (user.getFamily() != null) {
            monthTransactions = transactionService.findByUsersAndPeriod(user.getFamily().getMembers(), startOfMonth, LocalDateTime.now());
        } else {
            monthTransactions = transactionService.findByUserAndPeriod(user, startOfMonth, LocalDateTime.now());
        }

        Map<Long, BigDecimal> spentMap = new HashMap<>();
        for (Transaction t : monthTransactions) {
            spentMap.merge(t.getCategory().getId(), t.getAmount(), BigDecimal::add);
        }

        Map<Long, Integer> percentMap = new HashMap<>();
        for (Category c : categories) {
            BigDecimal spent = spentMap.getOrDefault(c.getId(), BigDecimal.ZERO);
            // Определяем актуальный лимит (семейный или системный)
            BigDecimal limit = c.getMonthlyLimit();
            if (c.getOwner() == null && user.getFamily() != null && user.getFamily().getCustomLimits().containsKey(c)) {
                limit = user.getFamily().getCustomLimits().get(c);
            }
            // Вычисляем процент
            if (limit != null && limit.compareTo(BigDecimal.ZERO) > 0) {
                int percent = spent.multiply(new BigDecimal(100)).divide(limit, RoundingMode.HALF_UP).intValue();
                percentMap.put(c.getId(), percent);
            } else {
                percentMap.put(c.getId(), 0);
            }
        }

        model.addAttribute("categories", categories);
        model.addAttribute("user", user);
        model.addAttribute("spentMap", spentMap);
        model.addAttribute("percentMap", percentMap);
        return "categories/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new Category());
        }
        return "categories/form";
    }

    @PostMapping("/save")
    public String save(@AuthenticationPrincipal UserDetails userDetails,
                       @ModelAttribute Category category,
                       RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername());

        category.setOwner(user.getRole() == Role.MANAGER ? null : user);

        try {
            categoryService.save(category);
        } catch (DataIntegrityViolationException e) {
            // ЛОВИМ ВАШУ ОШИБКУ УНИКАЛЬНОСТИ
            redirectAttributes.addFlashAttribute("error", "Категория с таким именем уже существует!");
            redirectAttributes.addFlashAttribute("category", category);
            return "redirect:/categories/new";
        }
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", categoryService.findById(id));
        }
        return "categories/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
                         @ModelAttribute Category category,
                         RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername());
        Category existing = categoryService.findById(id);

        try {
            if (existing.getOwner() == null) {
                // 1. РЕДАКТИРОВАНИЕ СИСТЕМНОЙ КАТЕГОРИИ
                if (user.getRole() == Role.MANAGER) {
                    // Менеджер меняет базовые настройки.
                    // Личные лимиты семей при этом НЕ перезаписываются!
                    existing.setName(category.getName());
                    existing.setMonthlyLimit(category.getMonthlyLimit());
                    categoryService.save(existing);
                }
                else if (user.getRole() == Role.OWNER && user.getFamily() != null) {
                    // Владелец сохраняет индивидуальный лимит в словарь своей семьи
                    user.getFamily().getCustomLimits().put(existing, category.getMonthlyLimit());
                    familyService.saveFamily(user.getFamily());
                }
                else {
                    redirectAttributes.addFlashAttribute("error", "У вас нет прав для изменения системной категории.");
                    return "redirect:/categories/edit/" + id;
                }
            } else {
                // 2. РЕДАКТИРОВАНИЕ ЛИЧНОЙ КАТЕГОРИИ
                if (existing.getOwner().getId().equals(user.getId()) || user.getRole() == Role.MANAGER) {
                    existing.setName(category.getName());
                    existing.setMonthlyLimit(category.getMonthlyLimit());
                    categoryService.save(existing);
                } else {
                    redirectAttributes.addFlashAttribute("error", "Нет прав на редактирование чужой категории.");
                    return "redirect:/categories/edit/" + id;
                }
            }
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Категория с таким именем уже существует!");
            return "redirect:/categories/edit/" + id;
        }

        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        categoryService.deleteById(id);
        return "redirect:/categories";
    }
}