package by.bsuir.expense_tracker.controller;

import by.bsuir.expense_tracker.model.Transaction;
import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.service.TransactionService;
import by.bsuir.expense_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final TransactionService transactionService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        var transactions = transactionService.findByUser(user);
        // Сумма за текущий месяц
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        // Достаем все транзакции за месяц
        java.time.LocalDateTime endOfMonth = java.time.LocalDateTime.now();
        List<Transaction> monthTransactions = transactionService.findByUserAndPeriod(user, startOfMonth, endOfMonth);

        // Считаем только РАСХОДЫ
        java.math.BigDecimal totalExpense = monthTransactions.stream()
                .filter(t -> t.getType() == by.bsuir.expense_tracker.model.enums.TransactionType.EXPENSE)
                .map(by.bsuir.expense_tracker.model.Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Считаем только ДОХОДЫ
        java.math.BigDecimal totalIncome = monthTransactions.stream()
                .filter(t -> t.getType() == by.bsuir.expense_tracker.model.enums.TransactionType.INCOME)
                .map(by.bsuir.expense_tracker.model.Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        model.addAttribute("total", totalExpense); // Оставляем для совместимости
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("totalIncome", totalIncome);

        model.addAttribute("user", user);
        model.addAttribute("transactions", transactions);
        return "dashboard";
    }
}
