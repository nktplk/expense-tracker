package by.bsuir.expense_tracker.controller;

import by.bsuir.expense_tracker.model.Transaction;
import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.service.CategoryService;
import by.bsuir.expense_tracker.service.TransactionService;
import by.bsuir.expense_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("transaction", new Transaction());
        model.addAttribute("categories", categoryService.findAvailableForUser(user));
        model.addAttribute("users", userService.findAll());
        return "transactions/form";
    }

    @PostMapping("/save")
    public String save(@AuthenticationPrincipal UserDetails userDetails,
                       @ModelAttribute Transaction transaction,
                       @RequestParam(required = false) java.util.List<Long> companionIds) {
        User user = userService.findByUsername(userDetails.getUsername());
        transaction.setUser(user);
        if (companionIds != null) {
            transaction.setCompanions(new java.util.ArrayList<>(companionIds.stream()
                    .map(id -> userService.findAll().stream()
                            .filter(u -> u.getId().equals(id))
                            .findFirst().orElseThrow())
                    .toList()));
        }
        transactionService.save(transaction);
        return "redirect:/dashboard";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("transaction", transactionService.findById(id));
        model.addAttribute("categories", categoryService.findAvailableForUser(user));
        model.addAttribute("users", userService.findAll());
        return "transactions/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         @ModelAttribute Transaction transaction,
                         @RequestParam(required = false) java.util.List<Long> companionIds) {
        User user = userService.findByUsername(userDetails.getUsername());

        Transaction existing = transactionService.findById(id);
        boolean isManager = user.getRole().name().equals("MANAGER");
        if (!isManager && !existing.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Нет доступа");
        }
        existing.setAmount(transaction.getAmount());
        existing.setDescription(transaction.getDescription());
        existing.setCategory(transaction.getCategory());
        existing.setUser(user);

        if (companionIds != null) {
            existing.setCompanions(new java.util.ArrayList<>(companionIds.stream()
                    .map(cid -> userService.findAll().stream()
                            .filter(u -> u.getId().equals(cid))
                            .findFirst().orElseThrow())
                    .toList()));
        } else {
            existing.setCompanions(new java.util.ArrayList<>());
        }

        transactionService.save(existing);
        return "redirect:/dashboard";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        Transaction t = transactionService.findById(id);
        boolean isManager = user.getRole().name().equals("MANAGER");
        if (!isManager && !t.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Нет доступа");
        }
        transactionService.deleteById(id);
        return "redirect:/dashboard";
    }
}
