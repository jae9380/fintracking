package com.ft.back.batch.infrastructure.persistence;

import com.ft.back.batch.application.port.MonthlyStatisticsRepository;
import com.ft.back.batch.domain.MonthlyStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MonthlyStatisticsRepositoryImpl implements MonthlyStatisticsRepository {

    private final JpaMonthlyStatisticsRepository jpaMonthlyStatisticsRepository;

    @Override
    public void saveAll(List<MonthlyStatistics> statistics) {
        jpaMonthlyStatisticsRepository.saveAll(statistics);
    }
}
