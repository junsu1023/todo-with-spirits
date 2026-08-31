namespace TodoSpirits.Runtime
{
    /// <summary>
    /// Replaceable persistence boundary for the prototype spirit state and daily records.
    /// </summary>
    public interface ISpiritSaveRepository
    {
        string SavePath { get; }

        PrototypeSaveData Load();

        void Save(PrototypeSaveData saveData);
    }
}
