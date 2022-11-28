package cz.cvut.fel.berloga.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
public class SharedCalendarRecordEntity  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shared_calendar_record_seq_gen")
    @SequenceGenerator(name = "shared_calendar_record_seq_gen", sequenceName = "shared_calendar_record_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="record_id")
    private CalendarRecordEntity record;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="user_id")
    private UserEntity user;        // shared with
}
