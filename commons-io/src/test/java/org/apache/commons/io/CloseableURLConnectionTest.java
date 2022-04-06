package org.apache.commons.io;

import org.apache.commons.io.file.AbstractTempDirTest;
import org.apache.commons.io.test.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;



/**
 * @author Huang Yuxin
 * @date 2022/2/16
 */
class CloseableURLConnectionTest extends AbstractTempDirTest {


    @Test
    void open() throws IOException {
        final File testFile = TestUtils.newFile(tempDirFile, "testFile.txt");
        final CloseableURLConnection uriConnection = CloseableURLConnection.open(testFile.toURI());
        assertNotNull(uriConnection);
        uriConnection.close();
    }

    @Test
    void testOpen() throws IOException {
        final File testFile = TestUtils.newFile(tempDirFile, "testFile.txt");
        final CloseableURLConnection urlConnection = CloseableURLConnection.open(testFile.toURI().toURL());
        assertNotNull(urlConnection);
        urlConnection.close();
    }


    @Test
    void addRequestProperty() throws IOException {
        final CloseableURLConnection connection = CloseableURLConnection.open(new URL("https://www.google.com/"));
        final String key = "phone";
        final String value = "123456";
        connection.addRequestProperty(key, value);
        final String res = connection.getRequestProperty("phone");
        assertEquals(res, value);
    }

    @Test
    void close() throws IOException {
        final File testFile = TestUtils.newFile(tempDirFile, "testFile.txt");
        final CloseableURLConnection connection = CloseableURLConnection.open(testFile.toURI().toURL());
        connection.close();
        assertThrows(FileNotFoundException.class, () -> connection.getContent());
    }

    @Test
    void testEquals() throws IOException {
        final File testFile = TestUtils.newFile(tempDirFile, "testFile.txt");
        final CloseableURLConnection connection1 = CloseableURLConnection.open(testFile.toURI().toURL());
        final CloseableURLConnection connection2 = CloseableURLConnection.open(testFile.toURI().toURL());
        assertFalse(connection1.equals(connection2));
    }


    @Test
    void getConnectTimeout() throws Exception {
        final CloseableURLConnection connection = CloseableURLConnection.open(new URL("https://www.google.com/"));
        connection.setConnectTimeout(5);
        assertEquals(5, connection.getConnectTimeout());
    }

    @Test
    void getContent() throws IOException {
        // write data to test file
        final File testFile = TestUtils.newFile(tempDirFile, "testFile.txt");
        final String content = "Hello World!";
        FileUtils.write(testFile, content, Charset.defaultCharset());
        // open as connection
        final CloseableURLConnection connection = CloseableURLConnection.open(testFile.toURI().toURL());
        // read connection content
        final InputStream is = (InputStream) connection.getContent();
        final BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String res = "";
        String currentLine = "";
        while ((currentLine = br.readLine()) != null) {
            res += currentLine;
        }
        // compare expect result and real result
        assertEquals(content, res);
        // delete test file and close connection
        FileUtils.delete(testFile);
        connection.close();
    }


    @Test
    void getContentLength() throws IOException {
        final File testFile = TestUtils.newFile(tempDirFile, "testFile.txt");
        final String content = "Hello World!";
        FileUtils.write(testFile, content, Charset.defaultCharset());
        final CloseableURLConnection connection = CloseableURLConnection.open(testFile.toURI().toURL());
        assertEquals(content.length(), connection.getContentLength());
    }

    @Test
    void getContentType() throws IOException {
        final File testFile = TestUtils.newFile(tempDirFile, "testFile.txt");
        final String content = "Hello World!";
        FileUtils.write(testFile, content, Charset.defaultCharset());
        final CloseableURLConnection connection = CloseableURLConnection.open(testFile.toURI().toURL());
        assertEquals("text/plain", connection.getContentType());
    }

}