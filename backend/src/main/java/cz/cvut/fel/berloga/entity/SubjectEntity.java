package cz.cvut.fel.berloga.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectEntity  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subject_seq_gen")
    @SequenceGenerator(name = "subject_seq_gen", sequenceName = "subject_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "picture")
    private String picture;

    @Column(name = "description", columnDefinition="TEXT")
    private String description;

    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<UserSubjectEntity> users;

    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<QuestionForumEntity> questions;

    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PageEntity> pages;

}
