package com.ft.back.batch.infrastructure.job;

import com.ft.back.auth.domain.User;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserItemReader {

    private static final int PAGE_SIZE = 10;

    private final EntityManagerFactory entityManagerFactory;

    public JpaPagingItemReader<User> create() {
        return new JpaPagingItemReaderBuilder<User>()
                .name("userItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT u FROM User u ORDER BY u.id ASC")
                .pageSize(PAGE_SIZE)
                .build();
    }
}
