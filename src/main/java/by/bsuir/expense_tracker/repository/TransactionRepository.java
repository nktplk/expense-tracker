package by.bsuir.expense_tracker.repository;

import by.bsuir.expense_tracker.model.Transaction;
import by.bsuir.expense_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);
    List<Transaction> findByUserAndCreatedAtBetween(
            User user, LocalDateTime from, LocalDateTime to
    );
}