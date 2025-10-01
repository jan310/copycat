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

        System.out.println("The following actions are planned:");

        syncPlan.directoriesToCreate().forEach(path -> System.out.println("CREATE DIRECTORY: " + path));
        syncPlan.fileSyncTasks().forEach(task -> {
            switch (task.type()) {
                case COPY -> System.out.println("COPY FILE: " + task.sourcePath() + " -> " + task.targetPath());
                case UPDATE -> System.out.println("UPDATE FILE: " + task.sourcePath() + " -> " + task.targetPath());
                case DELETE -> System.out.println("DELETE FILE: " + task.targetPath());
            }
        });
        syncPlan.directoriesToDelete().forEach(path -> System.out.println("DELETE DIRECTORY: " + path));

        System.out.print("Continue? (y/n): ");
        String input = new Scanner(System.in).nextLine().trim().toLowerCase();

        if (input.equals("y")) {
            syncEngine.executeSyncPlan(syncPlan);
            System.out.println("Synchronization complete");
        } else {
            System.out.println("Synchronization aborted");
        }
    }

}
