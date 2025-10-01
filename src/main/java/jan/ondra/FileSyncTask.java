package jan.ondra;

import java.nio.file.Path;

enum ActionType {
    COPY,
    UPDATE,
    DELETE
}

public record FileSyncTask(
    ActionType type,
    Path sourcePath,
    Path targetPath
) {}
