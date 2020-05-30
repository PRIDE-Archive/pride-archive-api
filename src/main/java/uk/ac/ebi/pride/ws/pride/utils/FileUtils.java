package uk.ac.ebi.pride.ws.pride.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class FileUtils {

    public static String readFileURL(String urlString) throws IOException {

        URL url = new URL(urlString);
        BufferedReader read = new BufferedReader(
                new InputStreamReader(url.openStream()));
        StringBuffer fileString = new StringBuffer();
        String line;
        while ((line = read.readLine()) != null)
            fileString.append(line);
        read.close();
        return fileString.toString();

    }

}