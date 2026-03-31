package by.bsuir.expense_tracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

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
    private User owner; // Создатель семьи

    @OneToMany(mappedBy = "family")
    private List<User> members;
}