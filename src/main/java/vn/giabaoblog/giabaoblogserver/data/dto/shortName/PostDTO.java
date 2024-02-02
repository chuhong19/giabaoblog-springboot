package vn.giabaoblog.giabaoblogserver.data.dto.shortName;

import lombok.Data;
import lombok.NoArgsConstructor;
import vn.giabaoblog.giabaoblogserver.data.domains.Post;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class PostDTO implements Serializable {
    private Long id;
    private String title;
    private String content;
    private Long authorId;

    public PostDTO(PostDTO post) {
    }

    public PostDTO(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorId = post.getAuthorId();
    }

}
