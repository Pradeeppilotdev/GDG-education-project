package bigprojects.service;

import bigprojects.model.User;
import bigprojects.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // ➤ Check if email is registered
    public boolean isEmailRegistered(String email) {
        return userRepository.findByEmail(email) != null;
    }

    // ➤ Signup function
    public void signup(User user) {
        userRepository.save(user);
    }

    // ➤ Login function
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return "Login successful";
        }
        return "Invalid email or password";
    }
}