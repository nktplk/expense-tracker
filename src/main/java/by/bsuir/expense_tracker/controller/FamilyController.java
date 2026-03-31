package by.bsuir.expense_tracker.controller;

import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.model.enums.Role;
import by.bsuir.expense_tracker.service.FamilyService;
import by.bsuir.expense_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;
    private final UserService userService;

    @GetMapping
    public String familyPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("user", user);

        if (user.getFamily() != null) {
            model.addAttribute("family", user.getFamily());
        }

        if (user.getRole() == Role.OWNER) {
            model.addAttribute("allClients", userService.findUsersWithoutFamily());
        }

        if (user.getRole() == Role.MANAGER) {
            model.addAttribute("allFamilies", familyService.findAllFamilies());
        }

        model.addAttribute("myInvitations", familyService.findPendingInvitations(user));
        return "family/index";
    }

    @PostMapping("/create")
    public String createFamily(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String name) {
        User user = userService.findByUsername(userDetails.getUsername());
        familyService.createFamily(user, name);
        return "redirect:/family";
    }

    @PostMapping("/invite")
    public String invite(@AuthenticationPrincipal UserDetails userDetails, @RequestParam Long clientId) {
        User owner = userService.findByUsername(userDetails.getUsername());
        User client = userService.findById(clientId);
        familyService.sendInvitation(owner, client);
        return "redirect:/family";
    }

    @PostMapping("/invitation/{id}/accept")
    public String acceptInvite(@PathVariable Long id) {
        familyService.acceptInvitation(id);
        return "redirect:/family";
    }

    @PostMapping("/invitation/{id}/reject")
    public String rejectInvite(@PathVariable Long id) {
        familyService.rejectInvitation(id);
        return "redirect:/family";
    }

    // Клиент сам покидает семью
    @PostMapping("/leave")
    public String leaveFamily(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        familyService.leaveFamily(user);
        return "redirect:/family";
    }

    // Владелец удаляет клиента из семьи
    @PostMapping("/remove/{memberId}")
    public String removeMember(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long memberId) {
        User owner = userService.findByUsername(userDetails.getUsername());
        familyService.removeMember(memberId, owner);
        return "redirect:/family";
    }
}