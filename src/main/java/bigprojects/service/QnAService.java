package bigprojects.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class QnAService {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;

    public QnAService(WebClient.Builder webClient) {
        this.webClient = webClient.build();
    }

    public String getAnswer(String question) {
        // Create a prompt that requests voice-friendly learning content
        String enhancedPrompt = "Create a voice-based learning response with the following characteristics: " +
                "1. Use short, clear sentences optimized for listening rather than reading " +
                "2. Include 3-4 key learning points with memorable examples " +
                "3. Use pauses and verbal cues like 'First...', 'Next...', 'Remember that...' " +
                "4. Avoid complex vocabulary and use conversational language " +
                "5. Include verbal learning aids like mnemonics or rhymes when helpful " +
                "Question: " + question;

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", enhancedPrompt)
                        })
                },
                "generationConfig", Map.of(
                        "temperature", 0.6,
                        "maxOutputTokens", 400  // Allow more tokens for natural speech patterns
                )
        );

        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);
            String rawAnswer = jsonNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Format the response for voice-based learning
            return formatVoiceBasedLearning(rawAnswer, question);
        } catch (Exception e) {
            return "Let's talk about " + getTopicFromQuestion(question) + ". " +
                    "I don't have all the details right now. " +
                    "Let's try a different question. " +
                    "Think about what you'd like to learn, and I'll help you understand it step by step.";
        }
    }

    private String formatVoiceBasedLearning(String rawAnswer, String question) {
        // Process the response to make it more suitable for voice learning

        // First, replace any numbered points with verbal cues
        String processed = rawAnswer.replaceAll("1\\.", "First,")
                .replaceAll("2\\.", "Second,")
                .replaceAll("3\\.", "Third,")
                .replaceAll("4\\.", "Fourth,")
                .replaceAll("5\\.", "Fifth,")
                .replaceAll("•", "")
                .replaceAll("-", "")
                .replaceAll("\\*", "");

        // Replace semicolons with periods for clearer speech pauses
        processed = processed.replaceAll(";", ".");

        // Break into sentences
        String[] sentences = processed.split("(?<=[.!?])\\s+");
        StringBuilder voiceResponse = new StringBuilder();

        // Start with an introduction
        if (!processed.startsWith("Let's talk") && !processed.startsWith("Today") &&
                !processed.startsWith("I'm going to") && !processed.startsWith("Hello")) {
            voiceResponse.append("Let's talk about ").append(getTopicFromQuestion(question)).append(". ");
        }

        // Add a learning introduction if not present
        boolean hasLearningIntro = processed.contains("going to learn") ||
                processed.contains("let's learn") ||
                processed.contains("we'll learn");

        if (!hasLearningIntro) {
            voiceResponse.append("Here's what you need to know. ");
        }

        // Process sentences to make them more voice-friendly
        for (String sentence : sentences) {
            // Skip very short sentences or duplicate content
            if (sentence.length() < 3 || voiceResponse.toString().contains(sentence)) {
                continue;
            }

            // Add verbal pauses for longer sentences
            if (sentence.length() > 100) {
                String[] parts = sentence.split(",");
                if (parts.length > 1) {
                    for (String part : parts) {
                        if (!part.trim().isEmpty()) {
                            voiceResponse.append(part.trim()).append(". ");
                        }
                    }
                    continue;
                }
            }

            voiceResponse.append(sentence).append(" ");

            // Add short pauses between conceptual groups
            if (sentence.endsWith(".") || sentence.endsWith("!") || sentence.endsWith("?")) {
                // Don't add pause markers after the last sentence
                if (!sentence.equals(sentences[sentences.length - 1])) {
                    voiceResponse.append("<pause> ");
                }
            }
        }

        // Add a verbal memory aid if not present
        if (!processed.contains("remember") && !processed.contains("Remember")) {
            voiceResponse.append("<pause> Remember this simple way to think about it: ")
                    .append(getMemoryAid(question)).append(" ");
        }

        // Add a conclusion
        if (!processed.contains("In summary") && !processed.contains("To summarize")) {
            voiceResponse.append("<pause> To wrap up: The key to understanding ")
                    .append(getTopicFromQuestion(question))
                    .append(" is practice and thinking about how it connects to things you already know.");
        }

        // Add SSML markers for voice emphasis and pauses
        String finalResponse = voiceResponse.toString()
                .replaceAll("<pause>", "")  // Remove pause markers (for SSML use, these would be converted to actual pause tags)
                .replaceAll("\\s+", " ")    // Normalize whitespace
                .trim();

        return finalResponse;
    }

    private String getMemoryAid(String question) {
        // Generate a simple memory aid based on the topic
        String topic = getTopicFromQuestion(question);

        String[] memoryAids = {
                "Think of it like building blocks - each part supports the next.",
                "Just remember the word LEARN: Listen, Explore, Apply, Review, and Note-taking.",
                "It's as easy as ABC: Always Be Curious about how " + topic + " works.",
                "Picture yourself actually doing it step by step.",
                "Connect it to something you do every day, like brushing your teeth or making coffee.",
                "The three keys are: understand the basics, practice regularly, and teach someone else.",
                "Remember this rhyme: 'What I hear, I might forget. What I see, I remember. What I do, I understand.'",
                "Use the hand method: each finger represents one key point about " + topic + "."
        };

        return memoryAids[(int)(Math.random() * memoryAids.length)];
    }

    private String getTopicFromQuestion(String question) {
        // Extract main topic from question
        String topic = question.replaceAll("\\?|\\.|!|what is|how to|can you|tell me about", "").trim();

        // Truncate if too long
        if (topic.length() > 20) {
            String[] words = topic.split("\\s+");
            if (words.length > 3) {
                topic = String.join(" ", words[0], words[1], words[2]);
            } else {
                topic = topic.substring(0, 20);
            }
        }

        return topic.isEmpty() ? "this topic" : topic;
    }
}