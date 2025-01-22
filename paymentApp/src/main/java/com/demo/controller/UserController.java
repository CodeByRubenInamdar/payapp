package com.demo.controller;

import com.demo.model.BalanceRequest;
import com.demo.model.User;
import com.demo.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	
	@GetMapping("/login")
	public String loginForm(Model model) {
		model.addAttribute("user", new User());
		return "login";
	}

	@PostMapping("/login")
	public String handleLogin(@RequestParam("loginOption") String loginOption,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "phone", required = false) String phoneNumber,
			@RequestParam("password") String password, HttpSession session, Model model) {

		Optional<User> userOptional;

		
		if ("email".equals(loginOption) && email != null && !email.isEmpty()) {
			userOptional = userService.findByEmail(email);
		} else if ("phone".equals(loginOption) && phoneNumber != null && !phoneNumber.isEmpty()) {
			userOptional = userService.findByPhone(phoneNumber);
		} else {
			model.addAttribute("error", "Invalid login option or missing input.");
			return "login";
		}

		
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if (passwordEncoder.matches(password, user.getPassword())) {
				session.setAttribute("loggedInUser", user);
				session.setMaxInactiveInterval(30 * 60); // 30 minutes session timeout
				return "redirect:/";
			} else {
				model.addAttribute("error", "Incorrect password.");
				return "login";
			}
		} else {
			model.addAttribute("error", "User not found.");
			return "login";
		}
	}

	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}

	@PostMapping("/register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("validationErrors", bindingResult.getAllErrors());
			return "register";
		}

		user.setEmail(user.getEmail().toLowerCase());
		if (userService.isEmailAlreadyRegistered(user.getEmail())) {
			model.addAttribute("errorMessage", "Email is already registered.");
			return "register";
		}

		userService.registerUser(user);
		model.addAttribute("successMessage", "User successfully registered!");
		return "redirect:/users/login";
	}

	@GetMapping("/profile")
	public String viewProfile(Model model, HttpSession session) {
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		if (loggedInUser == null) {
			return "redirect:/users/login";
		}
		model.addAttribute("user", loggedInUser);
		return "profile";
	}

	@PostMapping("/profile")
	public String updateProfile(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam("profileImage") MultipartFile profileImage, Model model, HttpSession session) {
		if (result.hasErrors()) {
			return "profile";
		}

		User loggedInUser = (User) session.getAttribute("loggedInUser");
		if (loggedInUser == null) {
			return "redirect:/users/login";
		}

		if (!profileImage.isEmpty()) {
			try {
				
				String uploadDir = "C:\\Category\\img";
				File directory = new File(uploadDir);
				if (!directory.exists()) {
					directory.mkdirs(); 
				}

			
				String filePath = uploadDir + File.separator + profileImage.getOriginalFilename();
				profileImage.transferTo(new File(filePath));

				
				user.setProfileImagePath("/img/" + profileImage.getOriginalFilename());
			} catch (IOException e) {
				e.printStackTrace();
				model.addAttribute("error", "Failed to upload profile image. Please try again.");
				return "profile";
			}
		}

		
		loggedInUser.setName(user.getName());
		loggedInUser.setEmail(user.getEmail());
		loggedInUser.setPhoneNumber(user.getPhoneNumber());
		loggedInUser.setProfileImagePath(user.getProfileImagePath()); 

		
		userService.updateUser(loggedInUser);
		session.setAttribute("loggedInUser", loggedInUser); 

		model.addAttribute("successMessage", "Profile updated successfully.");
		return "redirect:/users/profile";
	}

	@GetMapping("/check-balance")
	public String checkBalanceForm(Model model) {
		model.addAttribute("balanceRequest", new BalanceRequest());
		return "check-balance"; 
	}

	
	@PostMapping("/check-balance")
	public String checkBalance(@ModelAttribute("balanceRequest") BalanceRequest balanceRequest, HttpSession session,
			RedirectAttributes redirectAttributes) {
		User loggedInUser = (User) session.getAttribute("loggedInUser");

		if (loggedInUser == null) {
			return "redirect:/users/login"; 
		}

		Double balance = userService.checkBalance(loggedInUser.getId(), balanceRequest.getPin());

		if (balance != null) {
			redirectAttributes.addFlashAttribute("balance", balance);
			return "redirect:/users/balanceDisplay"; 
		} else {
			redirectAttributes.addFlashAttribute("error", "Incorrect PIN. Please try again.");
			return "redirect:/users/check-balance"; 
		}
	}

	@GetMapping("/balanceDisplay")
	public String displayBalance(Model model) {
		return "balanceDisplay";
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/users/login";
	}

	@GetMapping("/processing")
	public String processingPage(HttpSession session) {
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		boolean isTransactionSuccessful = processTransaction(loggedInUser);
		session.setAttribute("transactionStatus", isTransactionSuccessful ? "success" : "failed");

		return "processing";
	}

	private boolean processTransaction(User user) {
		boolean isValidTransaction = true;
		if (isValidTransaction) {
			return true;
		} else {
			return false;
		}
	}

	@GetMapping("/forgot-password")
	public String forgotPasswordPage(HttpSession session, Model model) {
		int captcha = (int) (Math.random() * 9000) + 1000; 
		session.setAttribute("captcha", captcha);
		model.addAttribute("captcha", captcha); 
		return "forgot-password";
	}

	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String email, @RequestParam String captchaInput, HttpSession session,
			Model model) {
		Integer sessionCaptcha = (Integer) session.getAttribute("captcha");

		if (sessionCaptcha == null || !sessionCaptcha.toString().equals(captchaInput)) {
			model.addAttribute("error", "Invalid CAPTCHA. Please try again.");
			return "forgot-password";
		}

		boolean isUserExists = userService.sendPasswordResetLink(email);

		if (isUserExists) {
			model.addAttribute("message", "A password reset link has been sent to your email.");
		} else {
			model.addAttribute("error", "No user found with the provided email.");
		}

		return "forgot-password";
	}

		@GetMapping("/reset-password/{token}")
	public String resetPasswordForm(@PathVariable String token, Model model) {
		boolean isTokenValid = userService.verifyResetToken(token);

		if (isTokenValid) {
			model.addAttribute("token", token); 
			return "reset-password"; 
		} else {
			model.addAttribute("error", "Invalid or expired reset token.");
			return "login";
		}
	}

	@PostMapping("/update-password")
	public String updatePassword(@RequestParam String password, @RequestParam String token, Model model) {
		boolean isUpdated = userService.updatePassword(password, token);

		if (isUpdated) {
			model.addAttribute("message", "Your password has been updated successfully.");
			return "login"; 
		} else {
			model.addAttribute("error", "Failed to update password.");
			return "reset-password";
		}
	}

}
