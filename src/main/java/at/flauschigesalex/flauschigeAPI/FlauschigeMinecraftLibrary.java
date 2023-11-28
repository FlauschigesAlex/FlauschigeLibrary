package at.flauschigesalex.flauschigeAPI;

import at.flauschigesalex.flauschigeAPI.minecraft.api.MojangAPI;
import at.flauschigesalex.flauschigeAPI.utils.reflections.Reflector;
import lombok.Getter;
import java.util.ArrayList;

@SuppressWarnings({"unused"})
@Getter
public final class FlauschigeMinecraftLibrary {
    public static void main(String[] args) {
        getAPI();
    }

    private static FlauschigeMinecraftLibrary flauschigeMinecraftLibrary;

    /**
     * Make sure to run this method in your main class!
     * This is extremely important for reflections!
     *
     * @return an instance of the API
     */
    public static FlauschigeMinecraftLibrary getAPI() {
        if (flauschigeMinecraftLibrary == null) flauschigeMinecraftLibrary = new FlauschigeMinecraftLibrary();
        return flauschigeMinecraftLibrary;
    }

    private final String ownDirectoryPath;
    private final ArrayList<String> workingDirectoryPath = new ArrayList<>();
    public FlauschigeMinecraftLibrary addWorkingDirectory(String path) {
        this.workingDirectoryPath.add(path);
        return this;
    }

    private FlauschigeMinecraftLibrary() {
        this.ownDirectoryPath = getClass().getPackage().getName();
        for (Package definedPackage : getClass().getClassLoader().getDefinedPackages()) {
            if (definedPackage.getName().startsWith(getClass().getPackage().getName())) continue;
            boolean doContinue = false;
            for (String workingDirectory : this.workingDirectoryPath) {
                if (!workingDirectory.startsWith(definedPackage.getName())) continue;
                doContinue = true;
                break;
            }
            if (doContinue) continue;
            this.workingDirectoryPath.add(definedPackage.getName());
        }
    }

    public MojangAPI getMojangAPI() {
        return MojangAPI.mojangAPI();
    }
    public Reflector getReflector() {
        return Reflector.getReflector();
    }
}
