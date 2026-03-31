package by.bsuir.expense_tracker.service.impl;

import by.bsuir.expense_tracker.model.Family;
import by.bsuir.expense_tracker.model.Invitation;
import by.bsuir.expense_tracker.model.User;
import by.bsuir.expense_tracker.repository.FamilyRepository;
import by.bsuir.expense_tracker.repository.InvitationRepository;
import by.bsuir.expense_tracker.repository.UserRepository;
import by.bsuir.expense_tracker.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyRepository familyRepository;
    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createFamily(User owner, String name) {
        if (owner.getFamily() != null) {
            throw new RuntimeException("Вы уже состоите в семье");
        }

        // Создаем саму семью
        Family family = Family.builder()
                .name(name)
                .owner(owner)
                .build();
        family = familyRepository.save(family);

        // Привязываем Владельца к его новой семье
        owner.setFamily(family);
        userRepository.save(owner);
    }

    @Override
    public void sendInvitation(User sender, User receiver) {
        if (sender.getFamily() == null) {
            throw new RuntimeException("У вас нет семьи для приглашения");
        }

        Invitation invitation = Invitation.builder()
                .sender(sender)
                .receiver(receiver)
                .family(sender.getFamily())
                .status("PENDING") // Статус: Ожидает подтверждения
                .build();

        invitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public void acceptInvitation(Long invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Приглашение не найдено"));

        invitation.setStatus("ACCEPTED");
        invitationRepository.save(invitation);

        // Добавляем пользователя в семью
        User receiver = invitation.getReceiver();
        receiver.setFamily(invitation.getFamily());
        userRepository.save(receiver);
    }

    @Override
    public void rejectInvitation(Long invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Приглашение не найдено"));

        invitation.setStatus("REJECTED");
        invitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public void leaveFamily(User user) {
        if (user.getFamily() == null) return;

        if (user.getId().equals(user.getFamily().getOwner().getId())) {
            deleteFamilyById(user.getFamily().getId());
        } else {
            user.setFamily(null);
            // Если Совладелец уходит сам, он снова становится обычным Клиентом
            if (user.getRole() == by.bsuir.expense_tracker.model.enums.Role.OWNER) {
                user.setRole(by.bsuir.expense_tracker.model.enums.Role.CLIENT);
            }
            userRepository.save(user);
        }
    }

    @Override
    public List<Invitation> findPendingInvitations(User user) {
        return invitationRepository.findByReceiverAndStatus(user, "PENDING");
    }

    @Override
    public List<Family> findAllFamilies() {
        return familyRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteFamilyById(Long id) {
        Family family = familyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Семья не найдена"));

        // Перед удалением семьи нужно "отвязать" от нее всех пользователей,
        // иначе база данных выдаст ошибку из-за внешних ключей (Foreign Key Constraint)
        List<User> members = family.getMembers();
        if (members != null) {
            for (User member : members) {
                member.setFamily(null);
                userRepository.save(member);
            }
        }

        familyRepository.delete(family);
    }

    @Override
    @Transactional
    public void removeMember(Long memberId, User requester) {
        User target = userRepository.findById(memberId).orElseThrow();
        Family family = target.getFamily();
        if (family == null) return;

        boolean canRemove = false;

        if (requester.getRole() == by.bsuir.expense_tracker.model.enums.Role.MANAGER) {
            canRemove = true;
        } else if (family.getOwner().getId().equals(requester.getId())) {
            canRemove = true; // Создатель может удалить кого угодно
        } else if (requester.getRole() == by.bsuir.expense_tracker.model.enums.Role.OWNER) {
            // Если удаляет СОВЛАДЕЛЕЦ
            if (target.getId().equals(family.getOwner().getId())) {
                throw new RuntimeException("Нельзя исключить создателя семьи.");
            }
            if (target.getRole() == by.bsuir.expense_tracker.model.enums.Role.OWNER) {
                throw new RuntimeException("Нельзя исключить другого совладельца.");
            }
            canRemove = true; // Совладелец может удалить обычного CLIENT
        }

        if (canRemove) {
            target.setFamily(null);
            // Если выгнали Совладельца, забираем у него права OWNER
            if (target.getRole() == by.bsuir.expense_tracker.model.enums.Role.OWNER && !target.getId().equals(family.getOwner().getId())) {
                target.setRole(by.bsuir.expense_tracker.model.enums.Role.CLIENT);
            }
            userRepository.save(target);
        }
    }

    @Override
    public void saveFamily(Family family) {
        familyRepository.save(family);
    }

    @Override
    @Transactional
    public void promoteToCoOwner(Long memberId, User requester) {
        User target = userRepository.findById(memberId).orElseThrow();
        Family family = target.getFamily();

        // Только физический Создатель семьи может раздавать права Совладельца
        if (family != null && family.getOwner().getId().equals(requester.getId())) {
            target.setRole(by.bsuir.expense_tracker.model.enums.Role.OWNER);
            userRepository.save(target);
        } else {
            throw new RuntimeException("Только создатель семьи может назначать совладельцев.");
        }
    }

    @Override
    @Transactional
    public void demoteToClient(Long memberId, User requester) {
        User target = userRepository.findById(memberId).orElseThrow();
        Family family = target.getFamily();

        if (family != null && family.getOwner().getId().equals(requester.getId())) {
            target.setRole(by.bsuir.expense_tracker.model.enums.Role.CLIENT);
            userRepository.save(target);
        } else {
            throw new RuntimeException("Только создатель семьи может понижать совладельцев.");
        }
    }
}