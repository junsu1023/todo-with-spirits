using System;
using System.Collections.Generic;
using TodoSpirits.Core;

namespace TodoSpirits.Runtime
{
    [Serializable]
    public sealed class PrototypeSaveData
    {
        public SpiritState SpiritState;
        public EssenceWallet EssenceWallet;
        public List<DailyCompanionRecord> Records;

        public PrototypeSaveData()
        {
            Records = new List<DailyCompanionRecord>();
        }

        public void EnsureCollections()
        {
            if (Records == null)
            {
                Records = new List<DailyCompanionRecord>();
            }
        }
    }
}
