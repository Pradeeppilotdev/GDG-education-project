package bigprojects.controller;

import bigprojects.service.UserService;
import bigprojects.service.EmailService;
import bigprojects.dto.LoginRequest;
import bigprojects.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // ➤ Show Signup Page
    @GetMapping("/signup")
    public String showSignupPage(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    // ➤ Signup Logic
    @PostMapping("/signup")
    public String signup(@ModelAttribute User user, Model model) {
        if (userService.isEmailRegistered(user.getEmail())) {
            model.addAttribute("error", "Email is already registered.");
            return "signup";
        }
        if (!user.getPassword().equals(user.getConfirmPassword())) {
            model.addAttribute("error", "Password and Confirm Password do not match.");
            return "signup";
        }
        userService.signup(user);
        return "redirect:/auth/send?email=" + user.getEmail();
    }

    // ➤ Show Login Page
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    // ➤ Login Logic
    @PostMapping("/login")
    public String login(@ModelAttribute("loginRequest") LoginRequest loginRequest, Model model, HttpSession session) {
        String loginResult = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
        if ("Login successful".equals(loginResult)) {
            session.setAttribute("loggedInUser", loginRequest.getEmail());
            return "redirect:/auth/home";
        }
        model.addAttribute("error", "Invalid email or password.");
        return "login";
    }

    // ➤ Show Home Page
    @GetMapping("/home")
    public String showHomePage(HttpSession session, Model model) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("userEmail", loggedInUser);
        return "home";
    }

    // ➤ Logout Logic
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }

    // ➤ Show OTP Send Page
    @GetMapping("/send")
    public String showOtpSendPage(@RequestParam(required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "sendOtp";
    }

    // ➤ Send OTP Logic
    @PostMapping("/send")
    public String sendOtp(@RequestParam String email, Model model) {
        boolean isOtpSent = emailService.sendOtp(email);
        if (isOtpSent) {
            model.addAttribute("email", email);
            return "redirect:/auth/ver?email=" + email;
        } else {
            model.addAttribute("message", "Failed to send OTP. Try again.");
            return "sendOtp";
        }
    }

    // ➤ Show OTP Verification Page
    @GetMapping("/ver")
    public String showOtpVerificationPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "verifyOtp";
    }

    // ➤ OTP Verification Logic
    @PostMapping("/ver")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp, Model model) {
        if (emailService.validateOtp(email, otp)) {
            model.addAttribute("message", "OTP verified successfully. Please login.");
            return "login";
        } else {
            model.addAttribute("message", "Invalid OTP. Please try again.");
            return "verifyOtp";
        }
    }
}