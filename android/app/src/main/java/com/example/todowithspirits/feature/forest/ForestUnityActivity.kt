package com.example.todowithspirits.feature.forest

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Keep
import com.example.domain.usecase.GetTaskCalendarUseCase
import com.example.domain.usecase.GetUserProfileUseCase
import com.example.domain.usecase.RestoreSessionUseCase
import com.example.core.auth.TokenHolder
import com.example.todowithspirits.util.TaskRefreshBus
import com.unity3d.player.UnityPlayer
import com.unity3d.player.UnityPlayerGameActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ForestHostDependencies {
    fun profile(): GetUserProfileUseCase
    fun calendar(): GetTaskCalendarUseCase
    fun taskRefreshBus(): TaskRefreshBus
    // AndroidManifest에서 android:process=":forest"로 별도 프로세스에서 실행되므로,
    // 이 프로세스는 SplashScreen을 거치지 않는다. TokenHolder.awaitBootstrap()이
    // AuthInterceptor에서 영원히 대기하지 않도록 여기서 직접 세션을 복원해야 한다.
    fun restoreSession(): RestoreSessionUseCase
}

/** The native curtain stays up until Unity has opened this authenticated account's save.
 *
 * AndroidManifest에서 android:process=":forest"로 호스트 앱과 별도 프로세스에서 실행된다.
 * GameActivity.onDestroy()의 네이티브 엔진 종료 대기(terminateNativeCode)가 메인 스레드를
 * 블로킹하는 현상이 실기기에서 여러 형태(직접 finish, moveTaskToBack 후 OS의 자체 trim,
 * 앱 백그라운드 전환 시 정리 등)로 반복 재현되어, 어떤 시점에 어떤 방식으로 이 액티비티를
 * 닫든 호스트(MainActivity)의 입력 처리가 함께 멈추는 것을 막을 수 없었다(실기기 ANR로
 * 5차례 확인). 별도 프로세스로 분리하면 이 블로킹은 숲 프로세스에만 영향을 주고 호스트
 * 프로세스의 메인 스레드는 전혀 블로킹되지 않으므로, 닫을 때 별다른 우회 없이 그냥
 * finish()를 호출한다. */
@Keep
class ForestUnityActivity : UnityPlayerGameActivity() {
    private val work = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val dependencies by lazy { EntryPointAccessors.fromApplication(applicationContext, ForestHostDependencies::class.java) }
    private val preferences by lazy { getSharedPreferences("forest-host-sync", MODE_PRIVATE) }
    private var refreshJob: Job? = null
    private var historyJob: Job? = null
    private var lastHistoryKey: String? = null
    private var lastHistoryRequest = 0L
    private var ready = false
    private var resumed = false
    private var closing = false
    private var accountId: String? = null
    private var date: LocalDate = LocalDate.now()
    private var requestedSession: String? = null
    private var requestedSessionId: String? = null
    private var activeSession: String? = null
    private var pendingReply: ForestHostRequest? = null
    private var pendingSnapshot: String? = null
    private var syncState = "loading"
    private var syncMessage = "오늘의 기록을 확인하고 있어요."
    private lateinit var curtain: LinearLayout
    private lateinit var curtainText: TextView
    private lateinit var curtainProgress: ProgressBar
    private lateinit var curtainActions: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        curtain = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            setBackgroundColor(Color.rgb(240, 237, 219))
        }
        curtainText = TextView(this).apply { textSize = 19f; gravity = Gravity.CENTER; setTextColor(Color.rgb(47, 70, 53)) }
        curtainProgress = ProgressBar(this)
        curtainActions = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            addView(Button(this@ForestUnityActivity).apply { text = "다시 확인하기"; setOnClickListener { requestForestRefresh() } })
            addView(Button(this@ForestUnityActivity).apply { text = "일정으로 돌아가기"; setOnClickListener { requestForestClose() } })
        }
        curtain.addView(curtainText, LinearLayout.LayoutParams(-1, -2))
        curtain.addView(curtainProgress, LinearLayout.LayoutParams(-2, -2).apply { topMargin = 32 })
        curtain.addView(curtainActions, LinearLayout.LayoutParams(-1, -2))
        addContentView(curtain, ViewGroup.LayoutParams(-1, -1))
        // 진입/새로고침 중에는 로딩만 보여주고, 실제로 막혔을 때만(showActions=true) 재시도·돌아가기 버튼을 노출한다.
        showCurtain("정령과 만날 준비를 하고 있어요.")

        // 이 프로세스는 Splash를 거치지 않으므로 여기서 직접 세션을 복원하고 부트스트랩
        // 완료를 알려야, refresh()가 호출하는 API들이 AuthInterceptor에서 무한 대기하지 않는다.
        work.launch {
            runCatching { dependencies.restoreSession()() }
            TokenHolder.markBootstrapCompleted()
        }

        work.launch { dependencies.taskRefreshBus().events.collect { if (resumed) refresh() } }
        work.launch {
            delay(20000)
            if (!ready && !closing) showCurtain("숲을 여는 데 시간이 걸리고 있어요. 잠시 후 다시 들어와 주세요.", showActions = true)
        }
        // A midnight rollover is a new host session even while the forest remains on screen.
        work.launch { while (!closing) { delay(30000); if (resumed && date != LocalDate.now()) refresh() } }
    }

    override fun onResume() {
        super.onResume()
        resumed = true
        // moveTaskToBack로만 나갔다 들어온 경우 인스턴스가 재사용되므로, 이전 종료 시점의
        // closing 플래그를 여기서 풀어줘야 재진입 시 refresh()/dispatch()가 다시 동작한다.
        closing = false
        if (::curtain.isInitialized) refresh()
    }

    override fun onPause() { resumed = false; super.onPause() }

    override fun onDestroy() {
        closing = true
        work.cancel()
        super.onDestroy()
    }

    @Deprecated("Android compatibility callback")
    override fun onBackPressed() = requestForestClose()

    // GameActivity(androidx.games:games-activity)는 뒤로가기 키를 onBackPressed()로 넘기지 않고
    // dispatchKeyEvent에서 바로 네이티브 입력 큐로 전달해 소비해버린다. 여기서 먼저 가로채야 한다.
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            requestForestClose()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    // finish()가 아니라 moveTaskToBack을 쓴다. 별도 프로세스(:forest)라 onDestroy()의 네이티브
    // 종료 대기가 블로킹돼도 호스트 프로세스는 전혀 영향받지 않지만, 반복적으로 나갔다
    // 들어왔다 할 때마다 매번 finish()로 인스턴스를 죽였다 새로 만들면 이 프로세스 자신의
    // 메인 스레드에서 "이전 인스턴스의 onDestroy() 블로킹 중에 새 인스턴스의 onCreate()가
    // 창을 못 띄워 포커스를 못 받는" 경합이 생겨 숲 프로세스 자체가 ANR날 수 있다(실기기로
    // 확인). moveTaskToBack으로 인스턴스를 살려두고 재사용하면 이 경합 자체가 없어진다.
    @Keep
    fun requestForestClose() = runOnUiThread {
        if (!closing) {
            closing = true
            refreshJob?.cancel()
            moveTaskToBack(true)
        }
    }

    override fun onUnityPlayerUnloaded() = runOnUiThread { moveTaskToBack(true) }

    @Keep
    fun requestForestRefresh() = runOnUiThread { if (!closing) refresh() }

    @Keep
    fun onForestUnityReady() = runOnUiThread { ready = true; dispatch() }

    @Keep
    fun onForestSnapshotResult(result: String) = runOnUiThread {
        if (closing) return@runOnUiThread
        if (result.startsWith("{")) {
            val response = runCatching { JSONObject(result) }.getOrNull() ?: return@runOnUiThread
            val reply = ForestHostRequest(response.optString("kind"), response.optString("accountId"),
                response.optString("date"), response.optString("requestId"))
            val expected = if (reply.kind == "host-session") {
                requestedSessionId?.takeIf { requestedSession == sessionKey() }?.let {
                    ForestHostRequest("host-session", accountId.orEmpty(), date.toString(), it)
                }
            } else pendingReply?.takeIf {
                activeSession == sessionKey() && it.accountId == accountId && it.date == date.toString()
            }
            val status = response.optString("status")
            if (!ForestHostReplyPolicy.accepts(expected, reply, status)) return@runOnUiThread
            if (reply.kind != "host-session") pendingReply = null
            when (status) {
                "session-ready" -> {
                    activeSession = requestedSession
                    requestedSessionId = null
                    curtain.visibility = View.GONE
                    dispatch()
                }
                "session-rejected" -> {
                    requestedSession = null
                    requestedSessionId = null
                    activeSession = null
                    pendingReply = null
                    showCurtain("이 계정의 숲 기록을 열지 못했어요. 저장된 기록은 지우지 않았어요.", showActions = true)
                }
                "applied", "duplicate" -> curtain.visibility = View.GONE
                "rejected" -> {
                    syncState = "error"
                    syncMessage = "오늘 기록을 숲에 반영하지 못했어요. 다시 확인해 주세요."
                    activeSession = null
                    requestedSession = null
                    requestedSessionId = null
                    showCurtain(syncMessage, showActions = true)
                }
            }
            return@runOnUiThread
        }
        // Legacy uncorrelated replies cannot reveal a forest or reject a newer request.
        if (result.startsWith("historical-rejected"))
            Toast.makeText(this, "이전 기록 일부를 다시 확인하지 못했어요. 보관한 이야기는 유지돼요.", Toast.LENGTH_LONG).show()
    }

    private fun refresh() {
        if (closing) return
        refreshJob?.cancel()
        refreshJob = work.launch {
            syncState = "loading"
            syncMessage = "오늘의 기록을 확인하고 있어요."
            pendingSnapshot = null
            pendingReply = null
            sendStatus()
            try {
                val profile = dependencies.profile()().getOrThrow()
                ensureActive()
                val today = LocalDate.now()
                val nextAccount = profile.userId.toString()
                if (accountId != nextAccount || date != today) {
                    accountId = nextAccount
                    date = today
                    activeSession = null
                    requestedSession = null
                    requestedSessionId = null
                    pendingReply = null
                }
                dispatch()
                val calendar = dependencies.calendar()(date, date).getOrThrow()
                ensureActive()
                val rows = ForestHostSnapshot.items(date, calendar.items)
                val revisionKey = "revision:$nextAccount"
                val revision = ForestHostSnapshot.nextRevision(preferences.getLong(revisionKey, 0), System.currentTimeMillis())
                val confirmedAt = Instant.now().toString()
                // commit()은 디스크에 동기 기록될 때까지 블로킹된다. work가 Main.immediate라
                // IO로 넘기지 않으면 메인 스레드가 그대로 막힌다.
                withContext(Dispatchers.IO) {
                    check(preferences.edit().putLong(revisionKey, revision).putString("confirmed:$nextAccount", confirmedAt).commit()) {
                        "동기화 상태를 저장하지 못했어요."
                    }
                }
                val items = JSONArray()
                rows.forEach { row -> items.put(JSONObject().put("id", row.id).put("title", row.title).put("category", row.category).put("isHabit", row.isHabit).put("isCompleted", row.isCompleted)) }
                pendingSnapshot = JSONObject().put("schemaVersion", 1).put("accountId", nextAccount).put("date", date.toString()).put("timeZone", ZoneId.systemDefault().id)
                    .put("revision", revision).put("items", items).toString()
                syncState = "ready"
                syncMessage = "오늘의 기록을 확인했어요."
                dispatch()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                ensureActive()
                pendingSnapshot = null
                syncState = if (error is java.io.IOException) "offline" else "error"
                syncMessage = if (error is IllegalArgumentException) error.message ?: "오늘 기록을 확인하지 못했어요." else "최신 일정을 확인하지 못했어요. 연결을 확인한 뒤 다시 시도해 주세요."
                if (accountId == null) showCurtain("계정을 확인하지 못해 숲을 열 수 없어요. 연결을 확인한 뒤 다시 시도해 주세요.", showActions = true)
                else dispatch()
            }
        }
    }

    private fun sessionKey() = "$accountId:$date"

    @Keep
    fun requestForestHistory(request: String) = runOnUiThread {
        if (closing || !ready || activeSession != sessionKey() || historyJob?.isActive == true) return@runOnUiThread
        val now = android.os.SystemClock.elapsedRealtime()
        if (lastHistoryKey == request && now - lastHistoryRequest < 60000) return@runOnUiThread
        lastHistoryKey = request
        lastHistoryRequest = now
        historyJob = work.launch {
            try {
                require(request.length <= 8192)
                val data = JSONObject(request)
                val expectedAccount = data.getString("accountId")
                require(expectedAccount == accountId)
                val requestedDates = data.getJSONArray("dates")
                require(requestedDates.length() <= 90)
                val dates = (0 until requestedDates.length()).map { LocalDate.parse(requestedDates.getString(it)) }.distinct().sorted()
                if (dates.isEmpty()) return@launch
                require(dates.last() < date)
                require(java.time.temporal.ChronoUnit.DAYS.between(dates.first(), dates.last()) < 90)
                val calendar = dependencies.calendar()(dates.first(), dates.last()).getOrThrow()
                ensureActive()
                if (closing || accountId != expectedAccount || activeSession != sessionKey()) return@launch
                // Validate all requested days before sending any corrections.
                val rowsByDate = dates.associateWith { day -> ForestHostSnapshot.items(day, calendar.items.filter { it.occurrenceDate == day }) }
                for ((day, rows) in rowsByDate) {
                    ensureActive()
                    val key = "revision:$expectedAccount"
                    val revision = ForestHostSnapshot.nextRevision(preferences.getLong(key, 0), System.currentTimeMillis())
                    withContext(Dispatchers.IO) { check(preferences.edit().putLong(key, revision).commit()) }
                    val items = JSONArray()
                    rows.forEach { row -> items.put(JSONObject().put("id", row.id).put("title", row.title).put("category", row.category).put("isHabit", row.isHabit).put("isCompleted", row.isCompleted)) }
                    val snapshot = JSONObject().put("schemaVersion", 1).put("accountId", expectedAccount).put("date", day.toString())
                        .put("revision", revision).put("timeZone", ZoneId.systemDefault().id).put("items", items)
                    UnityPlayer.UnitySendMessage("AppRoot", "ReceiveHistoricalTodoSnapshot", snapshot.toString())
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Toast.makeText(this@ForestUnityActivity, "이전 기록을 새로 확인하지 못했어요. 저장된 기록으로 보여드려요.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun dispatch() {
        if (!ready || accountId == null || closing) return
        if (activeSession != sessionKey()) {
            if (requestedSession != sessionKey()) {
                requestedSession = sessionKey()
                requestedSessionId = UUID.randomUUID().toString()
                val json = JSONObject().put("accountId", accountId).put("date", date.toString()).put("timeZone", ZoneId.systemDefault().id)
                    .put("requestId", requestedSessionId)
                UnityPlayer.UnitySendMessage("AppRoot", "ReceiveHostSession", json.toString())
            }
            return
        }
        sendStatus()
        pendingSnapshot?.let {
            pendingSnapshot = null
            sendCorrelated("todo-snapshot", "ReceiveTodoSnapshot", JSONObject(it))
        }
    }

    private fun sendStatus() {
        if (!ready || activeSession != sessionKey() || closing) return
        // Success is established by ReceiveTodoSnapshot, never by a premature status message.
        if (syncState == "ready") return
        val json = JSONObject().put("accountId", accountId).put("date", date.toString()).put("timeZone", ZoneId.systemDefault().id)
            .put("status", syncState).put("message", syncMessage).put("lastConfirmedAtUtc", preferences.getString("confirmed:$accountId", ""))
        sendCorrelated("todo-sync", "ReceiveTodoSyncState", json)
    }

    private fun sendCorrelated(kind: String, method: String, json: JSONObject) {
        val requestId = UUID.randomUUID().toString()
        // Both kinds share one slot: a newer snapshot also supersedes an older loading failure.
        pendingReply = ForestHostRequest(kind, json.getString("accountId"), json.getString("date"), requestId)
        UnityPlayer.UnitySendMessage("AppRoot", method, json.put("requestId", requestId).toString())
    }

    private fun showCurtain(message: String, showActions: Boolean = false) {
        if (!::curtain.isInitialized) return
        curtainText.text = message
        curtainProgress.visibility = if (showActions) View.GONE else View.VISIBLE
        curtainActions.visibility = if (showActions) View.VISIBLE else View.GONE
        curtain.visibility = View.VISIBLE
        curtain.bringToFront()
    }
}
