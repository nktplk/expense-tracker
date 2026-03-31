package by.bsuir.expense_tracker.controller;

import by.bsuir.expense_tracker.model.Account;
import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.model.enums.Role;
import by.bsuir.expense_tracker.service.AccountService;
import by.bsuir.expense_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;
    private final by.bsuir.expense_tracker.repository.CurrencyRepository currencyRepository;

    @GetMapping
    public String accountsPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("user", user);

        // 2. ДОСТАЕМ ВСЕ ВАЛЮТЫ ИЗ БАЗЫ И ПЕРЕДАЕМ В HTML
        model.addAttribute("currencies", currencyRepository.findAll());

        List<Account> myAccounts = accountService.getUserAccounts(user);
        model.addAttribute("myAccounts", myAccounts);

        if (user.getFamily() != null) {
            List<Account> allFamilyAccounts = accountService.getFamilyAccounts(user.getFamily().getMembers());
            model.addAttribute("allFamilyAccounts", allFamilyAccounts);

            if (user.getRole() == by.bsuir.expense_tracker.model.enums.Role.OWNER) {
                List<Account> familyAccountsOnly = new java.util.ArrayList<>(allFamilyAccounts);
                familyAccountsOnly.removeIf(acc -> acc.getUser().getId().equals(user.getId()));
                model.addAttribute("familyAccounts", familyAccountsOnly);
            }
        } else {
            model.addAttribute("allFamilyAccounts", myAccounts);
        }

        return "accounts/index";
    }

    @PostMapping("/create")
    public String createAccount(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String name,
                                @RequestParam(required = false) BigDecimal initialBalance,
                                @RequestParam Long currencyId) { // 3. ПРИНИМАЕМ ID ВАЛЮТЫ ИЗ ФОРМЫ
        User user = userService.findByUsername(userDetails.getUsername());

        // Находим валюту в базе по её ID
        by.bsuir.expense_tracker.model.Currency currency = currencyRepository.findById(currencyId)
                .orElseThrow(() -> new RuntimeException("Валюта не найдена"));

        // Создаем счет с этой валютой
        accountService.createAccount(user, name, initialBalance, currency);
        return "redirect:/accounts";
    }

    @PostMapping("/income")
    public String addIncome(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam Long accountId,
                            @RequestParam BigDecimal amount,
                            @RequestParam String source,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            accountService.addIncome(user, accountId, amount, source);
            redirectAttributes.addFlashAttribute("success", "Доход успешно зачислен!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/accounts";
    }

    @PostMapping("/transfer")
    public String transferMoney(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam Long fromAccountId,
                                @RequestParam Long toAccountId,
                                @RequestParam BigDecimal amount,
                                @RequestParam BigDecimal exchangeRate, // Принимаем курс
                                @RequestParam String comment,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            accountService.transferMoney(user, fromAccountId, toAccountId, amount, exchangeRate, comment);
            redirectAttributes.addFlashAttribute("success", "Перевод выполнен!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/accounts";
    }

    @PostMapping("/currencies/add")
    public String addCurrency(@RequestParam String code,
                              @RequestParam String name,
                              RedirectAttributes redirectAttributes) {
        try {
            by.bsuir.expense_tracker.model.Currency currency = by.bsuir.expense_tracker.model.Currency.builder()
                    .code(code.toUpperCase().trim()) // Автоматически делаем заглавными (usd -> USD)
                    .name(name.trim())
                    .build();
            currencyRepository.save(currency);
            redirectAttributes.addFlashAttribute("success", "Валюта " + code.toUpperCase() + " успешно добавлена в систему!");
        } catch (Exception e) {
            // Если менеджер попытается добавить USD, когда он уже есть (ошибка уникальности базы)
            redirectAttributes.addFlashAttribute("error", "Валюта с таким кодом уже существует!");
        }
        return "redirect:/accounts";
    }


}