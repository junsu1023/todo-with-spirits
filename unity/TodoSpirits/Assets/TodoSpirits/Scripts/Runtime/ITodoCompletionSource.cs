using System.Collections.Generic;
using TodoSpirits.Core;

namespace TodoSpirits.Runtime
{
    /// <summary>
    /// Boundary for completed TODO input. The prototype uses a local mock implementation.
    /// </summary>
    public interface ITodoCompletionSource
    {
        IReadOnlyList<CompletedTask> GetCompletedTasks(string date);
    }
}
