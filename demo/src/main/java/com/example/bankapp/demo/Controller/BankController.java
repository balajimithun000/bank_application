package com.example.bankapp.demo.Controller;
import java.util.List;

import com.example.bankapp.demo.model.Transaction;
import org.hibernate.dialect.unique.CreateTableUniqueDelegate;
import org.springframework.ui.Model;
import com.example.bankapp.demo.model.Account;
import com.example.bankapp.demo.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
@Controller
public class BankController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/dashboard")
    public String dashboard(Model model){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountBYUsername(username);
        model.addAttribute("account", account);
        return "dashboard";
    }

    // ================= REGISTER =================
    @GetMapping("/register")
    public String showRegistrationForm(){
        return "register";
    }

    @PostMapping("/register")
    public String registerAccount(@RequestParam String username,
                                  @RequestParam String password,
                                  Model model){
        try{
            accountService.registerAccount(username, password);
            return "redirect:/login";
        }catch(RuntimeException e){
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    // ================= DEPOSIT (LIVE) =================
    @PostMapping("/deposit")
    @ResponseBody
    public String deposit(@RequestParam BigDecimal amount){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountBYUsername(username);

        accountService.deposit(account, amount);

        return "success";
    }

    // ================= WITHDRAW (LIVE) =================
    @PostMapping("/withdraw")
    @ResponseBody
    public String withdraw(@RequestParam BigDecimal amount){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountBYUsername(username);

        accountService.withdraw(account, amount);

        return "success";
    }

    // ================= TRANSFER (LIVE) =================
    @PostMapping("/transfer")
    @ResponseBody
    public String transferAmount(@RequestParam String toUsername,
                                 @RequestParam BigDecimal amount){

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account fromAccount = accountService.findAccountBYUsername(username);

        accountService.transferAmount(fromAccount, toUsername, amount);

        return "success";
    }

    // ================= LIVE BALANCE API =================
    @GetMapping("/dashboard/account")
    @ResponseBody
    public Account getAccount(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountService.findAccountBYUsername(username);
    }

    // ================= TRANSACTION =================
    @GetMapping("/transaction")
    public String transactionHistory(Model model){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountBYUsername(username);
        List<Transaction> transactions = accountService.getTransactionHistory(account);
        model.addAttribute("transactions", transactions);
        return "transaction";
    }
}