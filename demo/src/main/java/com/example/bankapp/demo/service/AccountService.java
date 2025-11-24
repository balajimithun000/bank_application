package com.example.bankapp.demo.service;

import com.example.bankapp.demo.model.Account;
import com.example.bankapp.demo.model.Transaction;
import com.example.bankapp.demo.repository.AccountRepository;
import com.example.bankapp.demo.repository.TransactionRepository;
import org.hibernate.mapping.Array;
import java.util.Collection;
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
import java.util.List;

@Service
public class AccountService implements UserDetailsService {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    public Account findAccountBYUsername(String username){
        return  accountRepository.findByUsername(username)
                .orElseThrow(()  ->new RuntimeException("Account not found"));
    }

    public Account registerAccount(String username, String password){
        if(accountRepository.findByUsername(username).isPresent()){
            throw  new RuntimeException("username already exists");
        }

        Account account=new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setBalance(BigDecimal.ZERO);
        return  accountRepository.save(account);
    }

    public  void deposit(Account account, BigDecimal amount){
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

    public void withdraw( Account account,BigDecimal amount){
        if(account.getBalance().compareTo(amount)< 0){
            throw new RuntimeException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        Transaction transaction = new Transaction(
                amount,
                LocalDateTime.now(),
                account,
                "withdrawal"
        );
        transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionHistory(Account account){
        return  transactionRepository.findByAccountId(account.getId());
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{

        Account account=findAccountBYUsername(username);
        if(account==null){
            throw  new UsernameNotFoundException("username or password not found");

        }
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
    public void transferAmount(Account fromAccount, String toUsername, BigDecimal amount) {
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        Account toAccount = accountRepository.findByUsername(toUsername)
                .orElseThrow(() -> new RuntimeException("Recipient account not found"));

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
