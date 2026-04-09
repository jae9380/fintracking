package com.ft.back.transaction.domain;

import com.ft.back.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ft.back.common.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Category 도메인 테스트")
class CategoryTest {

    @Nested
    @DisplayName("사용자 정의 카테고리 생성")
    class Create {

        @Test
        @DisplayName("성공 - 카테고리가 정상 생성된다")
        void success_createUserCategory() {
            // given
            Long userId = 1L;
            String name = "식비";
            CategoryType type = CategoryType.EXPENSE;

            // when
            Category category = Category.create(userId, name, type);

            // then
            assertThat(category.getName()).isEqualTo("식비");
            assertThat(category.isDefault()).isFalse();
        }

        @Test
        @DisplayName("실패 - 카테고리명이 공백이면 예외 발생")
        void fail_blankName_throwsException() {
            // given
            Long userId = 1L;
            String blankName = " ";
            CategoryType type = CategoryType.EXPENSE;

            // when & then
            assertThatThrownBy(() -> Category.create(userId, blankName, type))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(TRANSACTION_INVALID_CATEGORY_NAME));
        }
    }

    @Nested
    @DisplayName("기본 카테고리 생성")
    class CreateDefault {

        @Test
        @DisplayName("성공 - 기본 카테고리로 생성된다")
        void success_createDefaultCategory() {
            // given
            String name = "급여";
            CategoryType type = CategoryType.INCOME;

            // when
            Category category = Category.createDefault(name, type);

            // then
            assertThat(category.isDefault()).isTrue();
            assertThat(category.getUserId()).isNull();
        }
    }

    @Nested
    @DisplayName("카테고리 접근 검증")
    class ValidateAccessible {

        @Test
        @DisplayName("성공 - 기본 카테고리는 모든 사용자 접근 가능")
        void success_defaultCategory_anyUserCanAccess() {
            // given
            Category category = Category.createDefault("급여", CategoryType.INCOME);
            Long anyUserId = 99L;

            // when & then
            assertThatNoException().isThrownBy(() -> category.validateAccessible(anyUserId));
        }

        @Test
        @DisplayName("성공 - 본인 카테고리는 접근 가능")
        void success_ownCategory_canAccess() {
            // given
            Long userId = 1L;
            Category category = Category.create(userId, "식비", CategoryType.EXPENSE);

            // when & then
            assertThatNoException().isThrownBy(() -> category.validateAccessible(userId));
        }

        @Test
        @DisplayName("실패 - 타인의 카테고리는 접근 불가")
        void fail_otherUserCategory_throwsException() {
            // given
            Long ownerId = 1L;
            Long otherId = 2L;
            Category category = Category.create(ownerId, "식비", CategoryType.EXPENSE);

            // when & then
            assertThatThrownBy(() -> category.validateAccessible(otherId))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(TRANSACTION_NO_ACCESS));
        }
    }
}
