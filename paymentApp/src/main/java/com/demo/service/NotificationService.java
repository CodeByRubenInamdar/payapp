package com.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demo.model.Notification;
import com.demo.model.User;
import com.demo.repository.NotificationRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void createNotification(String title, String message, User user) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setUser(user);
        notification.setTimestamp(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findUnreadNotificationsByUserOrderByHighestId(user);
    }

    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findUnreadNotificationsByUserOrderByHighestId(user);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }
    
}
