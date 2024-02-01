package vn.giabaoblog.giabaoblogserver.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.giabaoblog.giabaoblogserver.config.exception.AccessException;
import vn.giabaoblog.giabaoblogserver.config.exception.NotFoundException;
import vn.giabaoblog.giabaoblogserver.config.exception.UserNotFoundException;
import vn.giabaoblog.giabaoblogserver.data.domains.Post;
import vn.giabaoblog.giabaoblogserver.data.domains.User;
import vn.giabaoblog.giabaoblogserver.data.dto.shortName.CreateOrUpdatePostDTO;
import vn.giabaoblog.giabaoblogserver.data.dto.shortName.PostDTO;
import vn.giabaoblog.giabaoblogserver.data.repository.PostRepository;
import vn.giabaoblog.giabaoblogserver.data.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PostService {

    @Autowired
    private final PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }
//    public List<Post> getAllMyPosts() {
//
//    }
//
    public PostDTO createPost(CreateOrUpdatePostDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principal = (User) authentication.getPrincipal();
        Long userId = principal.getId();
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthorId(userId);
        postRepository.save(post);
        PostDTO postDTO = new PostDTO(post);
        return postDTO;
    }

    public PostDTO updatePost(CreateOrUpdatePostDTO request) {
        String title = request.getTitle();
        String content = request.getContent();
        Long postId = request.getPostId();
        if (postId == null) {
            throw new NotFoundException(String.format("Post not found"));
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principal = (User) authentication.getPrincipal();
        Long userId = principal.getId();

        Optional<Post> postOpt = postRepository.findById(postId);
        if (!postOpt.isPresent()) {
            throw new NotFoundException(String.format("Post not found with Id = %s", postId));
        }
        Post post = postOpt.get();
        if (!post.getAuthorId().equals(userId)) {
            throw new AccessException(String.format("You only can update your post"));
        }
        if (title != null) {
            post.setTitle(request.getTitle());
        }
        if (content != null) {
            post.setContent(request.getContent());
        }
        postRepository.save(post);
        PostDTO postDTO = new PostDTO(post);
        return postDTO;
    }

    public void deletePost(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principal = (User) authentication.getPrincipal();
        Long userId = principal.getId();

        Optional<Post> postOpt = postRepository.findById(postId);
        if (!postOpt.isPresent()) {
            throw new NotFoundException(String.format("Post not found with Id = %s", postId));
        }
        Post post = postOpt.get();
        if (!post.getAuthorId().equals(userId)) {
            throw new AccessException(String.format("You only can delete your post"));
        }
        postRepository.delete(post);
    }


}
