using System;
using System.Collections;
using System.IO;
using UnityEngine;

namespace TodoSpirits.Spirit
{
    public sealed partial class SpiritOverlay
    {
        string gestureReport;
        bool checkingGestures;

        // This is enabled only by the desktop verifier. It does not move the OS pointer.
        IEnumerator CheckGestures()
        {
            string reportPath = gestureReport;
            gestureReport = null;
            checkingGestures = true;

            var start = SpriteCenter();
            UpdatePointer(start, down: false, menuDown: false);
            yield return null;
            UpdatePointer(start, down: false, menuDown: true);
            bool menuGrabSucceeded = menuGrabbed;
            yield return null;
            UpdatePointer(start, down: false, menuDown: false);
            yield return new WaitForSecondsRealtime(0.5f);

            IntPtr firstMonitor = currentMonitor;
            IntPtr secondMonitor = WindowsDesktop.FindAnotherMonitor(firstMonitor);
            int firstWorkLeft = area.Left;
            int firstWorkTop = area.Top;
            int secondWorkLeft = firstWorkLeft;
            int secondWorkTop = firstWorkTop;
            int startDragCount = dragCount;
            int startReleaseCount = releaseCount;
            bool reachedSecond = false;
            bool reachedFirst = false;

            if (secondMonitor != IntPtr.Zero)
            {
                var secondArea = WindowsDesktop.WorkArea(secondMonitor);
                secondWorkLeft = secondArea.Left;
                secondWorkTop = secondArea.Top;
                yield return DragAndLand(secondMonitor);
                reachedSecond = IsLandedOn(secondMonitor);
                yield return DragAndLand(firstMonitor);
                reachedFirst = IsLandedOn(firstMonitor);
            }

            File.WriteAllText(reportPath, JsonUtility.ToJson(new GestureResult
            {
                menuGrabSucceeded = menuGrabSucceeded,
                hasSecondMonitor = secondMonitor != IntPtr.Zero,
                reachedSecond = reachedSecond,
                reachedFirst = reachedFirst,
                firstWorkLeft = firstWorkLeft,
                firstWorkTop = firstWorkTop,
                secondWorkLeft = secondWorkLeft,
                secondWorkTop = secondWorkTop,
                drags = dragCount - startDragCount,
                releases = releaseCount - startReleaseCount
            }, true));
            checkingGestures = false;
            SendClick(); // Continue with the existing schedule popup verification.
        }

        IEnumerator DragAndLand(IntPtr monitor)
        {
            var start = SpriteCenter();
            UpdatePointer(start, down: false, menuDown: false);
            yield return null;
            UpdatePointer(start, down: true, menuDown: false);
            yield return null;

            var targetArea = WindowsDesktop.WorkArea(monitor);
            var target = new WindowsDesktop.Point { X = targetArea.Left + targetArea.Width / 2, Y = targetArea.Top + targetArea.Height / 2 };
            // Cross the edge in a single sample, then hold still before release.
            for (int frame = 0; frame < 6; frame++)
            {
                UpdatePointer(target, down: true, menuDown: false);
                yield return null;
            }
            UpdatePointer(target, down: false, menuDown: false);

            float deadline = Time.unscaledTime + 4;
            while (!motion.Grounded && Time.unscaledTime < deadline)
            {
                yield return null;
                UpdatePointer(target, down: false, menuDown: false);
            }
        }

        WindowsDesktop.Point SpriteCenter() => new WindowsDesktop.Point
        {
            X = area.Left + Mathf.RoundToInt(motion.Feet.x),
            Y = area.Top + Mathf.RoundToInt(motion.Feet.y - Size / 2)
        };

        bool IsLandedOn(IntPtr monitor) => currentMonitor == monitor && motion.Grounded &&
            Mathf.Abs(motion.Feet.y - area.Height) < 1 && WindowsDesktop.GetWindowRect(handle, out var bounds) &&
            bounds.Left == area.Left && bounds.Top == area.Top && bounds.Width == area.Width && bounds.Height == area.Height;

        [Serializable]
        sealed class GestureResult
        {
            public bool menuGrabSucceeded, hasSecondMonitor, reachedSecond, reachedFirst;
            public int firstWorkLeft, firstWorkTop, secondWorkLeft, secondWorkTop, drags, releases;
        }
    }
}
