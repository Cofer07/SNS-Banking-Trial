package com.example.snsbankingrestapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping("")
    public String viewHomePage(){
        return "index";
    }
    
    @GetMapping("/login")
    public String login() {
    	return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        model.addAttribute("user", new User());

        return "signup_form";
    }

    @PostMapping("/process_register")
    public String processRegister(User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userRepo.save(user);

        return "register_success";
    }
    
    @GetMapping("/dashboard")
    public String prepareDashboard(Model model, Authentication authentication) {
    	CustomerUserDetails customerUserDetails = (CustomerUserDetails) authentication.getPrincipal();
    	// here you can grab the details from the authenticated user
    	model.addAttribute("userFullName", customerUserDetails.getFullName());
    	model.addAttribute("userId", customerUserDetails.getUserId());
    	return "bank";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> listUsers = userRepo.findAll();
        model.addAttribute("listUsers", listUsers);

        return "users";
    }

    @GetMapping("/bank")
    public String Bank(){
        return "bank";
    }


    @Autowired // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;


    @PostMapping(path="/add") // Map ONLY POST Requests
    public @ResponseBody String addNewUser (@RequestParam String name
            , @RequestParam String email) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request

        User n = new User();
        n.setName(name);
        n.setEmail(email);
        userRepository.save(n);
        return "Saved";
    }

    @PostMapping(path="/make-transaction") // Map ONLY POST Requests
    public @ResponseBody String addTransaction (@RequestParam Integer from
            , @RequestParam Integer to, @RequestParam Double amount) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request

        Optional<Account> receivingOptional = accountRepository.findById(to);
        Optional<Account> sendingOptional = accountRepository.findById(from);
        Account receivingAccount = receivingOptional.stream().findFirst().orElse(null);
        Account sendingAccount = sendingOptional.stream().findFirst().orElse(null);

        if(receivingAccount != null && sendingAccount != null){
            Transaction t = new Transaction(sendingAccount, receivingAccount, amount);
            receivingAccount.addReceivedTransaction(t);
            sendingAccount.addSentTransaction(t);

            receivingAccount.setBalance(receivingAccount.getBalance() + amount);
            sendingAccount.setBalance(sendingAccount.getBalance() - amount);

            accountRepository.save(receivingAccount);
            accountRepository.save(sendingAccount);
            transactionRepository.save(t);
            return "Saved";
        }
        else{
            if(receivingAccount == null && sendingAccount == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Origin account and receiving account do not exist.");
            }
            if(receivingAccount == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recipient account does not exist.");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Origin account does not exist.");
        }
    }

    @PostMapping(path="/add_funds") // Map ONLY POST Requests
    public @ResponseBody String addFunds (@RequestParam Integer destination, @RequestParam Double amount) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request

        Optional<Account> receivingOptional = accountRepository.findById(destination);
        Account receivingAccount = receivingOptional.stream().findFirst().orElse(null);

        if(receivingAccount != null){
            receivingAccount.setBalance(receivingAccount.getBalance() + amount);

            accountRepository.save(receivingAccount);
            return "Saved";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recipient account does not exist.");
    }

    @PostMapping(path ="/add-account")
    public @ResponseBody String addAccount (@RequestParam Integer user_id, @RequestParam String type){
        Optional<User> parent = userRepository.findById(user_id);
        User user = parent.stream().findFirst().orElse(null);
        if (user != null) {
            Account a = new Account(user);
            a.setType(type);
            a.setBalance(1000.00);
            user.addAccount(a);
            accountRepository.save(a);
            return "Success";
        }
        else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with that ID does not exist.");
        }
    }

    @GetMapping(path="/get-user")
    public @ResponseBody Map<String, String> getUser(@RequestParam int id){
        Optional<User> parent = userRepository.findById(id);
        User user = parent.stream().findFirst().orElse(null);
        if (user != null) {
            Map<String, String> map = new HashMap<>();
            map.put("ID", String.valueOf(user.getUserid()));
            map.put("FirstName", user.getFname());
            map.put("LastName", user.getLname());
            map.put("Email", user.getEmail());
            map.put("PhoneNumber", user.getPhoneNum());
            return map;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with that ID does not exist.");
    }

    @GetMapping(path="/all-transactions")
    public @ResponseBody Map<String, List<Transaction>> getAllTransactions(@RequestParam int accountid) {
        // This returns a JSON or XML with the transactions
        Optional<Account> optionalAccount = accountRepository.findById(accountid);
        Account account = optionalAccount.stream().findFirst().orElse(null);
        if (account != null){
            Map<String, List<Transaction>> map = new HashMap<>();
            map.put("Received", account.getReceived_transactions());
            map.put("Sent", account.getSent_transactions());
            return map;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account with that ID does not exist.");
    }

    @GetMapping(path="/recent-transactions")
    public @ResponseBody Map<String, List<Transaction>> getRecentTransactions(@RequestParam int accountid, @RequestParam int limit) {
        // This returns a JSON or XML with the transactions
        Optional<Account> optionalAccount = accountRepository.findById(accountid);
        Account account = optionalAccount.stream().findFirst().orElse(null);
        if (account != null){
            Map<String, List<Transaction>> map = new HashMap<>();
            List<Transaction> sent = account.getSent_transactions();
            List<Transaction> received = account.getReceived_transactions();
            List<Transaction> combined = account.getSent_transactions();
            combined.addAll(received);

            sent.sort(Comparator.comparing(Transaction::getDate));
            received.sort(Comparator.comparing(Transaction::getDate));
            combined.sort(Comparator.comparing(Transaction::getDate));

            map.put("Received", received.subList(0, Math.min(limit, received.size())));
            map.put("Sent", sent.subList(0, Math.min(limit, sent.size())));
            map.put("Combined", combined.subList(0, Math.min(limit, combined.size())));
            return map;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account with that ID does not exist.");
    }

    @GetMapping(path="/all")
    public @ResponseBody Iterable<User> getAllUsers() {
        // This returns a JSON or XML with the users
        return userRepository.findAll();
    }
}
