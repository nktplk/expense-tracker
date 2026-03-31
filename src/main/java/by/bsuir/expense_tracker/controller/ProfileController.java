package by.bsuir.expense_tracker.controller;

import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.repository.UserRepository;
import by.bsuir.expense_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("user", userService.findByUsername(userDetails.getUsername()));
        return "profile";
    }

    @PostMapping
    public String update(@AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam String email,
                         @RequestParam(required = false) String password) {
        User user = userService.findByUsername(userDetails.getUsername());
        user.setEmail(email);
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        userRepository.save(user);
        return "redirect:/profile?saved";
    }
}