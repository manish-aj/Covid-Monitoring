package com.manish.covidMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MultipartUploader {
    private final String boundaryString;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection urlConnect;
    private OutputStream outputStream;
    private PrintWriter printWriter;

    public MultipartUploader(String requestURL, String charset)
            throws IOException {

        boundaryString = "===" + System.currentTimeMillis() + "===";

        URL url = new URL(requestURL);
        urlConnect = (HttpURLConnection) url.openConnection();
        urlConnect.setUseCaches(false);
        urlConnect.setDoOutput(true); // indicates POST method
        urlConnect.setDoInput(true);
        urlConnect.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundaryString);
        outputStream = urlConnect.getOutputStream();
        printWriter = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                true);
    }

    public void addNewFile(String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName() + ".db";
        printWriter.append("--")
                .append(boundaryString)
                .append(LINE_FEED);
        printWriter.append("Content-Disposition: form-data; name=\"")
                .append(fieldName)
                .append("\"; filename=\"")
                .append(fileName)
                .append("\"")
                .append(LINE_FEED);
        printWriter.append("Content-Type: ")
                .append(URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED);
        printWriter.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        printWriter.append(LINE_FEED);
        printWriter.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        printWriter.append(LINE_FEED);
        printWriter.flush();
    }

    public List<String> completeUpload() throws IOException {
        List<String> response = new ArrayList<>();

        printWriter.append(LINE_FEED).flush();
        printWriter.append("--")
                .append(boundaryString)
                .append("--")
                .append(LINE_FEED);
        printWriter.close();

        int status = urlConnect.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlConnect.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();
            urlConnect.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }

        return response;
    }
}
