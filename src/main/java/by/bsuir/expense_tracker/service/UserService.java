package by.bsuir.expense_tracker.service;

import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.dto.RegisterDto;
import java.util.List;

public interface UserService {
    void register(RegisterDto dto);
    User findByUsername(String username);
    List<User> findAll();
    void toggleEnabled(Long userId);
    void changeRole(Long userId, String role);
    User findById(Long id);
    List<User> findUsersWithoutFamily();
}