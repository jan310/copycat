package jan.ondra;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static jan.ondra.ActionType.COPY;
import static jan.ondra.ActionType.DELETE;
import static jan.ondra.ActionType.UPDATE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class SyncEngine {

    public SyncPlan createSyncPlan(Path sourceDir, Path targetDir) throws IOException {
        List<Path> directoriesToCreate = new ArrayList<>();
        List<SyncTask> filesToCopyUpdateDelete = new ArrayList<>();
        List<Path> directoriesToDelete = new ArrayList<>();

        Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path sourceSubDir, BasicFileAttributes attrs) {
                Path targetSubDir = targetDir.resolve(sourceDir.relativize(sourceSubDir));

                if (!Files.exists(targetSubDir)) {
                    directoriesToCreate.add(targetSubDir);
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes sourceFileAttrs) throws IOException {
                if (sourceFile.getFileName().toString().equals(".DS_Store")) {
                    return FileVisitResult.CONTINUE;
                }

                Path targetFile = targetDir.resolve(sourceDir.relativize(sourceFile));

                if (!Files.exists(targetFile)) {
                    filesToCopyUpdateDelete.add(new SyncTask(COPY, sourceFile, targetFile));
                } else if (
                    sourceFileAttrs.size() != Files.size(targetFile) ||
                    sourceFileAttrs.lastModifiedTime().toMillis() > Files.getLastModifiedTime(targetFile).toMillis()
                ) {
                    filesToCopyUpdateDelete.add(new SyncTask(UPDATE, sourceFile, targetFile));
                }

                return FileVisitResult.CONTINUE;
            }
        });

        Files.walkFileTree(targetDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path targetFile, BasicFileAttributes targetFileAttrs) {
                if (targetFile.getFileName().toString().equals(".DS_Store")) {
                    return FileVisitResult.CONTINUE;
                }

                Path sourceFile = sourceDir.resolve(targetDir.relativize(targetFile));

                if (!Files.exists(sourceFile)) {
                    filesToCopyUpdateDelete.add(new SyncTask(DELETE, null, targetFile));
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path targetSubDir, IOException exc) {
                Path sourceSubDir = sourceDir.resolve(targetDir.relativize(targetSubDir));

                if (!Files.exists(sourceSubDir)) {
                    directoriesToDelete.add(targetSubDir);
                }

                return FileVisitResult.CONTINUE;
            }
        });

        return new SyncPlan(directoriesToCreate, filesToCopyUpdateDelete, directoriesToDelete);
    }

    public void executeSyncPlan(SyncPlan syncPlan) throws IOException {
        for (Path path : syncPlan.directoriesToCreate()) {
            Files.createDirectory(path);
        }

        for (SyncTask task : syncPlan.filesToCopyUpdateDelete()) {
            switch (task.type()) {
                case COPY -> Files.copy(task.sourcePath(), task.targetPath());
                case UPDATE -> Files.copy(task.sourcePath(), task.targetPath(), REPLACE_EXISTING);
                case DELETE -> Files.delete(task.targetPath());
            }
        }

        for (Path path : syncPlan.directoriesToDelete()) {
            Files.delete(path);
        }
    }

}
