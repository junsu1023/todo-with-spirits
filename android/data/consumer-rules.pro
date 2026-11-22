
# Gson은 필드 이름으로 JSON을 매핑하는데 요청 / 응답 모델에 @SerializedName이 없어,
# R8이 필드 이름을 바꾸거나 지우지 않도록 그대로 둔다.
-keep class com.example.data.request.** { *; }
-keep class com.example.data.response.** { *; }
