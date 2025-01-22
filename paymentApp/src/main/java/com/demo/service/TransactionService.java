package com.demo.service;

import com.demo.model.Transaction;
import com.demo.model.TransactionStatus;
import com.demo.model.User;
import com.demo.repository.TransactionRepository;
import com.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	public String transferMoney(String senderPhoneNumber, String receiverPhoneNumber, Double amount) {
		if (senderPhoneNumber.equals(receiverPhoneNumber)) {
			return "Sender and receiver cannot be the same";
		}

		Optional<User> senderOpt = userRepository.findByPhoneNumber(senderPhoneNumber);
		Optional<User> receiverOpt = userRepository.findByPhoneNumber(receiverPhoneNumber);

		if (!senderOpt.isPresent()) {
			return "Sender does not exist";
		}
		if (!receiverOpt.isPresent()) {
			return "Receiver does not exist";
		}

		User senderUser = senderOpt.get();
		User receiverUser = receiverOpt.get();

		if (senderUser.getWalletBalance() < amount) {
			return "Insufficient balance";
		}

		
		senderUser.setWalletBalance(senderUser.getWalletBalance() - amount);
		receiverUser.setWalletBalance(receiverUser.getWalletBalance() + amount);

		
		userRepository.save(senderUser);
		userRepository.save(receiverUser);

		
		Transaction transaction = new Transaction();
		transaction.setSender(senderUser);
		transaction.setReceiver(receiverUser);
		transaction.setAmount(amount);
		transaction.setTransactionTime(LocalDateTime.now());
		transaction.setDescription("Money transfer from " + senderUser.getName() + " to " + receiverUser.getName());
		transactionRepository.save(transaction);

		return "Transaction successful";
	}

	public List<Transaction> getTransactionHistory(String phoneNumber) {
		List<Transaction> senderTransactions = transactionRepository.findBySenderPhoneNumber(phoneNumber);
		List<Transaction> receiverTransactions = transactionRepository.findByReceiverPhoneNumber(phoneNumber);
		List<Transaction> allTransactions = new ArrayList<>();
		allTransactions.addAll(senderTransactions);
		allTransactions.addAll(receiverTransactions);
		return allTransactions;
	}

	public List<User> findAllUsers() {
		return userRepository.findAll();
	}

	public List<User> findAllByPhoneNumber(String phoneNumber) {
		return userRepository.findByPhoneNumberContaining(phoneNumber);
	}

	public List<User> findAllByName(String name) {
		return userRepository.findByNameContainingIgnoreCase(name);
	}

	public List<Transaction> findAllTransactions() {
		return transactionRepository.findAll();
	}

	public List<Transaction> getTransactionsByUser(User user) {
		return transactionRepository.findBySenderOrReceiver(user, user);
	}

	public void save(Transaction transaction) {
		transactionRepository.save(transaction);
	}

	public List<Transaction> findTransactionsByUserId(Long userId) {
	    List<Transaction> sentTransactions = transactionRepository.findBySenderId(userId);
	    List<Transaction> receivedTransactions = transactionRepository.findByReceiverId(userId);

	   
	    sentTransactions.addAll(receivedTransactions);
	    return sentTransactions;
	}


	public void saveTransaction(Transaction transaction) {
	    if (transaction.getTransactionTime() == null) {
	        transaction.setTransactionTime(LocalDateTime.now());
	    }
	    transactionRepository.save(transaction);
	}


	public void saveFailedTransaction(User sender, User receiver, Double amount) {
		Transaction transaction = new Transaction(sender, receiver, amount);
		transaction.setTransactionStatus(TransactionStatus.FAILED);
		transaction.setTransactionTime(LocalDateTime.now());
		transactionRepository.save(transaction);
	}

	public List<Transaction> getSortedTransactions(List<Transaction> transactions) {
	    transactions.sort(Comparator.comparing(
	        Transaction::getTransactionTime, 
	        Comparator.nullsLast(Comparator.naturalOrder())
	    ).reversed());
	    return transactions;
	}

	public List<Transaction> findBySender(Long sender) {
	    return transactionRepository.findBySenderId(sender);
	}

	public List<Transaction> findByReceiver(Long receiver) {
	    return transactionRepository.findByReceiverId(receiver);
	}

	public List<Transaction> getTransactionsByUser(Long userId) {
    return transactionRepository.findBySender_IdOrReceiver_Id(userId, userId);
}

	



	
}
