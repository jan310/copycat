package jan.ondra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        SyncPlan syncPlan = SyncEngine.createSyncPlan(srcDir, targetDir);

        System.out.println("The following directories will be created:");
        syncPlan.mkDirTasks().forEach(t -> System.out.println(" - " + t));
        System.out.println();

        System.out.println("The following files will be copied/updated/deleted:");
        syncPlan.fileTasks().forEach(t -> System.out.println(" - " + t));
        System.out.println();

        System.out.println("The following directories will be deleted:");
        syncPlan.delDirTasks().forEach(t -> System.out.println(" - " + t));
        System.out.println();
    }

}
