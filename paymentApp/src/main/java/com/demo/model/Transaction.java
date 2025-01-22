package com.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    private Double amount;

    private LocalDateTime transactionTime;

    private String description;

    @Transient
    private String senderName;

    @Transient
    private String receiverName;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @PostLoad
    public void populateNames() {
        if (sender != null) {
            this.senderName = sender.getName();
        }
        if (receiver != null) {
            this.receiverName = receiver.getName();
        }
    }
    public Transaction(User sender, User receiver, Double amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.transactionTime = LocalDateTime.now(); 
    }
	

}
