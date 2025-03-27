package bigprojects.controller;

import bigprojects.model.Video;
import bigprojects.dto.SearchRequest;
import bigprojects.service.YoutubeService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Controller
@RequestMapping("/videos")
public class VideoController {

    private final YoutubeService youtubeService;

    public VideoController(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    @GetMapping
    public String listVideos(Model model) {
        model.addAttribute("videos", youtubeService.getAllVideos());
        model.addAttribute("searchRequest", new SearchRequest());
        return "videos";
    }

    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("searchRequest", new SearchRequest());
        return "search";
    }

    @PostMapping("/search")
    public String searchVideos(@ModelAttribute SearchRequest searchRequest, Model model) {
        try {
            List<Video> searchResults = youtubeService.searchVideos(searchRequest);
            model.addAttribute("searchResults", searchResults);
            model.addAttribute("searchRequest", searchRequest);
            return "search-results";
        } catch (GeneralSecurityException | IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error searching videos",
                    e
            );
        }
    }

    @GetMapping("/watch/{youtubeId}")
    public String watchVideo(@PathVariable String youtubeId, Model model) {
        Video video = youtubeService.getAllVideos().stream()
                .filter(v -> v.getYoutubeId().equals(youtubeId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Video not found with YouTube ID: " + youtubeId
                ));

        model.addAttribute("video", video);
        return "watch-video";  // Create this Thymeleaf view
    }

    @PostMapping("/save")
    public String saveVideo(@ModelAttribute Video video) {
        youtubeService.saveVideo(video);
        return "redirect:/videos";
    }

    @GetMapping("/category/{category}")
    public String getVideosByCategory(@PathVariable String category, Model model) {
        model.addAttribute("videos", youtubeService.getVideosByCategory(category));
        model.addAttribute("category", category);
        return "category";
    }

    @GetMapping("/favorites")
    public String getFavoriteVideos(Model model) {
        model.addAttribute("videos", youtubeService.getFavoriteVideos());
        return "favorites";
    }

    @PostMapping("/toggle-favorite/{id}")
    public String toggleFavorite(@PathVariable Long id) {
        youtubeService.toggleFavorite(id);
        return "redirect:/videos";
    }

    @PostMapping("/delete/{id}")
    public String deleteVideo(@PathVariable Long id) {
        youtubeService.deleteVideo(id);
        return "redirect:/videos";
    }

    @GetMapping("/view/{id}")
    public String viewVideo(@PathVariable Long id, Model model) {
        Video video = youtubeService.getAllVideos().stream()
                .filter(v -> v.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Video not found with id: " + id
                ));

        model.addAttribute("video", video);
        return "videos";
    }
}
