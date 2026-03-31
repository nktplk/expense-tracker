package by.bsuir.expense_tracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Например: "Наличные", "Карта зарплатная", "Счет от родителей"

    @Column(precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO; // Баланс может уходить в минус

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Владелец счета

    @ManyToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;
}