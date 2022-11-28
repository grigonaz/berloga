package cz.cvut.fel.berloga.repository;

import cz.cvut.fel.berloga.entity.SharedCalendarRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SharedCalendarRecordRepository extends JpaRepository<SharedCalendarRecordEntity, Long> {
}
