package com.ft.back.transaction.infrastructure.persistence;

import com.ft.back.transaction.application.port.CategoryRepository;
import com.ft.back.transaction.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final JpaCategoryRepository jpaCategoryRepository;

    @Override
    public Category save(Category category) {
        return jpaCategoryRepository.save(category);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return jpaCategoryRepository.findById(id);
    }

    @Override
    public List<Category> findAllAccessibleByUserId(Long userId) {
        return jpaCategoryRepository.findAllByUserIdOrIsDefaultTrue(userId);
    }

    @Override
    public void delete(Category category) {
        jpaCategoryRepository.delete(category);
    }
}
