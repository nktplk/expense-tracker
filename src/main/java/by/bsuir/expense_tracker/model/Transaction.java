package by.bsuir.expense_tracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;               // кто создал

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    // попутчики — другие участники расхода
    @ManyToMany
    @JoinTable(
            name = "transaction_companions",
            joinColumns = @JoinColumn(name = "transaction_id"),
            inverseJoinColumns = @JoinColumn(name = "companion_id")
    )
    private List<User> companions;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @Enumerated(EnumType.STRING)
    private by.bsuir.expense_tracker.model.enums.TransactionType type = by.bsuir.expense_tracker.model.enums.TransactionType.EXPENSE; // По умолчанию расход

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account; // Счет, с которого списали (или на который зачислили)

    @ManyToOne
    @JoinColumn(name = "target_account_id")
    private Account targetAccount; // Используется ТОЛЬКО для переводов (куда ушли деньги)
}