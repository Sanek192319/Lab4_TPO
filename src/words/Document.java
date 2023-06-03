package words;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Document {
    private final String path;
    private final List<String> lines;

    Document(String path, List<String> lines) {
        this.path = path;
        this.lines = lines;
    }

    public String getPath() {
        return path;
    }

    public List<String> getLines() {
        return lines;
    }

    public static Document fromFile(File file) throws IOException {
        List<String> lines = new LinkedList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        }
        return new Document(file.getPath(), lines);
    }
}
