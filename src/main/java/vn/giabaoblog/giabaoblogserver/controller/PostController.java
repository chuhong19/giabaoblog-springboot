package vn.giabaoblog.giabaoblogserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.giabaoblog.giabaoblogserver.data.domains.Post;
import vn.giabaoblog.giabaoblogserver.data.dto.mix.PostIdDTO;
import vn.giabaoblog.giabaoblogserver.data.dto.request.SearchPostRequest;
import vn.giabaoblog.giabaoblogserver.data.dto.response.StandardResponse;
import vn.giabaoblog.giabaoblogserver.data.dto.shortName.CreateOrUpdatePostDTO;
import vn.giabaoblog.giabaoblogserver.data.dto.shortName.PostDTO;
import vn.giabaoblog.giabaoblogserver.services.PostService;

import java.util.List;

@RestController
@RequestMapping(value = "/post")
public class PostController {

    @Autowired
    public PostService postService;

    @GetMapping("/getAllPosts")
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/getAllMyPosts")
    public List<Post> getAllMyPosts() {
        return postService.getAllMyPosts();
    }

    @GetMapping("/getAllMyFollowPosts")
    public void getAllMyFollowPosts() {
        postService.getAllMyFollowPosts();
    }

    @PostMapping("/create")
    public StandardResponse createPost(@RequestBody CreateOrUpdatePostDTO request) {
        PostDTO postDTO = postService.createPost(request);
        return StandardResponse.create("200", "Post created", postDTO);
    }

    @PostMapping("/update")
    public StandardResponse updatePost(@RequestBody CreateOrUpdatePostDTO request) {
        PostDTO postDTO = postService.updatePost(request);
        return StandardResponse.create("200", "Post updated", postDTO);
    }

    @PostMapping("/delete")
    public StandardResponse deletePost(@RequestBody PostIdDTO request) {
        Long postId = request.getPostId();
        postService.deletePost(postId);
        return StandardResponse.create("204", "Post deleted", postId);
    }

//    @PostMapping("/filterPost")
//    public Page<Post> filterPost(@RequestBody SearchPostRequest request) {
//        return postService.filterPost(request);
//    }
}
