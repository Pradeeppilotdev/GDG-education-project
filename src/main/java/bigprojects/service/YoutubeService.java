package bigprojects.service;
import bigprojects.model.Video;
import bigprojects.dto.SearchRequest;
import bigprojects.repo.VideoRepository;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.VideoListResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
public class YoutubeService {

    private static final String APPLICATION_NAME = "Educational Video Generator";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Value("${youtube.api.key}")
    private String apiKey;

    @Autowired
    private VideoRepository videoRepository;

    public List<Video> searchVideos(SearchRequest searchRequest) throws GeneralSecurityException, IOException {
        YouTube youtubeService = getYouTubeService();

        YouTube.Search.List request = youtubeService.search().list("snippet");
        request.setKey(apiKey);
        request.setQ(searchRequest.getQuery() + " education");
        request.setType("video");
        request.setMaxResults(searchRequest.getMaxResults().longValue());
        request.setVideoCategoryId("27");
        request.setRelevanceLanguage("en");

        SearchListResponse response = request.execute();
        List<SearchResult> searchResults = response.getItems();

        List<Video> videos = new ArrayList<>();

        if (searchResults != null) {
            for (SearchResult result : searchResults) {
                Video video = new Video(
                        result.getId().getVideoId(),
                        result.getSnippet().getTitle(),
                        result.getSnippet().getDescription(),
                        result.getSnippet().getThumbnails().getHigh().getUrl(),
                        searchRequest.getCategory()
                );
                videos.add(video);
            }
        }

        return videos;
    }

    public Video getVideoById(String videoId) throws GeneralSecurityException, IOException {
        YouTube youtubeService = getYouTubeService();

        YouTube.Videos.List request = youtubeService.videos().list("snippet");
        request.setKey(apiKey);
        request.setId(videoId);

        VideoListResponse response = request.execute();
        List<com.google.api.services.youtube.model.Video> videoList = response.getItems();

        if (videoList != null && !videoList.isEmpty()) {
            com.google.api.services.youtube.model.Video ytVideo = videoList.get(0);
            return new Video(
                    ytVideo.getId(),
                    ytVideo.getSnippet().getTitle(),
                    ytVideo.getSnippet().getDescription(),
                    ytVideo.getSnippet().getThumbnails().getHigh().getUrl(),
                    "Education"
            );
        } else {
            throw new RuntimeException("Video not found with ID: " + videoId);
        }
    }

    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    public List<Video> getVideosByCategory(String category) {
        return videoRepository.findByCategory(category);
    }

    public List<Video> getFavoriteVideos() {
        return videoRepository.findByFavoriteTrue();
    }

    public Video saveVideo(Video video) {
        return videoRepository.save(video);
    }

    public void deleteVideo(Long id) {
        videoRepository.deleteById(id);
    }

    public Video toggleFavorite(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        video.setFavorite(!video.isFavorite());
        return videoRepository.save(video);
    }

    private YouTube getYouTubeService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}