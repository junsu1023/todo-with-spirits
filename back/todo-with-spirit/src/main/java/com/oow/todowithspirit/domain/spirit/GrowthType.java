package com.oow.todowithspirit.domain.spirit;

public enum GrowthType {

    // todo: change details
    FOCUS("집중", "업무, 공부, 깊은 작업 관련 할 일을 완료했을 때 성장합니다."),
    VITALITY("활력", "운동, 건강, 생활 습관 루틴을 완료했을 때 성장합니다."),
    PERSISTENCE("꾸준함", "매일 반복되는 습관을 유지하고 체크했을 때 성장합니다."),
    CREATIVITY("창의", "글쓰기, 개발, 창작 활동 관련 할 일을 완료했을 때 성장합니다.");

    private final String label;
    private final String description;

    GrowthType(String label, String description) {
        this.label = label;
        this.description = description;
    }
}
