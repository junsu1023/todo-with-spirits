using TodoSpirits.Spirit;
using NUnit.Framework;
using UnityEngine;

public sealed class SpiritMotionTests
{
    [TestCase(1f)]
    [TestCase(1.5f)]
    [TestCase(2f)]
    public void ReleasedSpiritSettlesAtWorkAreaFloor(float dpi)
    {
        var motion = new SpiritMotion { Feet = new Vector2(500 * dpi, 150 * dpi) };
        motion.Release(new Vector2(200, -500) * dpi);
        for (int i = 0; i < 600; i++) motion.Step(1f / 30, 1280 * dpi, 900 * dpi, 96 * dpi, dpi);
        Assert.That(motion.Grounded, Is.True);
        Assert.That(motion.Feet.y, Is.EqualTo(900 * dpi).Within(.01));
        Assert.That(motion.Feet.x, Is.InRange(48 * dpi, 1232 * dpi));
        Assert.That(motion.Velocity, Is.EqualTo(Vector2.zero));
    }

    [Test]
    public void ReleaseLeavesIdleAndWorkAreaResizeMovesFloor()
    {
        var motion = new SpiritMotion { Feet = new Vector2(500, 900) };
        motion.Release(Vector2.zero);
        motion.Step(1f / 30, 1280, 900, 96, 1);
        Assert.That(motion.Grounded, Is.True);
        motion.Step(1f / 30, 1280, 850, 96, 1);
        Assert.That(motion.Feet.y, Is.EqualTo(850));
        motion.Feet = new Vector2(500, 300);
        motion.Release(Vector2.zero);
        Assert.That(motion.Grounded, Is.False);
        motion.Step(1f / 30, 1280, 850, 96, 1);
        Assert.That(motion.Feet.y, Is.GreaterThan(300).And.LessThan(850));
    }

    [TestCase(false, 1f)]
    [TestCase(true, 2f)]
    public void FullscreenReturnPeeksThenWalksInFromNearestEdge(bool fromRight, float dpi)
    {
        float width = 1280 * dpi, floor = 900 * dpi, size = 96 * dpi;
        var motion = new SpiritMotion { Feet = new Vector2(width * (fromRight ? .75f : .25f), floor) };
        float DistanceFromEdge() => fromRight ? width - motion.Feet.x : motion.Feet.x;
        void Advance(int frames)
        {
            for (int i = 0; i < frames && motion.IsEntering; i++)
                motion.StepEntrance(1f / 30, width, floor, size);
        }

        motion.BeginEntrance(width);
        motion.StepEntrance(0, width, floor, size);
        Assert.That(DistanceFromEdge(), Is.EqualTo(-size / 2).Within(.01));
        Advance(8);
        float peek = DistanceFromEdge();
        Assert.That(peek, Is.InRange(-size / 2, 0f));
        Assert.That(motion.Feet.y, Is.EqualTo(floor));
        Advance(4);
        Assert.That(DistanceFromEdge(), Is.EqualTo(peek));
        Advance(12);
        Assert.That(DistanceFromEdge(), Is.GreaterThan(0));
        Assert.That(motion.Feet.y, Is.LessThan(floor));
        Assert.That(motion.IsEntering, Is.True);
        Advance(24);
        Assert.That(motion.IsEntering, Is.False);
        Assert.That(motion.Grounded, Is.True);
        Assert.That(motion.Feet.y, Is.EqualTo(floor));
        float end = DistanceFromEdge();
        Assert.That(end, Is.GreaterThan(size / 2));
        motion.Walk(1f / 30, width, size, dpi);
        Assert.That(DistanceFromEdge(), Is.GreaterThan(end));
    }
}
