package com.demo.service;

import com.demo.model.User;
import com.demo.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	
	  @Autowired
	  private JavaMailSender javaMailSender;
 
	public User registerUser(User user) {
		if (userRepository.existsByEmail(user.getEmail())) {
			throw new RuntimeException("Email is already registered.");
		}
		if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
			throw new RuntimeException("Phone number is already registered.");
		}
		// Encrypt the password before saving
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}

	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public boolean isEmailAlreadyRegistered(String email) {
		return userRepository.findByEmail(email).isPresent();
	}

	public Optional<User> findByPhoneNumber(String phoneNumber) {
		return userRepository.findByPhoneNumber(phoneNumber);
	}

	public void transferMoney(String senderPhoneNumber, String receiverPhoneNumber, Double amount) {
		User sender = userRepository.findByPhoneNumber(senderPhoneNumber)
				.orElseThrow(() -> new RuntimeException("Sender not found"));
		User receiver = userRepository.findByPhoneNumber(receiverPhoneNumber)
				.orElseThrow(() -> new RuntimeException("Receiver not found"));

		if (sender.getWalletBalance() < amount) {
			throw new RuntimeException("Insufficient funds");
		}

		sender.setWalletBalance(sender.getWalletBalance() - amount);
		receiver.setWalletBalance(receiver.getWalletBalance() + amount);

		userRepository.save(sender);
		userRepository.save(receiver);
	}

	public User getUserById(Long userId) {
		return userRepository.findById(userId).orElse(null);
	}

	public String getPhoneNumberByName(String name) {
		return userRepository.findByName(name).getPhoneNumber();
	}

	public void updateUser(User user) {
		userRepository.save(user);
	}

	public void save(User user) {
		userRepository.save(user);
	}

	public User findByUsername(String username) {
		return userRepository.findByName(username);
	}

	public Optional<User> findByPhone(String phoneNumber) {
		return userRepository.findByPhoneNumber(phoneNumber);
	}
	
	 public Double checkBalance(Long userId, int pin) {
	        Optional<User> userOptional = userRepository.findById(userId);
	        if (userOptional.isPresent()) {
	            User user = userOptional.get();
	            if (user.getPin() == pin) {
	                return user.getWalletBalance(); // Return balance if PIN matches
	            }
	        }
	        return null; // Return null if PIN is incorrect or user not found
	    }
	 
	 public boolean sendPasswordResetLink(String email) {
	        Optional<User> user = userRepository.findByEmail(email);

	        if (user.isPresent()) {
	            String token = generatePasswordResetToken(); // Generate a reset token
	            user.get().setResetToken(token); // Save the token to the user
	            userRepository.save(user.get()); // Save token to the database

	            sendEmail(email, token); // Send the reset email
	            return true;
	        }
	        return false;
	    }

	    // Verify the Reset Token
	    public boolean verifyResetToken(String token) {
	        Optional<User> user = userRepository.findByResetToken(token);
	        return user.isPresent();
	    }

	    // Update User's Password
	    public boolean updatePassword(String password, String token) {
	        Optional<User> user = userRepository.findByResetToken(token);

	        if (user.isPresent()) {
	            User existingUser = user.get();
	            existingUser.setPassword(passwordEncoder.encode(password)); // Hash password before saving
	            existingUser.setResetToken(null); // Clear the reset token after the password is updated
	            userRepository.save(existingUser);
	            return true;
	        }
	        return false;
	    }

	    // Utility Methods
	    private void sendEmail(String email, String token) {
	        String resetLink = "http://localhost:8080/users/reset-password/" + token;

	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setTo(email);
	        message.setSubject("Password Reset Request");
	        message.setText("You requested a password reset. Click the link below to reset your password:\n" + resetLink);

	        javaMailSender.send(message); // Send the email
	    }

	    private String generatePasswordResetToken() {
	        return UUID.randomUUID().toString(); // Generate a unique token
	    }

	    public void sendEmail(String to, String subject, String text) {
	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setTo(to);
	        message.setSubject(subject);
	        message.setText(text);
	        javaMailSender.send(message);
	    }
}
