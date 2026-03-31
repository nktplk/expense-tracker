package by.bsuir.expense_tracker.service;

import by.bsuir.expense_tracker.model.Account;
import by.bsuir.expense_tracker.model.User;
import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    Account createAccount(User user, String name, BigDecimal initialBalance, by.bsuir.expense_tracker.model.Currency currency);    List<Account> getUserAccounts(User user);
    List<Account> getFamilyAccounts(List<User> familyMembers);

    // Главные финансовые операции
    void addIncome(User user, Long accountId, BigDecimal amount, String source);
    void addExpense(User user, Long accountId, Long categoryId, BigDecimal amount, String description);
    void transferMoney(User sender, Long fromAccountId, Long toAccountId, BigDecimal amount, BigDecimal exchangeRate, String comment);
}
