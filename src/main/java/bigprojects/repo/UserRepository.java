package bigprojects.repo;

import bigprojects.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // Find user by email
    User findByEmail(String email);

}
