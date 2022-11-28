package cz.cvut.fel.berloga.repository;

import cz.cvut.fel.berloga.entity.CalendarRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarRecordRepository extends JpaRepository<CalendarRecordEntity, Long> {
    List<CalendarRecordEntity> findCalendarRecordEntityByUserId(Long userId);
}
