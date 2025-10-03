package jan.ondra;

import java.nio.file.Path;

public record SyncTask(
    ActionType type,
    Path sourcePath,
    Path targetPath
) {}
