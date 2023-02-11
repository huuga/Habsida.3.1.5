package ru.kata.spring.boot_security.demo.controller;

import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.service.UserServiceImp;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;


@RestController
public class UserController {

    private final UserServiceImp userServiceImp;

    public UserController(UserServiceImp userServiceImp) {
        this.userServiceImp = userServiceImp;
    }

    @RequestMapping(value = "/start")
    public ModelAndView start() {
        User root = new User("Николай", "Петров", (byte) 30, "root",
                "root@mail.ru", Role.ROLE_ADMIN, Role.ROLE_USER);
        User user = new User("Семен", "Туполев", (byte) 30, "user",
                "user@mail.ru", Role.ROLE_USER);

        userServiceImp.addUser(root);
        userServiceImp.addUser(user);

        return new ModelAndView("start_page");
    }


    @GetMapping(value = "/admin")
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


    @PostMapping("/admin")
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


    @PatchMapping("/admin")
    public User editUser(@RequestBody User editedUser) {
        if (editedUser.getPassword().equals("")) {
            User user = userServiceImp.findUserById(editedUser.getId());
            editedUser.setPassword(user.getPassword());
        }
        userServiceImp.updateUser(editedUser);
        return userServiceImp.findUserById(editedUser.getId());
    }

    @GetMapping("/logout")
    public ModelAndView logout(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            request.getSession().invalidate();
        }
        return new ModelAndView("redirect:/");
    }


    @DeleteMapping("/admin")
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

    @GetMapping("/user")
    public ModelAndView usersPage(Principal principal, Model model) {
        User user = (User) ((Authentication) principal).getPrincipal();
        model.addAttribute("admin_role", Role.ROLE_ADMIN);
        model.addAttribute("principal", user);
        return new ModelAndView("user_page");
    }


    @GetMapping("/")
    public ModelAndView loginController(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error_text", "Invalid email or password.");
        }
        return new ModelAndView("login_page");
    }

    @GetMapping("/403")
    public ModelAndView accessDenied() {
        return new ModelAndView("403");
    }
}
