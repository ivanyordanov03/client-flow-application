package app.account.service;

import app.account.model.Account;
import app.account.repository.AccountRepository;
import app.user.model.User;
import app.web.dto.RegisterOwnerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void createNew(RegisterOwnerRequest registerOwnerRequest, User owner) {

        accountRepository.save(initialize(registerOwnerRequest, owner));
    }

    private Account initialize(RegisterOwnerRequest registerOwnerRequest, User owner) {

        LocalDateTime now = LocalDateTime.now();

        return Account.builder()
                .phoneNumber(registerOwnerRequest.getPhoneNumber())
                .plan(registerOwnerRequest.getPlan())
                .owner(owner)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    public Account getByOwner(User user) {

        return accountRepository.findByOwner(user);
    }
}
