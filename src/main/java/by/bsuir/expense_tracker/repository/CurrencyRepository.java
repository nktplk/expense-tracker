package by.bsuir.expense_tracker.repository;

import by.bsuir.expense_tracker.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
}