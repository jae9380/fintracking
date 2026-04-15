package com.ft.back.batch.infrastructure.persistence;

import com.ft.back.batch.domain.MonthlyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMonthlyStatisticsRepository extends JpaRepository<MonthlyStatistics, Long> {
}
