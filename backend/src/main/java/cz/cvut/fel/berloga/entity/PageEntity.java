package cz.cvut.fel.berloga.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageEntity  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "page_seq_gen")
    @SequenceGenerator(name = "page_seq_gen", sequenceName = "page_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne()
    @JoinColumn(name="subject_id")
    private SubjectEntity subject;

    @Column(columnDefinition="TEXT")
    private String data;
}
