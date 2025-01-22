package com.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.demo.model.Notification;
import com.demo.model.User;
import com.demo.service.NotificationService;

import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/notifications")
public class NotificationController {
	
	@Autowired
	private NotificationService notificationService;
	
	
	  @GetMapping("/")
	    public String getNotifications(Model model, HttpSession session) {
	        User loggedInUser = (User) session.getAttribute("loggedInUser");
	        if (loggedInUser == null) {
	            return "redirect:/users/login";
	        }

	        List<Notification> notifications = notificationService.getUnreadNotifications(loggedInUser);
	        int notificationCount = notifications.size(); 
	        model.addAttribute("notifications", notifications);
	        model.addAttribute("notificationCount", notificationCount); 

	        
	        session.setAttribute("notificationCount", notificationCount);

	        return "notifications"; 
	    }
	
	@PostMapping("/read")
	public String markNotificationsAsRead(HttpSession session) {
	    User loggedInUser = (User) session.getAttribute("loggedInUser");
	    if (loggedInUser != null) {
	        notificationService.markAllAsRead(loggedInUser);
	        session.setAttribute("notificationCount", 0);
	    }
	    return "redirect:/transactions/history";
	}



}
