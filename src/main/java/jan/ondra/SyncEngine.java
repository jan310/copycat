package jan.ondra;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class SyncEngine {

    public static SyncPlan createSyncPlan(Path sourceDir, Path targetDir) throws IOException {
        List<SyncTask> mkDirTasks = new ArrayList<>();
        List<SyncTask> fileTasks = new ArrayList<>();
        List<SyncTask> delDirTasks = new ArrayList<>();

        Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path sourceSubDir, BasicFileAttributes attrs) {
                Path targetSubDir = targetDir.resolve(sourceDir.relativize(sourceSubDir));

                if (!Files.exists(targetSubDir)) {
                    mkDirTasks.add(new SyncTask(ActionType.MK_DIR, null, targetSubDir));
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes sourceFileAttrs) throws IOException {
                Path targetFile = targetDir.resolve(sourceDir.relativize(sourceFile));

                if (!Files.exists(targetFile)) {
                    fileTasks.add(new SyncTask(ActionType.COPY, sourceFile, targetFile));
                } else if (
                    sourceFileAttrs.size() != Files.size(targetFile) ||
                    sourceFileAttrs.lastModifiedTime().toMillis() > Files.getLastModifiedTime(targetFile).toMillis()
                ) {
                    fileTasks.add(new SyncTask(ActionType.UPDATE, sourceFile, targetFile));
                }

                return FileVisitResult.CONTINUE;
            }
        });

        Files.walkFileTree(targetDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path targetFile, BasicFileAttributes targetFileAttrs) {
                Path sourceFile = sourceDir.resolve(targetDir.relativize(targetFile));

                if (!Files.exists(sourceFile)) {
                    fileTasks.add(new SyncTask(ActionType.DEL_FILE, null, targetFile));
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path targetSubDir, IOException exc) {
                Path sourceSubDir = sourceDir.resolve(targetDir.relativize(targetSubDir));

                if (!Files.exists(sourceSubDir)) {
                    delDirTasks.add(new SyncTask(ActionType.DEL_DIR, null, targetSubDir));
                }

                return FileVisitResult.CONTINUE;
            }
        });

        return new SyncPlan(mkDirTasks, fileTasks, delDirTasks);
    }

}
