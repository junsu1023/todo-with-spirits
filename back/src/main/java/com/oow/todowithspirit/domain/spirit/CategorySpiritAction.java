package com.oow.todowithspirit.domain.spirit;

import com.oow.todowithspirit.domain.task.CategoryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category_spirit_actions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategorySpiritAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryType category;

    @Column(name = "action_text", nullable = false, length = 100)
    private String actionText;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
