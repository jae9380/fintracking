package com.ft.back.batch.application.port;

import com.ft.back.batch.domain.MonthlyStatistics;

import java.util.List;

public interface MonthlyStatisticsRepository {

    void saveAll(List<MonthlyStatistics> statistics);
}
