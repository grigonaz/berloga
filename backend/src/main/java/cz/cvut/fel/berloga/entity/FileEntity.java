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
public class FileEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "file_seq_gen")
    @SequenceGenerator(name = "file_seq_gen", sequenceName = "file_id_seq", allocationSize = 1)
    private Long id;

    // example: hello.txt
    @Column(name = "name", nullable = false)
    private String name;

    // example: 6d54f65s4d5c4s54e4r56df6
    @Column(name = "pseudoName", nullable = false, unique = true)
    private String pseudoName;

    // in bytes
    @Column(name = "size")
    private Long size;

    @JoinColumn(name = "owner")
    @ManyToOne(cascade = CascadeType.MERGE)
    private UserEntity owner;

    @OneToOne(mappedBy = "file")
    private MessageEntity message;

}
