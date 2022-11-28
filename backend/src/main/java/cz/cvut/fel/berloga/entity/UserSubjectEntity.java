package cz.cvut.fel.berloga.entity;

import cz.cvut.fel.berloga.entity.enums.UserSubjectEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSubjectEntity  implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_subject_seq_gen")
    @SequenceGenerator(name = "user_subject_seq_gen", sequenceName = "user_subject_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name="subject_id")
    private SubjectEntity subject;

    @Enumerated(EnumType.STRING)
    private UserSubjectEnum userType;
}
