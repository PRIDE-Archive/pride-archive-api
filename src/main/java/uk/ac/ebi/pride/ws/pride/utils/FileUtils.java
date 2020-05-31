package uk.ac.ebi.pride.ws.pride.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class FileUtils {

    public static String readFileURL(String urlString) throws IOException {

        URL url = new URL(urlString);
        BufferedReader read = new BufferedReader(
                new InputStreamReader(url.openStream()));
        StringBuffer fileString = new StringBuffer();
        String line;
        while ((line = read.readLine()) != null)
            fileString.append(line).append("\n");
        read.close();
        return fileString.toString();

    }

}