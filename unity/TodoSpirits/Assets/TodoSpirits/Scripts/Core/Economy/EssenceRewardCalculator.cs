using System;
using System.Collections.Generic;

namespace TodoSpirits.Core
{
    public static class EssenceRewardCalculator
    {
        public const int DailyMaximum = 25;

        public static int Calculate(int completedTaskCount)
        {
            if (completedTaskCount <= 0)
            {
                return 0;
            }

            if (completedTaskCount == 1)
            {
                return 10;
            }

            if (completedTaskCount <= 3)
            {
                return 18;
            }

            return DailyMaximum;
        }

        public static EssenceRewardBand GetBand(int completedTaskCount)
        {
            if (completedTaskCount <= 0)
            {
                return EssenceRewardBand.None;
            }

            if (completedTaskCount == 1)
            {
                return EssenceRewardBand.OneTask;
            }

            if (completedTaskCount <= 3)
            {
                return EssenceRewardBand.TwoToThreeTasks;
            }

            return EssenceRewardBand.FourOrMoreTasks;
        }

        public static EssenceRewardBand GetBandForRewardAmount(int rewardAmount)
        {
            if (rewardAmount <= 0)
            {
                return EssenceRewardBand.None;
            }

            if (rewardAmount <= 10)
            {
                return EssenceRewardBand.OneTask;
            }

            if (rewardAmount <= 18)
            {
                return EssenceRewardBand.TwoToThreeTasks;
            }

            return EssenceRewardBand.FourOrMoreTasks;
        }
    }

    [Serializable]
    public sealed class EssenceWallet
    {
        public int Balance;
        public List<RewardBandByDate> HighestRewardBandByDate;

        public EssenceWallet()
        {
            HighestRewardBandByDate = new List<RewardBandByDate>();
        }

        public EssenceWallet(int balance, IEnumerable<RewardBandByDate> highestRewardBandByDate)
        {
            Balance = Math.Max(0, balance);
            HighestRewardBandByDate = highestRewardBandByDate == null
                ? new List<RewardBandByDate>()
                : new List<RewardBandByDate>(highestRewardBandByDate);
        }

        public int ApplyCompletedTaskCount(string date, int completedTaskCount)
        {
            return ApplyReward(
                date,
                EssenceRewardCalculator.Calculate(completedTaskCount),
                EssenceRewardCalculator.GetBand(completedTaskCount));
        }

        public int ApplyReward(string date, int reward)
        {
            return ApplyReward(
                date,
                reward,
                EssenceRewardCalculator.GetBandForRewardAmount(reward));
        }

        public int ApplyReward(string date, int reward, EssenceRewardBand band)
        {
            if (string.IsNullOrWhiteSpace(date))
            {
                throw new ArgumentException("날짜는 비어 있을 수 없습니다.", nameof(date));
            }

            if (reward < 0 || reward > EssenceRewardCalculator.DailyMaximum)
            {
                throw new ArgumentOutOfRangeException(
                    nameof(reward),
                    "일일 정수 보상은 0 이상 25 이하여야 합니다.");
            }

            EnsureRewardHistory();
            var history = FindRewardBand(date);
            if (history == null)
            {
                history = new RewardBandByDate(date, 0, EssenceRewardBand.None);
                HighestRewardBandByDate.Add(history);
            }

            var inferredBand = EssenceRewardCalculator.GetBandForRewardAmount(reward);
            var effectiveBand = (int)band > (int)inferredBand ? band : inferredBand;
            if ((int)effectiveBand > (int)history.HighestBand)
            {
                history.HighestBand = effectiveBand;
            }

            if (reward <= history.HighestReward)
            {
                return 0;
            }

            var delta = reward - history.HighestReward;
            history.HighestReward = reward;
            Balance += delta;
            return delta;
        }

        public int GetHighestReward(string date)
        {
            EnsureRewardHistory();
            var history = FindRewardBand(date);
            return history == null ? 0 : history.HighestReward;
        }

        public EssenceRewardBand GetHighestBand(string date)
        {
            EnsureRewardHistory();
            var history = FindRewardBand(date);
            return history == null ? EssenceRewardBand.None : history.HighestBand;
        }

        public RewardBandByDate GetRewardBandByDate(string date)
        {
            EnsureRewardHistory();
            return FindRewardBand(date);
        }

        private RewardBandByDate FindRewardBand(string date)
        {
            for (var i = 0; i < HighestRewardBandByDate.Count; i++)
            {
                var entry = HighestRewardBandByDate[i];
                if (entry != null && string.Equals(entry.Date, date, StringComparison.Ordinal))
                {
                    return entry;
                }
            }

            return null;
        }

        private void EnsureRewardHistory()
        {
            if (HighestRewardBandByDate == null)
            {
                HighestRewardBandByDate = new List<RewardBandByDate>();
            }
        }
    }
}
