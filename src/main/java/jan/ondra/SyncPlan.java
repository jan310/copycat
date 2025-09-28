package jan.ondra;

import java.util.List;

public record SyncPlan(
    List<SyncTask> mkDirTasks,
    List<SyncTask> fileTasks,
    List<SyncTask> delDirTasks
) {}
