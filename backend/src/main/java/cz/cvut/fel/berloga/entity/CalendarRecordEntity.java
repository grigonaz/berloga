package cz.cvut.fel.berloga.entity;

import cz.cvut.fel.berloga.entity.enums.RecordTypeEnum;
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
public class CalendarRecordEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calendar_record_seq_gen")
    @SequenceGenerator(name = "calendar_record_seq_gen", sequenceName = "calendar_record_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private OffsetDateTime dateTimeStart;

    @Column(nullable = false)
    private OffsetDateTime dateTimeFinish;


    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="user_id")
    private UserEntity user;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    private RecordTypeEnum recordType;

    @Column(name = "repeat_cycle")
    private String repeat;  // 1d9, 2h70, 6w0 (jump_type_repeatCycles) 0 = unlimited, null = dont repeat

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="subject_id")
    private SubjectEntity subject;

    @OneToMany(mappedBy = "record", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<SharedCalendarRecordEntity> shares;
}
