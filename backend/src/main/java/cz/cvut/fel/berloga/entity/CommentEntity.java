package cz.cvut.fel.berloga.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentEntity  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_seq_gen")
    @SequenceGenerator(name = "comment_seq_gen", sequenceName = "comment_id_seq", allocationSize = 1)
    private Long id;

    @Column(columnDefinition="TEXT")
    private String content;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="user_id")
    private UserEntity sender;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="question_id")
    private QuestionForumEntity question;

    @Column(name = "date_time", nullable = false)
    private OffsetDateTime dateTime;
}
