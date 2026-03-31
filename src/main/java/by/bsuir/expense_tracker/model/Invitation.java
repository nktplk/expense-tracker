package by.bsuir.expense_tracker.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "invitations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User sender; // Owner

    @ManyToOne
    private User receiver; // Client

    @ManyToOne
    private Family family;

    private String status; // PENDING, ACCEPTED, REJECTED
}