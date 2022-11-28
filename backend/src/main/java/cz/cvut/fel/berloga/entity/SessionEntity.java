package cz.cvut.fel.berloga.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "session_entity")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionEntity  implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "session_seq_gen")
	@SequenceGenerator(name = "session_seq_gen", sequenceName = "session_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name="user_id")
	private UserEntity user;

	@Column
	private String session;

	@Column(nullable = false)
	private OffsetDateTime lastAccess;


}