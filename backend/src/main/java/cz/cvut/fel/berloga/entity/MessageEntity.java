package cz.cvut.fel.berloga.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class MessageEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_seq_gen")
    @SequenceGenerator(name = "message_seq_gen", sequenceName = "message_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "text", columnDefinition="TEXT")
    private String text;

    @Column(name = "date", nullable = false)
    private OffsetDateTime date;

    @ManyToOne
    @JoinColumn(name="user_id")
    private UserEntity sender;

    @ManyToOne
    @JoinColumn(name="chat_id")
    private ChatEntity chat;

    @JoinColumn(name="file_id", referencedColumnName = "id")
    @OneToOne
    private FileEntity file;

}