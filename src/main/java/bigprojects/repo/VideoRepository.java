package bigprojects.repo;

import bigprojects.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long>{
    List<Video> findByCategory(String category);
    List<Video> findByFavoriteTrue();
    List<Video> findByTitleContainingIgnoreCase(String title);
}
