package ru.kata.spring.boot_security.demo.dao;

import org.springframework.stereotype.Repository;
import ru.kata.spring.boot_security.demo.entity.User;

import javax.persistence.*;
import java.util.List;

@Repository
public class UserDaoImp implements UserDao {

   @PersistenceContext
   private EntityManager em;

   @Override
   public List<User> getUsersList() {
      Query query = em.createQuery("SELECT u FROM User u");
      return query.getResultList();
   }

   @Override
   public void addUser(User user) {
      em.persist(user);
   }

   @Override
   public void removeUser(Long id) {
      User user = em.find(User.class, id);
      em.remove(user);
   }

   @Override
   public User findUserByUsername(String email) {
      TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
      query.setParameter("email", email);
      try {
         return query.getSingleResult();
      } catch (NoResultException e) {
         return null;
      }
   }

   @Override
   public User findUserById(Long id) {
      return em.find(User.class, id);
   }


   @Override
   public void updateUser(User user) {
      em.merge(user);
   }
}
