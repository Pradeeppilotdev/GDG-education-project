package bigprojects.controller;

import bigprojects.service.QnAService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AIController {

    private final QnAService qnAService;

    public AIController(QnAService qnAService) {
        this.qnAService = qnAService;
    }

    @GetMapping("/chatbot")
    public String showChatbotPage() {
        return "chatbot"; // Refers to chatbot.html in templates folder
    }

    @PostMapping("/chatbot/ask")
    public String askQuestion(@RequestParam("question") String question, Model model) {
        if (question == null || question.trim().isEmpty()) {
            model.addAttribute("answer", "⚠️ Please enter a valid question.");
        } else {
            String answer = qnAService.getAnswer(question);
            model.addAttribute("question", question);
            model.addAttribute("answer", answer);
        }
        return "chatbot";
    }
}
