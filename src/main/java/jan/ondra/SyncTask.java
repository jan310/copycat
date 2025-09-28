package jan.ondra;

import java.nio.file.Path;

enum ActionType {
    MK_DIR,
    COPY,
    UPDATE,
    DEL_FILE,
    DEL_DIR,
}

public record SyncTask(ActionType type, Path srcPath, Path targetPath) {
    @Override
    public String toString() {
        return switch (type) {
            case MK_DIR -> "CREATE DIRECTORY:\t" + targetPath;
            case COPY -> "COPY:\t" + srcPath + " -> " + targetPath;
            case UPDATE -> "UPDATE:\t" + srcPath + " -> " + targetPath;
            case DEL_FILE -> "DELETE FILE:\t" + targetPath;
            case DEL_DIR -> "DELETE DIRECTORY:\t" + targetPath;
        };
    }
}
