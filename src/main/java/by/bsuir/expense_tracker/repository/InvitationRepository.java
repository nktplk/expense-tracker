package by.bsuir.expense_tracker.repository;

import by.bsuir.expense_tracker.model.Invitation;
import by.bsuir.expense_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    // Ищет входящие заявки для конкретного пользователя со статусом PENDING (Ожидает)
    List<Invitation> findByReceiverAndStatus(User receiver, String status);
}