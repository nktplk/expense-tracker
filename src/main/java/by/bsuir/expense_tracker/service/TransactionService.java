package by.bsuir.expense_tracker.service;

import by.bsuir.expense_tracker.model.Transaction;
import by.bsuir.expense_tracker.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    List<Transaction> findByUser(User user);
    void save(Transaction transaction);
    void deleteById(Long id);
    Transaction findById(Long id);
    List<Transaction> findByUserAndPeriod(User user, LocalDateTime from, LocalDateTime to);
    List<Transaction> findVisibleTransactions(User user);


}
