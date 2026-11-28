using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using Kirurobo;
using TodoSpirits.Contracts;
using UnityEngine;
using Debug = UnityEngine.Debug;

namespace TodoSpirits.Spirit
{
    [DefaultExecutionOrder(100)]
    public sealed partial class SpiritOverlay : MonoBehaviour
    {
        public Camera Camera;
        public SpriteRenderer Body;
        public UniWindowController Window;
        readonly SpiritMotion motion = new SpiritMotion();
        readonly List<Vector3> trail = new List<Vector3>(16);
        Color32[] pixels;
        int textureWidth, textureHeight;
        IntPtr handle;
        IntPtr currentMonitor;
        WindowsDesktop.Rect area;
        Vector2 grabOffset, downPoint;
        bool ready, pressed, grabbed, moved;
        bool menuPressed, menuGrabbed;
        Vector2 bodyStretch = Vector2.one;
        SpiritExpression expression;
        float dpiScale = 1, nextLayout, nextEvidence;
        string evidencePath;
        int dragCount, releaseCount, landingCount;
        SpiritLink link;
        float nextHeartbeat;
        bool spiritVisible = true, verifyClick;
        float Size => 96 * dpiScale;

        IEnumerator Start()
        {
            if (Application.isEditor)
            {
                Debug.Log("[Spirit] Native overlay requires a Windows standalone Player.");
                enabled = false;
                yield break;
            }
            Application.runInBackground = true;
            QualitySettings.vSyncCount = 0;
            Application.targetFrameRate = 30;
            Window.isHitTestEnabled = false;
            Window.hitTestType = UniWindowController.HitTestType.None;
            Window.isTransparent = true;
            Window.isTopmost = true;
            Window.isClickThrough = true;
            var texture = Body.sprite.texture;
            pixels = texture.GetPixels32(); // cached CPU alpha: no per-frame GPU readback or full-screen collider
            textureWidth = texture.width;
            textureHeight = texture.height;
            expression = new SpiritExpression(Body);
            var args = Environment.GetCommandLineArgs();
            for (int i = 0; i + 1 < args.Length; i++)
                if (args[i] == "--evidence") evidencePath = Path.GetFullPath(args[i + 1]);
            string pipeName = null; int parentId = 0;
            for (int i = 0; i < args.Length; i++)
            {
                if (args[i] == "--spirit-pipe" && i + 1 < args.Length) pipeName = args[++i];
                else if (args[i] == "--spirit-parent" && i + 1 < args.Length) int.TryParse(args[++i], out parentId);
                else if (args[i] == "--spirit-verify-click") verifyClick = true;
                else if (args[i] == "--spirit-verify-gestures" && i + 1 < args.Length) gestureReport = Path.GetFullPath(args[++i]);
            }

            // UniWindowController attaches in Update. Wait for the Player's own window, never another app's HWND.
            for (int attempt = 0; attempt < 180 && handle == IntPtr.Zero; attempt++)
            {
                yield return null;
                using (var process = Process.GetCurrentProcess()) handle = process.MainWindowHandle;
            }
            if (handle == IntPtr.Zero) throw new InvalidOperationException("Player window not found.");
            // 켜질 때 빈 창이 보이지 않게: 창을 찾자마자 숨기고, 자리를 잡아 첫 장면을 그린 뒤에 보인다.
            WindowsDesktop.SetVisible(handle, false);
            yield return null;
            RefreshLayout();
            motion.Feet = new Vector2(area.Width * .65f, area.Height * .35f);
            motion.Release(Vector2.zero);
            // A button held while launching must not become a new drag.
            pressed = (WindowsDesktop.GetAsyncKeyState(1) & 0x8000) != 0;
            menuPressed = (WindowsDesktop.GetAsyncKeyState(2) & 0x8000) != 0;
            Render();
            yield return null;
            WindowsDesktop.SetVisible(handle, true);
            ready = true;
            if (pipeName != null && parentId > 0) link = new SpiritLink(pipeName, parentId);
            Debug.Log($"[Spirit] ready renderer={SystemInfo.graphicsDeviceType}, work={area.Width}x{area.Height}, dpi={dpiScale * 96}");
        }

        void RefreshLayout(IntPtr monitor = default)
        {
            if (monitor == IntPtr.Zero)
                monitor = WindowsDesktop.NearestMonitor(handle);
            var previousArea = area;
            area = WindowsDesktop.WorkArea(monitor);
            if (!WindowsDesktop.GetWindowRect(handle, out var rect) || rect.Left != area.Left || rect.Top != area.Top || rect.Width != area.Width || rect.Height != area.Height)
                WindowsDesktop.FitToWorkArea(handle, area);
            uint dpi = WindowsDesktop.GetDpiForWindow(handle);
            float previousScale = dpiScale;
            dpiScale = (dpi == 0 ? 96 : dpi) / 96f;
            if (currentMonitor != IntPtr.Zero)
            {
                var localFeet = motion.Feet + new Vector2(previousArea.Left - area.Left, previousArea.Top - area.Top);
                motion.Feet = ClampFeet(localFeet);
                if (grabbed)
                    grabOffset *= dpiScale / previousScale;
            }
            if (currentMonitor != monitor || previousScale != dpiScale || previousArea.Height != area.Height)
                Debug.Log($"[Spirit] layout work={area.Left},{area.Top},{area.Width}x{area.Height}; dpi={dpiScale * 96}");
            currentMonitor = monitor;
            nextLayout = Time.unscaledTime + .5f;
        }

        Vector2 ClampFeet(Vector2 position)
        {
            float halfWidth = Mathf.Min(Size / 2, area.Width / 2f);
            return new Vector2(
                Mathf.Clamp(position.x, halfWidth, area.Width - halfWidth),
                Mathf.Clamp(position.y, Mathf.Min(Size, area.Height), area.Height));
        }

        void ApplyVisibility(bool visible)
        {
            if (visible == spiritVisible)
                return;
            spiritVisible = visible;
            grabbed = false;
            menuGrabbed = false;
            pressed = (WindowsDesktop.GetAsyncKeyState(1) & 0x8000) != 0;
            menuPressed = (WindowsDesktop.GetAsyncKeyState(2) & 0x8000) != 0;
            expression.SetDragging(false);
            Window.isClickThrough = true;
            if (!visible)
                WindowsDesktop.SetVisible(handle, false);
            Camera.enabled = visible;
            Body.enabled = visible;
            Application.targetFrameRate = visible ? 30 : 5;
            if (visible)
            {
                RefreshLayout();
                motion.BeginEntrance(area.Width);
                motion.StepEntrance(0, area.Width, area.Height, Size);
                bodyStretch = Vector2.one;
                Render();
                WindowsDesktop.SetVisible(handle, true);
                Debug.Log($"[Spirit] fullscreen return started from {(motion.Feet.x < 0 ? "left" : "right")}");
            }
        }

        void Update()
        {
            if (!ready) return;
            if (link != null)
            {
                if (link.ShouldQuit) { Application.Quit(); return; }
                while (link.TryReceive(out var message))
                {
                    if (message.Kind == SpiritMessageKind.Shutdown) { Application.Quit(); return; }
                    if (message.Kind == SpiritMessageKind.Visibility)
                        ApplyVisibility(message.Visible);
                }
                if (Time.unscaledTime >= nextHeartbeat)
                {
                    link.Send(new SpiritMessage { Kind = SpiritMessageKind.Heartbeat, Visible = spiritVisible });
                    nextHeartbeat = Time.unscaledTime + 2;
                }
                if (verifyClick && link.Connected && spiritVisible && motion.Grounded)
                { verifyClick = false; SendClick(); Debug.Log("[Spirit] explicit verification click (not OS input)"); }
            }
            if (!spiritVisible)
                return;
            if (Time.unscaledTime >= nextLayout) RefreshLayout();
            if (motion.IsEntering)
            {
                motion.StepEntrance(Time.unscaledDeltaTime, area.Width, area.Height, Size);
                Render();
                if (!motion.IsEntering)
                {
                    // A press that started during the entrance belongs to the window behind us.
                    pressed = (WindowsDesktop.GetAsyncKeyState(1) & 0x8000) != 0;
                    menuPressed = (WindowsDesktop.GetAsyncKeyState(2) & 0x8000) != 0;
                    Debug.Log("[Spirit] fullscreen return completed");
                }
                return;
            }
            if (checkingGestures) return;
            if (gestureReport != null && link?.Connected == true && motion.Grounded && !grabbed)
            {
                StartCoroutine(CheckGestures());
                return;
            }
            if (!WindowsDesktop.GetCursorPos(out var cursor)) return;
            bool down = (WindowsDesktop.GetAsyncKeyState(1) & 0x8000) != 0;
            bool menuDown = (WindowsDesktop.GetAsyncKeyState(2) & 0x8000) != 0;
            UpdatePointer(cursor, down, menuDown);
        }

        void UpdatePointer(WindowsDesktop.Point cursor, bool down, bool menuDown)
        {
            var point = new Vector2(cursor.X - area.Left, cursor.Y - area.Top);
            if (grabbed && down && !moved && Vector2.Distance(point, downPoint) > 8 * dpiScale)
            {
                moved = true;
                dragCount++;
            }
            if (grabbed && moved && down)
            {
                var cursorMonitor = WindowsDesktop.NearestMonitor(cursor);
                if (cursorMonitor != currentMonitor)
                {
                    RefreshLayout(cursorMonitor);
                    point = new Vector2(cursor.X - area.Left, cursor.Y - area.Top);
                }
            }
            bool hit = spiritVisible && Hit(point);
            if (menuDown && !menuPressed && !down && hit && !Window.isClickThrough)
                menuGrabbed = true;
            if (!menuDown && menuGrabbed)
            {
                menuGrabbed = false;
                if (hit)
                    SendClick(SpiritMessageKind.MenuRequested);
            }
            menuPressed = menuDown;
            if (down && !pressed && !menuDown && hit && !Window.isClickThrough)
            {
                grabbed = true;
                moved = false;
                grabOffset = motion.Feet - point;
                downPoint = point;
                trail.Clear();
                Debug.Log("[Spirit] pointer-down on spirit");
            }
            if (grabbed && down && moved)
            {
                motion.Feet = ClampFeet(point + grabOffset);
                if (trail.Count == 16) trail.RemoveAt(0);
                trail.Add(new Vector3(motion.Feet.x + area.Left, motion.Feet.y + area.Top, Time.unscaledTime));
            }
            if (grabbed && !down)
            {
                Vector2 velocity = Vector2.zero;
                if (moved && trail.Count > 1)
                {
                    var last = trail[trail.Count - 1];
                    var first = trail[0];
                    foreach (var sample in trail) if (last.z - sample.z > .1f) first = sample;
                    float dt = Time.unscaledTime - first.z; // holding still before release reduces throw speed
                    var desktopFeet = motion.Feet + new Vector2(area.Left, area.Top);
                    if (dt > .001f) velocity = Vector2.ClampMagnitude((desktopFeet - new Vector2(first.x, first.y)) / dt, 3000 * dpiScale);
                }
                grabbed = false;
                releaseCount++;
                if (!moved && hit) SendClick();
                motion.Release(velocity);
                Debug.Log($"[Spirit] release velocity={velocity}");
            }
            pressed = down;
            bool wasGrounded = motion.Grounded;
            if (!grabbed) motion.Step(Time.unscaledDeltaTime, area.Width, area.Height, Size, dpiScale);
            if (spiritVisible && !grabbed && !menuGrabbed && Vector2.Distance(point, motion.Feet) > Size * 1.2f)
                motion.Walk(Time.unscaledDeltaTime, area.Width, Size, dpiScale);
            if (!wasGrounded && motion.Grounded) { landingCount++; Debug.Log($"[Spirit] landed feet={motion.Feet}, floor={area.Height}"); }
            Render();
            // Keep capture through the entire drag, including transparent pixels and screen edges.
            // A gesture begun in another app must never be stolen when it crosses the sprite.
            Window.isClickThrough = !grabbed && !menuGrabbed && (!spiritVisible || !Hit(point) || down || menuDown);
            if (evidencePath != null && Time.unscaledTime >= nextEvidence)
            {
                SaveEvidence(cursor);
                nextEvidence = Time.unscaledTime + .2f;
            }
        }

        void SendClick(SpiritMessageKind kind = SpiritMessageKind.Clicked)
        {
            // Top-left of the sprite texture in physical desktop pixels, not Unity screen coordinates.
            link?.Send(new SpiritMessage { Kind = kind, SpiritId = "spirit-1",
                X = area.Left + motion.Feet.x - Size / 2,
                Y = area.Top + motion.Feet.y - Size * (1 - Body.sprite.pivot.y / Body.sprite.rect.height),
                Width = Size, Height = Size });
            Debug.Log(kind == SpiritMessageKind.MenuRequested ? "[Spirit] spirit-menu" : "[Spirit] spirit-clicked");
        }

        void Render()
        {
            Camera.orthographicSize = area.Height / 2f;
            float scale = Size / (Body.sprite.rect.height / Body.sprite.pixelsPerUnit);
            bool dragging = grabbed && moved;
            var stretch = dragging ? new Vector2(.88f, 1.18f) : Vector2.one;
            bodyStretch = Vector2.Lerp(bodyStretch, stretch, Time.unscaledDeltaTime * 12);
            Body.transform.localScale = new Vector3(scale * bodyStretch.x, scale * bodyStretch.y, scale);
            Body.transform.position = new Vector3(motion.Feet.x - area.Width / 2f, area.Height / 2f - motion.Feet.y, 0);
            expression.SetDragging(dragging);
        }

        bool Hit(Vector2 point)
        {
            // Read only this one unrotated sprite's cached alpha. Future multi-spirit hit testing is a separate gate.
            var sprite = Body.sprite;
            float x = (point.x - motion.Feet.x) * sprite.rect.height / (Size * bodyStretch.x) + sprite.pivot.x;
            float y = (motion.Feet.y - point.y) * sprite.rect.height / (Size * bodyStretch.y) + sprite.pivot.y;
            if (x < 0 || y < 0 || x >= sprite.rect.width || y >= sprite.rect.height) return false;
            int px = Mathf.FloorToInt(sprite.rect.x + x), py = Mathf.FloorToInt(sprite.rect.y + y);
            return px >= 0 && py >= 0 && px < textureWidth && py < textureHeight && pixels[py * textureWidth + px].a >= 26;
        }

        [Serializable] sealed class Evidence
        {
            public string utc, state, graphics;
            public int workLeft, workTop, workWidth, workHeight, cursorX, cursorY, dragCount, releaseCount, landingCount, screenWidth, screenHeight;
            public float feetX, feetY, floor, dpiScale;
            public bool clickThrough, transparent, topmost, nativeVisible, nativeTopmost, nativeClickThrough, nativeHitIsPlayer;
        }

        void SaveEvidence(WindowsDesktop.Point cursor)
        {
            var e = new Evidence
            {
                utc = DateTime.UtcNow.ToString("O"), state = grabbed ? (moved ? "Dragging" : "Pressed") : motion.Grounded ? "Idle" : "Falling",
                graphics = SystemInfo.graphicsDeviceType.ToString(), workLeft = area.Left, workTop = area.Top, workWidth = area.Width, workHeight = area.Height,
                feetX = motion.Feet.x + area.Left, feetY = motion.Feet.y + area.Top, floor = area.Bottom, dpiScale = dpiScale,
                cursorX = cursor.X, cursorY = cursor.Y, clickThrough = Window.isClickThrough, transparent = Window.isTransparent, topmost = Window.isTopmost,
                dragCount = dragCount, releaseCount = releaseCount, landingCount = landingCount, screenWidth = Screen.width, screenHeight = Screen.height,
                nativeVisible = WindowsDesktop.IsWindowVisible(handle),
                nativeTopmost = (WindowsDesktop.GetWindowStyle(handle, -20).ToInt64() & 8) != 0,
                nativeClickThrough = (WindowsDesktop.GetWindowStyle(handle, -20).ToInt64() & 0x20) != 0,
                nativeHitIsPlayer = WindowsDesktop.WindowFromPoint(cursor) == handle
            };
            Directory.CreateDirectory(Path.GetDirectoryName(evidencePath));
            File.WriteAllText(evidencePath, JsonUtility.ToJson(e, true));
        }

        void OnApplicationQuit() { if (Window) Window.isClickThrough = true; link?.Dispose(); link = null; }
        void OnDestroy() => expression?.Dispose();
    }
}
