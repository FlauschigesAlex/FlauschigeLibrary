package at.flauschigesalex.defaultLibrary;

import at.flauschigesalex.defaultLibrary.minecraft.api.MojangAPI;
import at.flauschigesalex.defaultLibrary.utils.reflections.Reflector;
import lombok.Getter;
import java.util.ArrayList;

@SuppressWarnings({"unused"})
@Getter
public class FlauschigeLibrary {
    public static void main(String[] args) {
        getAPI();
    }

    private static FlauschigeLibrary flauschigeLibrary;

    /**
     * Make sure to run this method in your main class!
     * This is extremely important for reflections!
     *
     * @return an instance of the API
     */
    public static FlauschigeLibrary getAPI() {
        if (flauschigeLibrary == null) flauschigeLibrary = new FlauschigeLibrary();
        return flauschigeLibrary;
    }

    private final String ownDirectoryPath;
    private final ArrayList<String> workingDirectoryPath = new ArrayList<>();
    public FlauschigeLibrary addWorkingDirectory(String path) {
        this.workingDirectoryPath.add(path);
        return this;
    }

    protected FlauschigeLibrary() {
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
