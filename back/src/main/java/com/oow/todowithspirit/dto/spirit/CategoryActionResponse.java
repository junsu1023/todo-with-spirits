package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.CategorySpiritAction;
import com.oow.todowithspirit.domain.task.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryActionResponse {

    private final CategoryType category;
    private final String actionText;

    public static CategoryActionResponse from(CategorySpiritAction action) {
        return new CategoryActionResponse(action.getCategory(), action.getActionText());
    }
}