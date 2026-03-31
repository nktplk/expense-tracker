package by.bsuir.expense_tracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;          // "Еда", "Транспорт", "Развлечения"

    private BigDecimal monthlyLimit; // лимит в месяц (может быть null)

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;           // null = системная категория, иначе — личная

    @Column(name = "is_deleted")
    private boolean isDeleted = false; // По умолчанию категория активна
}