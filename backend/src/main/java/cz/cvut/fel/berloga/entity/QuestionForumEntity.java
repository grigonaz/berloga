package cz.cvut.fel.berloga.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionForumEntity  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "question_seq_gen")
    @SequenceGenerator(name = "question_seq_gen", sequenceName = "question_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="user_id", nullable = false)
    private UserEntity questioner;

    @Column(name = "question", nullable = false, columnDefinition="TEXT")
    private String question;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="subject_id")
    private SubjectEntity subject;

    @Column(name = "done", nullable = false)
    private Boolean done;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "date_time", nullable = false)
    private OffsetDateTime dateTime;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CommentEntity> comments;



}
