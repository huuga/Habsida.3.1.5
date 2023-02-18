package ru.kata.spring.boot_security.demo.controller;

import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.service.UserServiceImp;

import java.util.LinkedHashMap;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    final
    UserServiceImp userServiceImp;

    public AdminController(UserServiceImp userServiceImp) {
        this.userServiceImp = userServiceImp;
    }

    @GetMapping()
    public ModelAndView usersManage(Model model, Authentication authentication) {
        Role[] rolesArr = Role.values();
        model.addAttribute("roles_list", rolesArr);
        model.addAttribute("principal", authentication.getPrincipal());
        return new ModelAndView("admin_page");
    }

    @GetMapping(value = "/getusers")
    public List<User> getUsers() {
        return userServiceImp.getUsersList();
    }


    @PostMapping()
    public ResponseEntity saveNewUser(@RequestBody User newUser) {
        try{
            newUser.setEnabled(true);
            userServiceImp.addUser(newUser);
            ResponseEntity<User> response = new ResponseEntity<>(userServiceImp.findUserByUsername(newUser.getEmail()), HttpStatus.CREATED);
            return response;
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }


    @PatchMapping()
    public User editUser(@RequestBody User editedUser) {
        if (editedUser.getPassword().equals("")) {
            User user = userServiceImp.findUserById(editedUser.getId());
            editedUser.setPassword(user.getPassword());
        }
        userServiceImp.updateUser(editedUser);
        return userServiceImp.findUserById(editedUser.getId());
    }

    @DeleteMapping()
    public ResponseEntity deleteUser(@RequestBody String id) throws ParseException {
        LinkedHashMap<String, Object> jsonObject = new JSONParser(id).parseObject();
        String strId = (String) jsonObject.get("id");
        try {
            userServiceImp.removeUser(Long.parseLong(strId));
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
