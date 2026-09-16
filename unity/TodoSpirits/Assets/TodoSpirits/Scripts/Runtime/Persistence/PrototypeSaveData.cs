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
        public List<CompanionLife> Companions = new List<CompanionLife>();
        public string ActiveCompanionId;
        public string SessionDate;
        public CompanionInterventions Interventions = new CompanionInterventions();
        public TaskClassificationMemory ClassificationMemory = new TaskClassificationMemory();

        public PrototypeSaveData()
        {
            Records = new List<DailyCompanionRecord>();
        }

        public void EnsureCollections()
        {
            if (ClassificationMemory == null) ClassificationMemory = new TaskClassificationMemory();
            ClassificationMemory.EnsureCollections();
            if (Interventions == null) Interventions = new CompanionInterventions();
            Interventions.EnsureCollections();
            if (Companions == null) Companions = new List<CompanionLife>();
            foreach (var companion in Companions) companion?.EnsureCollections();
            if (Records == null)
            {
                Records = new List<DailyCompanionRecord>();
            }
        }
    }
}
