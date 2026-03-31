package by.bsuir.expense_tracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "families")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Family {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "family")
    private List<User> members;

    // НОВОЕ ПОЛЕ: Хранит переопределенные лимиты для системных категорий
    @ElementCollection
    @CollectionTable(name = "family_category_limits", joinColumns = @JoinColumn(name = "family_id"))
    @MapKeyJoinColumn(name = "category_id")
    @Column(name = "custom_limit")
    private Map<Category, BigDecimal> customLimits = new HashMap<>();
}