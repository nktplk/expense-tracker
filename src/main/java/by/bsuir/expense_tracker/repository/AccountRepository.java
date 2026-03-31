package by.bsuir.expense_tracker.repository;

import by.bsuir.expense_tracker.model.Account;
import by.bsuir.expense_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);
    List<Account> findByUserIn(List<User> users); // Для дашборда Овнера (счета всей семьи)
}