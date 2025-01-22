package com.demo.controller;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.demo.model.Transaction;
import com.demo.model.TransactionStatus;
import com.demo.model.TransactionType;
import com.demo.model.User;
import com.demo.service.NotificationService;
import com.demo.service.TransactionService;
import com.demo.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping("/send")
    public String sendMoneyPage(Model model, HttpSession session) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            System.out.println("User not found in session!");  
            return "redirect:/users/login"; 
        }

        
        if (loggedInUser != null) {
            System.out.println("Logged-in user: " + loggedInUser.getName() + ", " + loggedInUser.getPhoneNumber());  // Debug log
            model.addAttribute("senderName", loggedInUser.getName());
            model.addAttribute("senderPhoneNumber", loggedInUser.getPhoneNumber());
        } else {
            model.addAttribute("status", "Could not fetch user details.");
        }

        
        List<User> users = transactionService.findAllUsers();
        model.addAttribute("users", users);

        return "send-money";
    }

    @PostMapping("/send")
    public String sendMoney(@RequestParam String senderPhoneNumber,
                            @RequestParam String receiverPhoneNumber,
                            @RequestParam Double amount,
                            Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/users/login"; // Redirect to login if session is not active
        }

        try {
            User sender = userService.findByPhoneNumber(senderPhoneNumber)
                    .orElseThrow(() -> new RuntimeException("Sender not found"));
            User receiver = userService.findByPhoneNumber(receiverPhoneNumber)
                    .orElseThrow(() -> new RuntimeException("Receiver not found"));

            if (sender.getWalletBalance() < amount) {
                session.setAttribute("status", "Insufficient funds.");
                notificationService.createNotification(
                        "Transaction Failed",
                        "Failed to send ₹" + amount + " to " + receiverPhoneNumber,
                        sender
                    );
                return "redirect:/transactions/failed";
            }

        
            userService.transferMoney(senderPhoneNumber, receiverPhoneNumber, amount);

          
            Transaction sentTransaction = new Transaction();
            sentTransaction.setSender(sender);
            sentTransaction.setReceiver(receiver);
            sentTransaction.setAmount(amount);
            sentTransaction.setTransactionType(TransactionType.SENT);
            sentTransaction.setTransactionStatus(TransactionStatus.SUCCESS);
            sentTransaction.setTransactionTime(LocalDateTime.now());
            transactionService.saveTransaction(sentTransaction);

          
            Transaction receivedTransaction = new Transaction();
            receivedTransaction.setSender(sender);
            receivedTransaction.setReceiver(receiver);
            receivedTransaction.setAmount(amount);
            receivedTransaction.setTransactionType(TransactionType.RECEIVED);
            receivedTransaction.setTransactionStatus(TransactionStatus.SUCCESS);
            receivedTransaction.setTransactionTime(LocalDateTime.now());
            transactionService.saveTransaction(receivedTransaction);

            
            sender.setTransactionType(TransactionType.SENT); 
            receiver.setTransactionType(TransactionType.RECEIVED); 
            userService.save(sender);
            userService.save(receiver);

            session.setAttribute("senderName", sender.getName());
            session.setAttribute("receiverName", receiver.getName());
            session.setAttribute("amount", amount);
            
            notificationService.createNotification(
                    "Transaction Successful",
                    "You sent ₹" + amount + " to " + receiver.getName(),
                    sender
                );
                notificationService.createNotification(
                    "Money Received",
                    "You received ₹" + amount + " from " + sender.getName(),
                    receiver
                );
            return "redirect:/transactions/success";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("status", "An error occurred while processing your request. Please try again later.");
            return "send-money";
        }
    }
    
    @GetMapping("/failed")
    public String transactionFailed(Model model, HttpSession session) {
       
        String senderName = (String) session.getAttribute("senderName");
        String receiverName = (String) session.getAttribute("receiverName");
        Double amount = (Double) session.getAttribute("amount");
        String status = (String) session.getAttribute("status");

       
        System.out.println("Sender Name: " + senderName);
        System.out.println("Receiver Name: " + receiverName);
        System.out.println("Amount: " + amount);
        System.out.println("Status: " + status);

     
        model.addAttribute("senderName", senderName != null ? senderName : "N/A");
        model.addAttribute("receiverName", receiverName != null ? receiverName : "N/A");
        model.addAttribute("amount", amount != null ? amount : 0.0);
        model.addAttribute("status", status != null ? status : "Transaction failed.");

        return "failed"; 
    }

    @GetMapping("/history")
    public String userTransactionHistory(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/users/login"; 
        }

    
        List<Transaction> transactions = transactionService.findTransactionsByUserId(loggedInUser.getId());
        model.addAttribute("transactions", transactions);
        List<Transaction> sortedTransactions = transactionService.getSortedTransactions(transactions);
        model.addAttribute("transactions", sortedTransactions);
        
        List<Transaction> transactions2 = transactionService.getTransactionsByUser(loggedInUser.getId());
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("transactions", transactions2);
        return "transactions"; 
        
       
 

    }

    
    @GetMapping("/getPhoneNumber")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getPhoneNumber(@RequestParam String name, Model model, HttpSession session) {
      
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
        }

       
        User user = userService.findByUsername(name);

        
        List<User> users = transactionService.findAllUsers();

        
        users.removeIf(u -> u.getName().equals(loggedInUser.getName()));

        
        model.addAttribute("users", users);

        Map<String, String> response = new HashMap<>();

        if (user != null) {
            response.put("phoneNumber", user.getPhoneNumber());
        } else {
            response.put("phoneNumber", "");  
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verifyPin")
    public ResponseEntity<Map<String, Object>> verifyPin(@RequestParam String pin) {
        Map<String, Object> response = new HashMap<>();
        String storedPin = "1234"; 
        if (storedPin.equals(pin)) {
            response.put("valid", true);
        } else {
            response.put("valid", false);
        }

        return ResponseEntity.ok(response);
    }



    @GetMapping("/enter-pin") 
    public String enterPinPage(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/users/login"; 
        }

        model.addAttribute("email", loggedInUser.getEmail());
        return "enter-pin"; 
    }

//    @PostMapping("/enter-pin")
//    public String processPin(@RequestParam String pin, 
//                             @RequestParam String email, 
//                             HttpSession session, Model model) {
//        boolean isValid = userService.verifyUserPin(email, pin);
//
//        if (isValid) {
//            // Add any logic to proceed with a successful PIN verification
//            session.setAttribute("pinVerified", true);
//            return "redirect:/transactions/success"; // Redirect to send money page
//        } else {
//            model.addAttribute("error", "Invalid PIN. Please try again.");
//            return "enter-pin"; // Reload the PIN entry page with an error
//        }
//    }
    
    @GetMapping("/success")
    public String transactionDone(Model model, HttpSession session) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/users/login"; 
            }

        String senderName = (String) session.getAttribute("senderName");
        String receiverName = (String) session.getAttribute("receiverName");
        Double amount = (Double) session.getAttribute("amount");

        model.addAttribute("senderName", senderName != null ? senderName : "N/A");
        model.addAttribute("receiverName", receiverName != null ? receiverName : "N/A");
        model.addAttribute("amount", amount != null ? amount : 0.0);
      

        return "success";
    }
    
    
}
