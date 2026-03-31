package by.bsuir.expense_tracker.service;

import by.bsuir.expense_tracker.model.Family;
import by.bsuir.expense_tracker.model.Invitation;
import by.bsuir.expense_tracker.model.User;

import java.util.List;

public interface FamilyService {
    void createFamily(User owner, String name);
    void sendInvitation(User sender, User receiver);
    void acceptInvitation(Long invitationId);
    void rejectInvitation(Long invitationId);
    void leaveFamily(User user);
    List<Invitation> findPendingInvitations(User user);
    List<Family> findAllFamilies();
    void deleteFamilyById(Long id);
    void removeMember(Long memberId, User owner);
    void saveFamily(Family family);
}