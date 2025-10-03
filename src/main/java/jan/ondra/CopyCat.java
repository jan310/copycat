package jan.ondra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class CopyCat {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            return;
        }

        Path srcDir = Paths.get(args[0]);
        Path targetDir = Paths.get(args[1]);

        if (
            !Files.exists(srcDir) ||
            !Files.isDirectory(srcDir) ||
            !Files.exists(targetDir) ||
            !Files.isDirectory(targetDir)
        ) {
            System.err.println("Invalid directory: " + srcDir);
            return;
        }

        SyncEngine syncEngine = new SyncEngine();

        SyncPlan syncPlan = syncEngine.createSyncPlan(srcDir, targetDir);

        if (
            syncPlan.directoriesToCreate().isEmpty() &&
            syncPlan.directoriesToDelete().isEmpty() &&
            syncPlan.filesToCopyUpdateDelete().isEmpty()
        ) {
            System.out.println("No differences detected.");
            return;
        }

        System.out.println("The following actions are planned:");

        syncPlan.directoriesToCreate().forEach(path -> System.out.println("MKDIR:\t/" + targetDir.relativize(path)));
        syncPlan.filesToCopyUpdateDelete().forEach(task -> {
            switch (task.type()) {
                case COPY -> System.out.println("COPY:\t/" + targetDir.relativize(task.targetPath()));
                case UPDATE -> System.out.println("UPDATE:\t/" + targetDir.relativize(task.targetPath()));
                case DELETE -> System.out.println("DELETE:\t/" + targetDir.relativize(task.targetPath()));
            }
        });
        syncPlan.directoriesToDelete().forEach(path -> System.out.println("RMDIR:\t/" + targetDir.relativize(path)));

        System.out.print("Continue? (y/n): ");
        String input = new Scanner(System.in).nextLine().trim().toLowerCase();

        if (input.equals("y")) {
            long startTime = System.currentTimeMillis();
            syncEngine.executeSyncPlan(syncPlan);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Synchronization complete (duration: " + duration  + "ms)");
        } else {
            System.out.println("Synchronization aborted");
        }
    }

}
