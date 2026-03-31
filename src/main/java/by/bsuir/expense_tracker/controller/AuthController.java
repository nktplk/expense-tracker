package by.bsuir.expense_tracker.controller;

import by.bsuir.expense_tracker.dto.RegisterDto;
import by.bsuir.expense_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("dto", new RegisterDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterDto dto, Model model) {
        try {
            userService.register(dto);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}
