package jan.ondra;

import java.nio.file.Path;
import java.util.List;

public record SyncPlan(
    List<Path> directoriesToCreate,
    List<SyncTask> filesToCopyUpdateDelete,
    List<Path> directoriesToDelete
) {}
