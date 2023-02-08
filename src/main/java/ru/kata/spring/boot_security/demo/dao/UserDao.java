package ru.kata.spring.boot_security.demo.dao;

import ru.kata.spring.boot_security.demo.entity.User;

import java.util.List;

public interface UserDao {
   List<User> getUsersList();
   void addUser(User user);
   void removeUser(Long id);
   User findUserByUsername(String email);
   User findUserById(Long id);
   void updateUser(User user);
}
