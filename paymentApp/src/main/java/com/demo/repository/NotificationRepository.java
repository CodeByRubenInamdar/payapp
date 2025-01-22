package com.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.demo.model.Notification;
import com.demo.model.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
	@Query("SELECT n FROM Notification n WHERE n.user = :user AND n.isRead = false ORDER BY n.id DESC")
    List<Notification> findUnreadNotificationsByUserOrderByHighestId(User user);

	List<Notification> findByUserAndIsReadFalse(User user);
}
