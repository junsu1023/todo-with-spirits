package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.currency.CurrencyReason;
import com.oow.todowithspirit.domain.decoration.Decoration;
import com.oow.todowithspirit.domain.decoration.DecorationRepository;
import com.oow.todowithspirit.domain.decoration.UserDecoration;
import com.oow.todowithspirit.domain.decoration.UserDecorationRepository;
import com.oow.todowithspirit.dto.decoration.DecorationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DecorationService {

    private final DecorationRepository decorationRepository;
    private final UserDecorationRepository userDecorationRepository;
    private final CurrencyService currencyService;

    public List<DecorationResponse> getDecorations(Long userId) {
        Map<Long, UserDecoration> owned = userDecorationRepository.findAllByUserId(userId).stream()
                .collect(Collectors.toMap(UserDecoration::getDecorationId, Function.identity()));

        return decorationRepository.findAllByOrderByIdAsc().stream()
                .map(decoration -> {
                    UserDecoration ownedRow = owned.get(decoration.getId());
                    return DecorationResponse.of(decoration, ownedRow != null, ownedRow != null && ownedRow.isInstalled());
                })
                .toList();
    }

    /**
     * 장식을 재화로 구매한다. 구매만 하고 설치는 하지 않은 상태로 보유하게 된다.
     * @return 구매 후 잔액
     */
    @Transactional
    public long purchase(Long userId, Long decorationId) {
        Decoration decoration = decorationRepository.findById(decorationId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Decoration not found"));
        if (userDecorationRepository.existsByUserIdAndDecorationId(userId, decorationId)) {
            throw new ApiException(ErrorCode.ALREADY_COMPLETED, "Already purchased");
        }

        currencyService.debit(userId, decoration.getPrice(), CurrencyReason.DECORATION_PURCHASE);
        userDecorationRepository.save(new UserDecoration(userId, decorationId));

        return currencyService.getBalance(userId);
    }

    @Transactional
    public void install(Long userId, Long decorationId) {
        UserDecoration owned = ownedDecoration(userId, decorationId);
        if (owned.isInstalled()) {
            throw new ApiException(ErrorCode.ALREADY_COMPLETED, "Already installed");
        }
        owned.install();
    }

    @Transactional
    public void uninstall(Long userId, Long decorationId) {
        UserDecoration owned = ownedDecoration(userId, decorationId);
        if (!owned.isInstalled()) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Decoration is not installed");
        }
        owned.uninstall();
    }

    private UserDecoration ownedDecoration(Long userId, Long decorationId) {
        return userDecorationRepository.findByUserIdAndDecorationId(userId, decorationId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "You do not own this decoration"));
    }
}
