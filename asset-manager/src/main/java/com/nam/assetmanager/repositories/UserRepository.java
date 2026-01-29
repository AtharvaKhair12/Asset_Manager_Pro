package com.nam.assetmanager.repositories;
import com.nam.assetmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}