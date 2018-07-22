package knapp.util;

import knapp.history.ValueHistory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class CurrentDirectory {

    private final String baseDir;

    public CurrentDirectory(String baseDir) {
        this.baseDir = baseDir;
    }

    public String toText(String relativeFilePath) throws IOException {
        return FileUtils.readFileToString(toFile(relativeFilePath));
    }

    public File toFile(String relativeFilePath) {
        return new File(baseDir+"/"+relativeFilePath);
    }

    public ValueHistory readToValueHistory(String relativeFilePath) throws IOException {
        String text = toText(relativeFilePath);
        return ValueHistory.parseText(text);
    }
}
