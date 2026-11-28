# 성장 외형 리소스 제작 기록

## 현재 적용 버전 — v2 알파 정리

아래 v1 제작 기록은 보존한다. 2026-09-18 후속 작업에서 사용자의 명시적 허용에 따라 두 v1의 낮은 알파 잡점만 정밀 픽셀 처리한 `wind-spirit-adolescent-v2.png`, `wind-spirit-adult-v2.png`를 같은 `Assets/TodoSpirits/Art/ForestDaily/`에 추가했다. 본체 RGBA·네 표정·위치·1254×1254 캔버스는 보존하며 원본 v1 PNG·메타는 그대로 둔다. 현재 프리팹의 두 성장 배열은 v2의 네 칸씩을 참조한다.

- 경계와 외부 잡점 검사: 두 v2 모두 본체4개, 본체 외부 점0, 캔버스 외곽alpha0.
- 원본 보존/정확한 처리 범위: [정리 기록](../Validation/ForestPrototype/growth-alpha-cleanup.md).
- Unity 분할/연결/검증: `growth-v2-prepare-final.json` PASS. 새 파일의 최초 자동 분할 메타도 변경 전 백업했다. v1·P1·어린 정령 메타를 재생성하지 않았다.
- Sprite 설정은 기존과 같은 Android ASTC6×6, WebGL DXT5다. 임포트 분석에서 v1의 불투명 본체에 새 큰 투명 구멍이 생긴 증거가 없어 압축 설정은 변경하지 않았다.
- 이미지 생성 도구의 정리 후보는 잔여 잡점·색조 변화가 남아 채택하지 않았다. 픽셀 정리는 승인 후 Python/Pillow로 새 사본에만 수행했다.

2026-09-18 · 내장 `image_gen` 도구 사용. 기존 어린 분홍 바람 정령을 유지하고 중간·성체용 별도 표현 시트를 생성했다. 기존 PNG, GUID, P1 후보를 덮어쓰지 않았다.

## 결과물과 적용 기준

| 단계 | 실제 파일 | 구별되는 모습 |
| --- | --- | --- |
| 어린 정령 | 기존 `Assets/TodoSpirits/Art/Forest/wind-spirit-expressions.png` | 기존 무장식 몸과 표정 |
| 관심~준비 단계 | `Assets/TodoSpirits/Art/ForestDaily/wind-spirit-adolescent-v1.png` | 잎 스카프·길어진 외꼬리 |
| 성체 | `Assets/TodoSpirits/Art/ForestDaily/wind-spirit-adult-v1.png` | 어깨 잎 망토·양쪽 꼬리 |

각 신규 시트는 2×2의 독립된 idle/blink/happy/curious 표정이다. 표정 시트이며 독서 애니메이션의 누락된 두 번째 프레임을 납품한 것으로 세지 않는다. P1 패키지의 나머지 8종도 이 작업으로 완성 처리하지 않는다.

제작 직후 원본을 육안 확인했다. 각 단계의 장식과 실루엣은 구별된다. 투명 배경 주변에는 작은 잔여 픽셀이 보이므로 임포터가 이를 별도 sprite로 자동 분할하지 않도록 명시적인 네 영역만 사용한다. 최종 해상도·성장 전환·앨범의 당시 모습은 Unity 실행 결과로 별도 검증해야 한다.

## 생성 원본

- 참조: `Assets/TodoSpirits/Art/Forest/wind-spirit-expressions.png`
- 중간 단계 생성 원본: `C:/Users/wigon/.codex/generated_images/01a0b2a0-e3ba-7d00-bc00-33094394ea75/exec-0ae6c847-157b-4aae-98c2-9ae232d43cc6.png`
- 성체 생성 원본: `C:/Users/wigon/.codex/generated_images/01a0b2a0-e3ba-7d00-bc00-33094394ea75/exec-05297d9e-e5f7-4b8f-ab4f-666a2f336815.png`

## 사용한 요청문 — 중간 단계

```text
Use case: identity-preserve. Asset type: production game character expression sprite sheet, transparent PNG. Reference image is the existing young pink wind spirit, not an edit destination. Create a separate ADOLESCENT growth-stage sprite sheet of this exact character. Preserve pink cloud-like body, curled forehead tuft, pink outline, brown eyes, blush cheeks, no feet, friendly proportions and soft clean 2D shading. Make growth visibly readable through a slightly more elongated pear body, a longer flowing tail curl and a small sage-green two-leaf neck kerchief with a tiny warm seed clasp. Keep face cute, not adult human. FOUR unique expressions in an exact 2 by 2 equal-cell SQUARE grid: top-left open-eyed quiet neutral, top-right content closed eyes, bottom-left joyful raised arms and open smiling mouth, bottom-right curious slight head tilt with small round mouth and one hand to cheek. All full bodies same scale with lower silhouette baseline consistent, each safely centered in its own quadrant with generous transparent gutters. Every quadrant has ONLY one spirit. Truly transparent alpha background, no checkerboard, no labels, no text, no border, no scenery, no floor shadow, no floating sparkles or loose particles. Clean cutout edges. Do not copy the young sprite unchanged; draw the adolescent anatomy and leaf kerchief consistently in each distinct pose. Keep illustrations simple enough to read at 160 pixels.
```

## 사용한 요청문 — 성체

```text
Use case: identity-preserve. Asset type: production game character expression sprite sheet, transparent PNG. Reference image defines the existing young pink wind spirit's identity. Create a separate ADULT growth-stage expression sheet, same pink wind spirit. Preserve pink cloud-like body, curled forehead tuft, pink outline, brown eyes, blush cheeks, friendly nonhuman form with no feet and soft clean 2D shading. The adult is recognizably more grown: softly taller pear silhouette with flowing twin tail curls, a small elegant sage-green leaf mantle over the shoulders, fastened with one warm seed clasp. No weapons, crown, extravagant costume or human face. Four unique poses in an exact 2 by 2 equal-cell SQUARE grid: top-left open-eyed calm neutral with gently lowered arms, top-right content closed eyes, bottom-left delighted open smile with welcoming arms, bottom-right curious slight head tilt with small round mouth and one hand to cheek. Match body height, alignment, mantle design, colors and consistent baseline among all four. Full bodies centered per quadrant, generous transparent padding, nothing crosses the center gridlines. Truly transparent alpha background; no checkerboard, labels, text, frame, scenery, floor shadows, sparks or detached particles. Clean silhouette cutout edges, polished coherent 2D mobile game art readable at 160 pixels. This is a new adult stage, do not duplicate the young original.
```
