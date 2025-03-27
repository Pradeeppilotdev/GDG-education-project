package bigprojects.dto;

import java.util.List;

public class GeminiChatRequest {
    private List<Content> contents;

    // Default constructor
    public GeminiChatRequest() {}

    public GeminiChatRequest(String prompt) {
        this.contents = List.of(new Content(prompt));
    }

    public List<Content> getContents() {
        return contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }

    public static class Content {
        private List<Part> parts;

        public Content() {}

        public Content(String text) {
            this.parts = List.of(new Part(text));
        }

        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
    }

    public static class Part {
        private String text;

        public Part() {}

        public Part(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
