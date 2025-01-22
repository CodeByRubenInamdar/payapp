package com.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.demo.model.Transaction;
import com.demo.model.User;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderId(Long senderId);

    List<Transaction> findBySenderOrReceiver(User sender, User receiver);
    List<Transaction> findBySenderPhoneNumber(String senderPhoneNumber);
    List<Transaction> findByReceiverPhoneNumber(String receiverPhoneNumber);

	List<Transaction> findByReceiverId(Long userId);

	List<Transaction> findBySender_IdOrReceiver_Id(Long userId, Long userId2);
   
}
