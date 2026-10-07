package com.oow.todowithspirit.domain.spirit;

import com.oow.todowithspirit.domain.task.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategorySpiritActionRepository extends JpaRepository<CategorySpiritAction, Long> {

    List<CategorySpiritAction> findAllByCategoryOrderBySortOrderAsc(CategoryType category);

    List<CategorySpiritAction> findAllByOrderByCategoryAscSortOrderAsc();
}
