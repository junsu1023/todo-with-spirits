using NUnit.Framework;

namespace TodoSpirits.Core.Tests
{
    public sealed class EssenceRewardCalculatorTests
    {
        [TestCase(0, 0, EssenceRewardBand.None)]
        [TestCase(1, 10, EssenceRewardBand.OneTask)]
        [TestCase(2, 18, EssenceRewardBand.TwoToThreeTasks)]
        [TestCase(3, 18, EssenceRewardBand.TwoToThreeTasks)]
        [TestCase(4, 25, EssenceRewardBand.FourOrMoreTasks)]
        [TestCase(99, 25, EssenceRewardBand.FourOrMoreTasks)]
        public void Calculate_UsesPrototypeBands(
            int completedTaskCount,
            int expectedReward,
            EssenceRewardBand expectedBand)
        {
            Assert.That(
                EssenceRewardCalculator.Calculate(completedTaskCount),
                Is.EqualTo(expectedReward));
            Assert.That(
                EssenceRewardCalculator.GetBand(completedTaskCount),
                Is.EqualTo(expectedBand));
        }

        [Test]
        public void ApplyCompletedTaskCount_GrantsOnlyDifferenceFromHighestDailyReward()
        {
            var wallet = new EssenceWallet();
            const string date = "2026-08-31";

            Assert.That(wallet.ApplyCompletedTaskCount(date, 1), Is.EqualTo(10));
            Assert.That(wallet.ApplyCompletedTaskCount(date, 1), Is.Zero);
            Assert.That(wallet.ApplyCompletedTaskCount(date, 3), Is.EqualTo(8));
            Assert.That(wallet.ApplyCompletedTaskCount(date, 2), Is.Zero);
            Assert.That(wallet.ApplyCompletedTaskCount(date, 4), Is.EqualTo(7));

            Assert.That(wallet.Balance, Is.EqualTo(25));
            Assert.That(wallet.GetHighestReward(date), Is.EqualTo(25));
            Assert.That(wallet.GetHighestBand(date), Is.EqualTo(EssenceRewardBand.FourOrMoreTasks));
        }
    }
}
