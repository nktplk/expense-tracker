package by.bsuir.expense_tracker.service.impl;

import by.bsuir.expense_tracker.model.Transaction;
import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.repository.TransactionRepository;
import by.bsuir.expense_tracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public List<Transaction> findByUser(User user) {
        return transactionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public void save(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    @Override
    public void deleteById(Long id) {
        transactionRepository.deleteById(id);
    }

    @Override
    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Транзакция не найдена"));
    }

    @Override
    public List<Transaction> findByUserAndPeriod(User user, LocalDateTime from, LocalDateTime to) {
        return transactionRepository.findByUserAndCreatedAtBetween(user, from, to);
    }

    @Override
    public List<Transaction> findVisibleTransactions(User user) {
        if (user.getRole().name().equals("MANAGER")) {
            // Менеджер видит вообще всё в базе
            return transactionRepository.findAll();
        }

        if (user.getRole().name().equals("OWNER") && user.getFamily() != null) {
            // Оунер видит транзакции всех членов своей семьи
            List<User> familyMembers = user.getFamily().getMembers();
            // ИСПОЛЬЗУЕМ НАШ НОВЫЙ МЕТОД:
            return transactionRepository.findByUserInOrderByCreatedAtDesc(familyMembers);
        }

        // Обычный Клиент (или оунер без семьи) видит только свои записи
        return transactionRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
