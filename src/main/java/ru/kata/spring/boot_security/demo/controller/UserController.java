package ru.kata.spring.boot_security.demo.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.service.UserServiceImp;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;




@Controller
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


    @RequestMapping(value = "/admin")
    public ModelAndView usersManage(Model model, Authentication authentication) {
        Role[] rolesArr = Role.values();
        model.addAttribute("usersList", userServiceImp.getUsersList());
        model.addAttribute("roles_list", rolesArr);
        model.addAttribute("principal", authentication.getPrincipal());
        return new ModelAndView("admin_page");
    }

    @GetMapping(value = "/admin/add")
    public ModelAndView addUser(Model model, Authentication authentication) {
        model.addAttribute("roles_list", Role.values());
        model.addAttribute("principal", authentication.getPrincipal());
        return new ModelAndView("user_add_page");
    }

    @PostMapping("/admin/add")
    public ModelAndView saveNewUser(User newUser, Model model) {
        try{
            newUser.setEnabled(true);
            userServiceImp.addUser(newUser);
            return new ModelAndView("redirect:/admin");
        } catch (DataIntegrityViolationException ex) {
            return new ModelAndView("redirect:/user_already_exist");
        }
    }


    @PostMapping("/admin/edit")
    public ModelAndView editUser(User editedUser, HttpServletRequest request) {
        editedUser.setEnabled(request.getParameter("isEnabled") != null);
        if (editedUser.getPassword().equals("")) {
            User user = userServiceImp.findUserById(editedUser.getId());
            editedUser.setPassword(user.getPassword());
        }
        userServiceImp.updateUser(editedUser);
        return new ModelAndView("redirect:/admin");
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            request.getSession().invalidate();
        }
        return "redirect:/";
    }


    @RequestMapping("/admin/delete")
    public ModelAndView deleteUser(HttpServletRequest request) {
        userServiceImp.removeUser(Long.parseLong(request.getParameter("deleting-user-id")));
        return new ModelAndView("redirect:/admin");
    }

    @RequestMapping("/user")
    public ModelAndView usersPage(Principal principal, Model model) {
        User user = (User) ((Authentication) principal).getPrincipal();
        model.addAttribute("admin_role", Role.ROLE_ADMIN);
        model.addAttribute("principal", user);
        return new ModelAndView("user_page");
    }

    @RequestMapping("/403")
    public ModelAndView accessDenied() {
        return new ModelAndView("403");
    }

    @RequestMapping("/user_already_exist")
    public ModelAndView usernameAlreadyExist(){
        return new ModelAndView("user_already_exist_page");
    }

    @RequestMapping("/")
    public ModelAndView loginController(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error_text", "Invalid email or password.");
        }
        return new ModelAndView("login_page");
    }
}
