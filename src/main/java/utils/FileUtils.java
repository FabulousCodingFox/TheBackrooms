package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {
    public static String readResourceFile(String path) throws IOException {
        InputStream inputStream = FileUtils.class.getClassLoader().getResourceAsStream(path);
        assert inputStream != null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return readFromBufferedReader(reader);
    }

    private static String readFromBufferedReader(BufferedReader br) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {resultStringBuilder.append(line).append("\n");}
        return resultStringBuilder.toString();
    }
}
