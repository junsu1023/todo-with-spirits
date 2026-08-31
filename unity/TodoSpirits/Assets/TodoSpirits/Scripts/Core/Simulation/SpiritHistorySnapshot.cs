using System;
using System.Collections.Generic;

namespace TodoSpirits.Core
{
    public static class SpiritHistorySnapshot
    {
        public static SpiritState Build(
            SpiritState baseState,
            IEnumerable<DailyCompanionRecord> records,
            string targetDate,
            int recentActionLimit = 3)
        {
            if (baseState == null)
            {
                throw new ArgumentNullException(nameof(baseState));
            }

            if (string.IsNullOrWhiteSpace(targetDate))
            {
                throw new ArgumentException("대상 날짜는 비어 있을 수 없습니다.", nameof(targetDate));
            }

            if (recentActionLimit < 0)
            {
                throw new ArgumentOutOfRangeException(
                    nameof(recentActionLimit),
                    "최근 행동 개수는 0 이상이어야 합니다.");
            }

            var matchingRecords = new List<DailyCompanionRecord>();
            if (records != null)
            {
                foreach (var record in records)
                {
                    if (IsPriorRecordForSpirit(record, baseState.SpiritId, targetDate))
                    {
                        matchingRecords.Add(record);
                    }
                }
            }

            matchingRecords.Sort(CompareRecordsChronologically);
            var recentActions = new List<SpiritActionId>();
            var firstRecentIndex = Math.Max(0, matchingRecords.Count - recentActionLimit);
            for (var i = firstRecentIndex; i < matchingRecords.Count; i++)
            {
                recentActions.Add(matchingRecords[i].SpiritDay.PrimaryAction);
            }

            return new SpiritState(
                baseState.SpiritId,
                baseState.Temperaments,
                recentActions,
                baseState.FavoriteAction);
        }

        private static bool IsPriorRecordForSpirit(
            DailyCompanionRecord record,
            string spiritId,
            string targetDate)
        {
            return record != null &&
                   record.SpiritDay != null &&
                   !string.IsNullOrWhiteSpace(record.Date) &&
                   string.Equals(record.SpiritId, spiritId, StringComparison.Ordinal) &&
                   string.Compare(record.Date, targetDate, StringComparison.Ordinal) < 0;
        }

        private static int CompareRecordsChronologically(
            DailyCompanionRecord left,
            DailyCompanionRecord right)
        {
            var dateComparison = string.Compare(left.Date, right.Date, StringComparison.Ordinal);
            if (dateComparison != 0)
            {
                return dateComparison;
            }

            var seedComparison = left.SpiritDay.Seed.CompareTo(right.SpiritDay.Seed);
            return seedComparison != 0
                ? seedComparison
                : left.SpiritDay.PrimaryAction.CompareTo(right.SpiritDay.PrimaryAction);
        }
    }
}
