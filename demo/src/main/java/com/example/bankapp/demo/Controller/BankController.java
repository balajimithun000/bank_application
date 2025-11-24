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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;

@Controller
public class BankController {
    @Autowired
    private AccountService accountService;

    @GetMapping("/dashboard")
    public String dashboard(Model model){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account=accountService.findAccountBYUsername(username);
        model.addAttribute("account",account);
        return "dashboard";
    }
    @GetMapping("/register")

    public String showRegistrationForm(){
        return "register";
    }

    @PostMapping("/register")
    public String registerAccount(@RequestParam String username,@RequestParam String password,Model model){
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

    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount, Model model){
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        Account account=accountService.findAccountBYUsername(username);
        try {
            accountService.deposit(account, amount);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("account", account);
            return "dashboard";
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount,Model model){
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        Account account=accountService.findAccountBYUsername(username);

        try{
            accountService.withdraw(account,amount);
        }catch (RuntimeException e){
            model.addAttribute("error",e.getMessage());
            model.addAttribute("account",account);
            return "dashboard";
        }
        return "redirect:/dashboard";
    }
    @GetMapping("/transaction")
    public String transactionHistory(Model model){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountBYUsername(username);
        List<Transaction> transactions = accountService.getTransactionHistory(account);
        model.addAttribute("transactions", transactions);
        return "transaction";
    }

    @PostMapping("/transfer")
        public String transferAmount(@RequestParam String toUsername,@RequestParam BigDecimal amount,Model model) {

       String username = SecurityContextHolder.getContext().getAuthentication().getName();
       Account fromAccount = accountService.findAccountBYUsername(username);
       try {
           accountService.transferAmount(fromAccount, toUsername, amount);
       } catch (RuntimeException e) {
           model.addAttribute("error", e.getMessage());
           model.addAttribute("account", fromAccount);
           return "dashboard";
       }

       return "redirect:/dashboard";
   }

}
