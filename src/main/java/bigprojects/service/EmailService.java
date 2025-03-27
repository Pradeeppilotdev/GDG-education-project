package bigprojects.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // ConcurrentHashMap for thread-safe OTP storage with expiry time
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    // Inner class to hold OTP with its expiry timestamp
    private static class OtpEntry {
        String otp;
        long expiryTime;

        OtpEntry(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }

    // Method to generate a random 6-digit OTP
    public String generateOTP() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000)); // Generates 6-digit OTP
    }

    // Method to send OTP to the user's email
    public boolean sendOtp(String email) {
        String otp = generateOTP();
        long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000); // OTP valid for 5 minutes
        otpStore.put(email, new OtpEntry(otp, expiryTime)); // Store OTP with expiry time

        // Send OTP via email
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your OTP Code");
            message.setText("Your OTP code is: " + otp + "\nNote: This OTP will expire in 5 minutes.");
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve the stored OTP
    public String getSentOtp(String email) {
        OtpEntry otpEntry = otpStore.get(email);
        return (otpEntry != null) ? otpEntry.otp : null;
    }

    // Validate the OTP entered by the user
    public boolean validateOtp(String email, String enteredOtp) {
        OtpEntry otpEntry = otpStore.get(email);

        if (otpEntry != null) {
            if (System.currentTimeMillis() > otpEntry.expiryTime) {
                otpStore.remove(email); // Remove expired OTP
                return false; // Expired OTP
            }
            return otpEntry.otp.equals(enteredOtp);
        }
        return false; // OTP not found
    }

    // Scheduled cleanup to remove expired OTPs every 10 minutes
    @Scheduled(fixedRate = 600000)
    public void cleanupExpiredOtps() {
        otpStore.entrySet().removeIf(entry -> System.currentTimeMillis() > entry.getValue().expiryTime);
    }

}
