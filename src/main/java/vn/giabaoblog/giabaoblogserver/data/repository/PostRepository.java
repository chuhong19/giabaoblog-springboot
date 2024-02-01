package vn.giabaoblog.giabaoblogserver.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.giabaoblog.giabaoblogserver.data.domains.Post;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Override
    Optional<Post> findById(Long id);
}
