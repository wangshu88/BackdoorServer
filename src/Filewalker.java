import java.io.File;

public class Filewalker {

    public static File[] walk(String path) {
        File root = new File(path);
        if (!root.isDirectory())
            return null;
        File[] list = root.listFiles();
        return list;
    }
}