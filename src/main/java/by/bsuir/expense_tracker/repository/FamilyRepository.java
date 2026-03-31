package by.bsuir.expense_tracker.repository;

import by.bsuir.expense_tracker.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyRepository extends JpaRepository<Family, Long> {
}