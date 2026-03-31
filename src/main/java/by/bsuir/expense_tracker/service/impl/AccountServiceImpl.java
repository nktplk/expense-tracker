package by.bsuir.expense_tracker.service.impl;

import by.bsuir.expense_tracker.model.Account;
import by.bsuir.expense_tracker.model.Category;
import by.bsuir.expense_tracker.model.Transaction;
import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.model.enums.TransactionType;
import by.bsuir.expense_tracker.repository.AccountRepository;
import by.bsuir.expense_tracker.repository.CategoryRepository;
import by.bsuir.expense_tracker.repository.TransactionRepository;
import by.bsuir.expense_tracker.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Account createAccount(User user, String name, BigDecimal initialBalance, by.bsuir.expense_tracker.model.Currency currency) {
        Account account = Account.builder()
                .user(user)
                .name(name)
                .currency(currency) // <-- ДОБАВИЛИ ВАЛЮТУ СЮДА
                .balance(initialBalance != null ? initialBalance : BigDecimal.ZERO)
                .build();
        return accountRepository.save(account);
    }

    @Override
    public List<Account> getUserAccounts(User user) {
        return accountRepository.findByUser(user);
    }

    @Override
    public List<Account> getFamilyAccounts(List<User> familyMembers) {
        return accountRepository.findByUserIn(familyMembers);
    }

    @Override
    @Transactional // Обязательно! Чтобы при ошибке деньги не "потерялись"
    public void addIncome(User user, Long accountId, BigDecimal amount, String source) {
        Account account = getAccountAndVerifyOwner(accountId, user);

        // Увеличиваем баланс счета
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        // Сохраняем историю
        Transaction transaction = Transaction.builder()
                .user(user)
                .account(account)
                .type(TransactionType.INCOME)
                .amount(amount)
                .description(source)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void addExpense(User user, Long accountId, Long categoryId, BigDecimal amount, String description) {
        Account account = getAccountAndVerifyOwner(accountId, user);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        // Уменьшаем баланс (он может спокойно уйти в минус, BigDecimal это поддерживает)
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .user(user)
                .account(account)
                .category(category)
                .type(TransactionType.EXPENSE)
                .amount(amount)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void transferMoney(User sender, Long fromAccountId, Long toAccountId, BigDecimal amount, BigDecimal exchangeRate, String comment) {
        Account fromAccount = getAccountAndVerifyOwner(fromAccountId, sender);
        Account toAccount = accountRepository.findById(toAccountId).orElseThrow();

        // Списываем исходную сумму
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));

        // Зачисляем сумму, умноженную на курс (если валюты разные)
        // Если валюты одинаковые, курс будет 1.0
        BigDecimal convertedAmount = amount.multiply(exchangeRate);
        toAccount.setBalance(toAccount.getBalance().add(convertedAmount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // В транзакции сохраним курс для истории
        Transaction transaction = Transaction.builder()
                .user(sender)
                .account(fromAccount)
                .targetAccount(toAccount)
                .type(TransactionType.TRANSFER)
                .amount(amount)
                .description(comment + " (Курс: " + exchangeRate + ")")
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
    }

    // Вспомогательный метод для безопасности
    private Account getAccountAndVerifyOwner(Long accountId, User user) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Счет не найден"));
        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("У вас нет прав на этот счет");
        }
        return account;
    }
}