using System;
using System.Runtime.InteropServices;

namespace TodoSpirits.Spirit
{
    // All coordinates here are physical desktop pixels, origin top-left (not Unity window coordinates).
    internal static class WindowsDesktop
    {
        [StructLayout(LayoutKind.Sequential)] internal struct Point { public int X, Y; }
        [StructLayout(LayoutKind.Sequential)] internal struct Rect
        {
            public int Left, Top, Right, Bottom;
            public int Width => Right - Left;
            public int Height => Bottom - Top;
        }
        [StructLayout(LayoutKind.Sequential)] struct MonitorInfo
        {
            public int Size;
            public Rect Monitor, Work;
            public uint Flags;
        }
        [DllImport("user32.dll")] internal static extern bool GetCursorPos(out Point point);
        [DllImport("user32.dll")] internal static extern short GetAsyncKeyState(int key);
        [DllImport("user32.dll")] static extern IntPtr MonitorFromPoint(Point point, uint flags);
        [DllImport("user32.dll")] static extern IntPtr MonitorFromWindow(IntPtr window, uint flags);
        [return: MarshalAs(UnmanagedType.Bool)]
        delegate bool MonitorVisitor(IntPtr monitor, IntPtr device, IntPtr bounds, IntPtr data);
        [DllImport("user32.dll")] static extern bool EnumDisplayMonitors(IntPtr device, IntPtr bounds, MonitorVisitor visitor, IntPtr data);
        [DllImport("user32.dll", CharSet = CharSet.Auto)] static extern bool GetMonitorInfo(IntPtr monitor, ref MonitorInfo info);
        [DllImport("user32.dll")] internal static extern uint GetDpiForWindow(IntPtr window);
        [DllImport("user32.dll")] internal static extern bool GetWindowRect(IntPtr window, out Rect rect);
        [DllImport("user32.dll")] internal static extern bool GetClientRect(IntPtr window, out Rect rect);
        [DllImport("user32.dll")] internal static extern bool ClientToScreen(IntPtr window, ref Point point);
        [DllImport("user32.dll")] internal static extern bool IsWindowVisible(IntPtr window);
        [DllImport("user32.dll", EntryPoint = "GetWindowLongPtrW")] internal static extern IntPtr GetWindowStyle(IntPtr window, int index);
        [DllImport("user32.dll")] internal static extern IntPtr WindowFromPoint(Point point);
        [DllImport("user32.dll")] static extern bool ShowWindow(IntPtr window, int command);
        [DllImport("user32.dll", SetLastError = true)] static extern bool SetWindowPos(IntPtr window, IntPtr after, int x, int y, int width, int height, uint flags);

        internal static IntPtr NearestMonitor(Point point) => MonitorFromPoint(point, 2);

        internal static IntPtr NearestMonitor(IntPtr window) => MonitorFromWindow(window, 2);

        internal static IntPtr FindAnotherMonitor(IntPtr current)
        {
            IntPtr other = IntPtr.Zero;
            EnumDisplayMonitors(IntPtr.Zero, IntPtr.Zero, (monitor, _, _, _) =>
            {
                if (monitor == current) return true;
                other = monitor;
                return false;
            }, IntPtr.Zero);
            return other;
        }

        internal static Rect WorkArea(IntPtr monitor)
        {
            var info = new MonitorInfo { Size = Marshal.SizeOf<MonitorInfo>() };
            if (!GetMonitorInfo(monitor, ref info)) throw new InvalidOperationException("Cannot read monitor work area.");
            return info.Work;
        }

        internal static void SetVisible(IntPtr window, bool visible) => ShowWindow(window, visible ? 4 : 0);

        internal static void FitToWorkArea(IntPtr window, Rect area)
        {
            if (!SetWindowPos(window, new IntPtr(-1), area.Left, area.Top, area.Width, area.Height, 0x0010 | 0x0020))
                throw new System.ComponentModel.Win32Exception(Marshal.GetLastWin32Error());
        }
    }
}
