package by.bsuir.expense_tracker.controller;

import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.model.enums.Role;
import by.bsuir.expense_tracker.service.FamilyService;
import by.bsuir.expense_tracker.service.TransactionService;
import by.bsuir.expense_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;
    private final UserService userService;
    private final TransactionService transactionService;

    @GetMapping
    public String familyPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("user", user);

        if (user.getFamily() != null) {
            model.addAttribute("family", user.getFamily());

            // Если ОВНЕР, показываем ему траты всей семьи
            if (user.getRole() == Role.OWNER) {
                // Для суммы за месяц
                java.time.LocalDateTime startOfMonth = java.time.LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                List<by.bsuir.expense_tracker.model.Transaction> famTransactions = transactionService.findByUsersAndPeriod(user.getFamily().getMembers(), startOfMonth, java.time.LocalDateTime.now());

                // Расходы семьи
                java.math.BigDecimal famExpenseTotal = famTransactions.stream()
                        .filter(t -> t.getType() == by.bsuir.expense_tracker.model.enums.TransactionType.EXPENSE)
                        .map(by.bsuir.expense_tracker.model.Transaction::getAmount)
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

                // Доходы семьи
                java.math.BigDecimal famIncomeTotal = famTransactions.stream()
                        .filter(t -> t.getType() == by.bsuir.expense_tracker.model.enums.TransactionType.INCOME)
                        .map(by.bsuir.expense_tracker.model.Transaction::getAmount)
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

                model.addAttribute("familyTotal", famExpenseTotal); // Оставляем для обратной совместимости
                model.addAttribute("familyExpenseTotal", famExpenseTotal);
                model.addAttribute("familyIncomeTotal", famIncomeTotal);
            }
        }

        // ВОЗВРАЩАЕМ ПОТЕРЯННУЮ ЛОГИКУ ДЛЯ КНОПОК ПРИГЛАШЕНИЙ
        if (user.getRole() == Role.OWNER) {
            model.addAttribute("allClients", userService.findUsersWithoutFamily());
        }
        if (user.getRole() == Role.MANAGER) {
            model.addAttribute("allFamilies", familyService.findAllFamilies());
        }
        model.addAttribute("myInvitations", familyService.findPendingInvitations(user));

        // ЗАКРЫВАЕМ МЕТОД И ВОЗВРАЩАЕМ ШАБЛОН
        return "family/index";
    } // <-- ИМЕННО ЭТОЙ СКОБКИ НЕ ХВАТАЛО!

    @PostMapping("/create")
    public String createFamily(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String name) {
        User user = userService.findByUsername(userDetails.getUsername());
        familyService.createFamily(user, name);
        return "redirect:/family";
    }

    @PostMapping("/invite")
    public String invite(@AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam String username,
                         RedirectAttributes redirectAttributes) {
        User owner = userService.findByUsername(userDetails.getUsername());

        try {
            // Пытаемся найти пользователя по введенному логину
            User client = userService.findByUsername(username);

            // Серия проверок перед отправкой приглашения:
            if (client.getRole() == Role.MANAGER) {
                redirectAttributes.addFlashAttribute("inviteError", "Нельзя пригласить администратора системы.");
                return "redirect:/family";
            }
            if (client.getFamily() != null) {
                redirectAttributes.addFlashAttribute("inviteError", "Пользователь уже состоит в другой семье.");
                return "redirect:/family";
            }
            if (client.getUsername().equals(owner.getUsername())) {
                redirectAttributes.addFlashAttribute("inviteError", "Вы не можете пригласить самого себя.");
                return "redirect:/family";
            }

            // Если все проверки пройдены — отправляем приглашение
            familyService.sendInvitation(owner, client);
            redirectAttributes.addFlashAttribute("inviteSuccess", "Приглашение успешно отправлено пользователю " + username);

        } catch (RuntimeException e) {
            // Если userService.findByUsername не найдет юзера, он выбросит ошибку, мы её ловим тут
            redirectAttributes.addFlashAttribute("inviteError", "Пользователь с таким логином не найден.");
        }

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

    // Владелец (или Совладелец/Менеджер) удаляет участника из семьи
    @PostMapping("/remove/{memberId}")
    public String removeMember(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long memberId,
                               RedirectAttributes redirectAttributes) {
        try {
            // Тот, кто нажал кнопку "Исключить" (requester)
            User requester = userService.findByUsername(userDetails.getUsername());
            familyService.removeMember(memberId, requester);
        } catch (RuntimeException e) {
            // Если сервис запретил удаление (например, Совладелец пытается удалить Создателя),
            // ловим ошибку и выводим её на экран красным блоком
            redirectAttributes.addFlashAttribute("inviteError", e.getMessage());
        }
        return "redirect:/family";
    }

    @PostMapping("/promote/{memberId}")
    public String promoteMember(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        try {
            User requester = userService.findByUsername(userDetails.getUsername());
            familyService.promoteToCoOwner(memberId, requester);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("inviteError", e.getMessage());
        }
        return "redirect:/family";
    }

    @PostMapping("/demote/{memberId}")
    public String demoteMember(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        try {
            User requester = userService.findByUsername(userDetails.getUsername());
            familyService.demoteToClient(memberId, requester);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("inviteError", e.getMessage());
        }
        return "redirect:/family";
    }
}