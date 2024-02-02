package vn.giabaoblog.giabaoblogserver.data.domains;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name= "posts")
public class Post extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "[id]")
    public Long id;

    @Column(name= "title", nullable = false)
    public String title;

    @Column(name= "content", nullable = false)
    public String content;

    @JoinColumn(name = "[author_id]", nullable = false)
    public Long authorId;

}
