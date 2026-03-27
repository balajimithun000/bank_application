package com.example.bankapp.demo.service;

import com.example.bankapp.demo.model.Account;
import com.example.bankapp.demo.model.Transaction;
import com.example.bankapp.demo.repository.AccountRepository;
import com.example.bankapp.demo.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // ================= FIND ACCOUNT =================
    public Account findAccountBYUsername(String username){
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    // ================= REGISTER =================
    public Account registerAccount(String username, String password){
        if(accountRepository.findByUsername(username).isPresent()){
            throw new RuntimeException("Username already exists");
        }

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setBalance(BigDecimal.ZERO);

        return accountRepository.save(account);
    }

    // ================= DEPOSIT =================
    public void deposit(Account account, BigDecimal amount){

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction(
                amount,
                LocalDateTime.now(),
                account,
                "Deposit"
        );

        transactionRepository.save(transaction);
    }

    // ================= WITHDRAW (FINAL LOGIC) =================
    public void withdraw(Account account, BigDecimal amount){

        BigDecimal minimumBalance = new BigDecimal("5000");
        BigDecimal penaltyAmount = new BigDecimal("500");

        // Step 1: subtract withdraw
        BigDecimal newBalance = account.getBalance().subtract(amount);

        // Step 2: apply penalty if below 5000
        if (newBalance.compareTo(minimumBalance) < 0) {

            newBalance = newBalance.subtract(penaltyAmount);

            // Penalty transaction
            Transaction penaltyTxn = new Transaction(
                    penaltyAmount,
                    LocalDateTime.now(),
                    account,
                    "Penalty (Minimum balance not maintained)"
            );

            transactionRepository.save(penaltyTxn);
        }

        // Step 3: allow negative balance
        account.setBalance(newBalance);
        accountRepository.save(account);

        // Step 4: withdraw transaction
        Transaction withdrawTxn = new Transaction(
                amount,
                LocalDateTime.now(),
                account,
                "Withdrawal"
        );

        transactionRepository.save(withdrawTxn);
    }

    // ================= TRANSACTION HISTORY =================
    public List<Transaction> getTransactionHistory(Account account){
        return transactionRepository.findByAccountId(account.getId());
    }

    // ================= SPRING SECURITY =================
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Account account = findAccountBYUsername(username);

        return new Account(
                account.getUsername(),
                account.getBalance(),
                account.getPassword(),
                account.getTransactions(),
                account.getAuthorities()
        );
    }

    public Collection<? extends GrantedAuthority> authorities(){
        return Arrays.asList(new SimpleGrantedAuthority("user"));
    }

    // ================= TRANSFER =================
    public void transferAmount(Account fromAccount, String toUsername, BigDecimal amount) {

        Account toAccount = accountRepository.findByUsername(toUsername)
                .orElseThrow(() -> new RuntimeException("Recipient account not found"));

        // allow negative balance
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.save(fromAccount);

        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(toAccount);

        Transaction debitTransaction = new Transaction(
                amount,
                LocalDateTime.now(),
                fromAccount,
                "Transfer out to " + toAccount.getUsername()
        );

        Transaction creditTransaction = new Transaction(
                amount,
                LocalDateTime.now(),
                toAccount,
                "Transfer in from " + fromAccount.getUsername()
        );

        transactionRepository.save(debitTransaction);
        transactionRepository.save(creditTransaction);
    }
}