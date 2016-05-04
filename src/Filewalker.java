import java.io.File;

public class Filewalker {

    public static String[] walk(String path) {
        File root = new File(path);
        if (!root.isDirectory())
            return new String[]{"Directory not found"};
        File[] list = root.listFiles();
        String[] listNames = new String[list.length];
        for (int i = 0; i < list.length; i++) {
            listNames[i] = list[i].getName();
        }
        return listNames;
    }
}