using System;
using System.Diagnostics;
using System.Runtime.InteropServices;
using UnityEngine;
using Debug = UnityEngine.Debug;

namespace TodoSpirits.Spirit
{
    // Unity 는 켜지자마자 테두리 있는 일반 창을 잠깐 보여 준다(투명 오버레이로 바뀌기 전).
    // 다른 프로세스에서는 이 창을 숨기거나 옮길 수 없다(초기화 중에는 메시지를 받지 않고, DWM 숨김은 거부된다).
    // 그래서 스크립트가 돌 수 있는 가장 이른 시점에, 자기 창을 스스로 화면 밖으로 옮긴다.
    // 이후 SpiritOverlay 가 창을 숨긴 채 작업 영역으로 맞추고, 첫 장면을 그린 뒤에 보이게 한다.
    internal static class EarlyWindow
    {
        delegate bool WindowVisitor(IntPtr window, IntPtr data);
        [DllImport("user32.dll")] static extern bool EnumWindows(WindowVisitor visitor, IntPtr data);
        [DllImport("user32.dll")] static extern uint GetWindowThreadProcessId(IntPtr window, out uint processId);
        [DllImport("user32.dll")] static extern bool IsWindowVisible(IntPtr window);
        [DllImport("user32.dll")] static extern bool SetWindowPos(IntPtr window, IntPtr after, int x, int y, int width, int height, uint flags);

        [RuntimeInitializeOnLoadMethod(RuntimeInitializeLoadType.BeforeSplashScreen)]
        static void MoveOffscreen()
        {
            if (Application.isEditor) return;
            uint self;
            using (var process = Process.GetCurrentProcess())
            {
                self = (uint)process.Id;
                int moved = 0;
                EnumWindows((window, _) =>
                {
                    GetWindowThreadProcessId(window, out uint owner);
                    if (owner == self && IsWindowVisible(window))
                    {
                        // SWP_NOSIZE | SWP_NOZORDER | SWP_NOACTIVATE
                        SetWindowPos(window, IntPtr.Zero, -32000, -32000, 0, 0, 0x1 | 0x4 | 0x10);
                        moved++;
                    }
                    return true;
                }, IntPtr.Zero);
                Debug.Log($"[Spirit] early window moved offscreen: windows={moved}, since start={(DateTime.Now - process.StartTime).TotalMilliseconds:F0}ms");
            }
        }
    }
}
