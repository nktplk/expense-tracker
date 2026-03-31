package by.bsuir.expense_tracker.controller;

import by.bsuir.expense_tracker.model.Currency;
import by.bsuir.expense_tracker.repository.CurrencyRepository;
import by.bsuir.expense_tracker.service.FamilyService;
import by.bsuir.expense_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
public class ManagerController {

    private final UserService userService;
    private final FamilyService familyService;
    private final CurrencyRepository currencyRepository;

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        return "manager/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        userService.toggleEnabled(id);
        return "redirect:/manager/users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam String role) {
        userService.changeRole(id, role);
        return "redirect:/manager/users";
    }

    @PostMapping("/family/{id}/delete")
    public String deleteFamily(@PathVariable Long id) {
        familyService.deleteFamilyById(id);
        return "redirect:/family";
    }


}