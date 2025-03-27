package bigprojects.controller;

import bigprojects.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/otp")
public class OtpController {

    @Autowired
    private EmailService emailService;

    // Display OTP sending page
    @GetMapping("/send")
    public String showOtpSendPage() {
        return "sendOtp";  // Refers to sendOtp.html
    }

    // Handle OTP sending logic
    @PostMapping("/send")
    public String sendOtp(@RequestParam String email, Model model) {
        boolean isOtpSent = emailService.sendOtp(email);
        if (isOtpSent) {
            model.addAttribute("email", email);  // Pass email for pre-filled data
            return "redirect:/otp/verify?email=" + email;
        } else {
            model.addAttribute("message", "Failed to send OTP. Try again.");
            return "sendOtp";
        }
    }


    // Display OTP verification page
    @GetMapping("/verify")
    public String showOtpVerifyPage() {
        return "verifyOtp";  // Refers to verifyOtp.html
    }

    // Handle OTP verification logic
    @PostMapping("/verify")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp, Model model) {
        boolean isOtpValid = emailService.validateOtp(email, otp);
        if (isOtpValid) {
            model.addAttribute("message", "OTP Verified Successfully!");
            return "success"; // success.html
        } else {
            model.addAttribute("message", "Invalid OTP. Please try again.");
            return "verifyOtp";
        }
    }
}