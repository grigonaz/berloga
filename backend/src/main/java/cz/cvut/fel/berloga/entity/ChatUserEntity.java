package cz.cvut.fel.berloga.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.cvut.fel.berloga.entity.enums.UserChatRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatUserEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_user_seq_gen")
    @SequenceGenerator(name = "chat_user_seq_gen", sequenceName = "chat_user_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name="user_id")
    private UserEntity user;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name="chat_id")
    private ChatEntity chat;

    @Enumerated(EnumType.STRING)
    private UserChatRoleEnum role;

}
