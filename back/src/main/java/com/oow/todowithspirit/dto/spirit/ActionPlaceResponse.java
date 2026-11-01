package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.SpiritActionPlace;
import com.oow.todowithspirit.domain.spirit.SpiritActionPlaceMaster;
import com.oow.todowithspirit.domain.spirit.SpiritPlaceAction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ActionPlaceResponse {

    private final Long id;
    private final SpiritActionPlace code;
    private final String placeName;
    private final String representativeAction;
    private final List<ActionExample> actions;

    public static ActionPlaceResponse of(SpiritActionPlaceMaster place, List<SpiritPlaceAction> actions) {
        List<ActionExample> examples = actions.stream()
                .map(a -> new ActionExample(a.getId(), a.getActionText(), a.isRequiresObject()))
                .toList();
        return new ActionPlaceResponse(place.getId(), place.getCode(), place.getPlaceName(), place.getRepresentativeAction(), examples);
    }

    @Getter
    @AllArgsConstructor
    public static class ActionExample {
        private final Long id;
        private final String actionText;
        private final boolean requiresObject;
    }
}