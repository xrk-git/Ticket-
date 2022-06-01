/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DefaultLocale;

public class XmlStreamReaderTest {
    private static final String XML5 = "xml-prolog-encoding-spaced-single-quotes";
    private static final String XML4 = "xml-prolog-encoding-single-quotes";
    private static final String XML3 = "xml-prolog-encoding-double-quotes";
    private static final String XML2 = "xml-prolog";
    private static final String XML1 = "xml";

    private static final String ENCODING_ATTRIBUTE_XML = "<?xml version=\"1.0\" ?> \n"
            + "<atom:feed xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "\n"
            + "  <atom:entry>\n"
            + "    <atom:title encoding='base64'><![CDATA\n"
            + "aW5nTGluZSIgLz4";

    private static final int[] NO_BOM_BYTES = {};

    private static final int[] UTF_16BE_BOM_BYTES = {0xFE, 0xFF};

    private static final int[] UTF_16LE_BOM_BYTES = {0xFF, 0XFE};

    private static final int[] UTF_32BE_BOM_BYTES = {0x00, 0x00, 0xFE, 0xFF};

    private static final int[] UTF_32LE_BOM_BYTES = {0xFF, 0XFE, 0x00, 0x00};

    private static final int[] UTF_8_BOM_BYTES = {0xEF, 0xBB, 0xBF};

    private static final Map<String, int[]> BOMs = new HashMap<>();

    static {
        BOMs.put("no-bom", NO_BOM_BYTES);
        BOMs.put("UTF-16BE-bom", UTF_16BE_BOM_BYTES);
        BOMs.put("UTF-16LE-bom", UTF_16LE_BOM_BYTES);
        BOMs.put("UTF-32BE-bom", UTF_32BE_BOM_BYTES);
        BOMs.put("UTF-32LE-bom", UTF_32LE_BOM_BYTES);
        BOMs.put("UTF-16-bom", NO_BOM_BYTES); // it's added by the writer
        BOMs.put("UTF-8-bom", UTF_8_BOM_BYTES);
    }

    private static final MessageFormat XML = new MessageFormat(
            "<root>{2}</root>");

    private static final MessageFormat XML_WITH_PROLOG = new MessageFormat(
            "<?xml version=\"1.0\"?>\n<root>{2}</root>");

    private static final MessageFormat XML_WITH_PROLOG_AND_ENCODING_DOUBLE_QUOTES = new MessageFormat(
            "<?xml version=\"1.0\" encoding=\"{1}\"?>\n<root>{2}</root>");

    private static final MessageFormat XML_WITH_PROLOG_AND_ENCODING_SINGLE_QUOTES = new MessageFormat(
            "<?xml version=\"1.0\" encoding=''{1}''?>\n<root>{2}</root>");

    private static final MessageFormat XML_WITH_PROLOG_AND_ENCODING_SPACED_SINGLE_QUOTES = new MessageFormat(
            "<?xml version=\"1.0\" encoding =  \t \n \r''{1}''?>\n<root>{2}</root>");

    private static final MessageFormat INFO = new MessageFormat(
            "\nBOM : {0}\nDoc : {1}\nStream Enc : {2}\nProlog Enc : {3}\n");

    private static final Map<String, MessageFormat> XMLs = new HashMap<>();

    static {
        XMLs.put(XML1, XML);
        XMLs.put(XML2, XML_WITH_PROLOG);
        XMLs.put(XML3, XML_WITH_PROLOG_AND_ENCODING_DOUBLE_QUOTES);
        XMLs.put(XML4, XML_WITH_PROLOG_AND_ENCODING_SINGLE_QUOTES);
        XMLs.put(XML5, XML_WITH_PROLOG_AND_ENCODING_SPACED_SINGLE_QUOTES);
    }

    /**
     * Create the XML.
     */
    private String getXML(final String bomType, final String xmlType,
                          final String streamEnc, final String prologEnc) {
        final MessageFormat xml = XMLs.get(xmlType);
        final String info = INFO.format(new Object[]{bomType, xmlType, prologEnc});
        return xml.format(new Object[]{streamEnc, prologEnc, info});
    }

    /**
     * @param bomType   no-bom, UTF-16BE-bom, UTF-16LE-bom, UTF-8-bom
     * @param xmlType   xml, xml-prolog, xml-prolog-charset
     * @param streamEnc encoding of the stream
     * @param prologEnc encoding of the prolog
     * @return XML stream
     * @throws IOException If an I/O error occurs
     */
    protected InputStream getXmlInputStream(final String bomType, final String xmlType,
        final String streamEnc, final String prologEnc) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        int[] bom = BOMs.get(bomType);
        if (bom == null) {
            bom = new int[0];
        }
        for (final int element : bom) {
            baos.write(element);
        }
        try (final Writer writer = new OutputStreamWriter(baos, streamEnc)) {
            final String xmlDoc = getXML(bomType, xmlType, streamEnc, prologEnc);
            writer.write(xmlDoc);

            // PADDING TO TEST THINGS WORK BEYOND PUSHBACK_SIZE
            writer.write("<da>\n");
            for (int i = 0; i < 10000; i++) {
                writer.write("<do/>\n");
            }
            writer.write("</da>\n");

        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public void testAlternateDefaultEncoding(final String cT, final String bomEnc,
                                              final String streamEnc, final String prologEnc, final String alternateEnc)
            throws Exception {
        final InputStream is = getXmlInputStream(bomEnc, prologEnc == null ? XML1
                : XML3, streamEnc, prologEnc);
        final XmlStreamReader xmlReader = new XmlStreamReader(is, cT, false, alternateEnc);
        if (!streamEnc.equals("UTF-16")) {
            // we can not assert things here because UTF-8, US-ASCII and
            // ISO-8859-1 look alike for the chars used for detection
            // (niallp 2010-10-06 - I re-instated the check below - the tests(6) passed)
            final String enc = alternateEnc != null ? alternateEnc : streamEnc;
            assertEquals(xmlReader.getEncoding(), enc);
        } else {
            //String enc = (alternateEnc != null) ? alternateEnc : streamEnc;
            assertEquals(xmlReader.getEncoding().substring(0,
                    streamEnc.length()), streamEnc);
        }
        xmlReader.close();
    }


    @Test
    public void testEncodingAttributeXML() throws Exception {
        final InputStream is = new ByteArrayInputStream(ENCODING_ATTRIBUTE_XML
                .getBytes(StandardCharsets.UTF_8));
        final XmlStreamReader xmlReader = new XmlStreamReader(is, "", true);
        assertEquals(xmlReader.getEncoding(), "UTF-8");
        xmlReader.close();
    }

    @Test
    public void testHttp() throws Exception {
        // niallp 2010-10-06 - remove following 2 tests - I reinstated
        // checks for non-UTF-16 encodings (18 tests) and these failed
        // _testHttpValid("application/xml", "no-bom", "US-ASCII", null);
        // _testHttpValid("application/xml", "UTF-8-bom", "US-ASCII", null);
        testHttpValid("application/xml", "UTF-8-bom", "UTF-8", null);
        testHttpValid("application/xml", "UTF-8-bom", "UTF-8", "UTF-8");
        testHttpValid("application/xml;charset=UTF-8", "UTF-8-bom", "UTF-8", null);
        testHttpValid("application/xml;charset=\"UTF-8\"", "UTF-8-bom", "UTF-8", null);
        testHttpValid("application/xml;charset='UTF-8'", "UTF-8-bom", "UTF-8", null);
        testHttpValid("application/xml;charset=UTF-8", "UTF-8-bom", "UTF-8", "UTF-8");
        testHttpValid("application/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", null);
        testHttpValid("application/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", "UTF-16");
        testHttpValid("application/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", "UTF-16BE");

        testHttpInvalid("application/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", null);
        testHttpInvalid("application/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", "UTF-16");
        testHttpInvalid("application/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", "UTF-16BE");

        testHttpInvalid("application/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", null);
        testHttpInvalid("application/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", "UTF-32");
        testHttpInvalid("application/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", "UTF-32BE");

        testHttpInvalid("application/xml", "UTF-8-bom", "US-ASCII", "US-ASCII");
        testHttpInvalid("application/xml;charset=UTF-16", "UTF-16LE", "UTF-8", "UTF-8");
        testHttpInvalid("application/xml;charset=UTF-16", "no-bom", "UTF-16BE", "UTF-16BE");
        testHttpInvalid("application/xml;charset=UTF-32", "UTF-32LE", "UTF-8", "UTF-8");
        testHttpInvalid("application/xml;charset=UTF-32", "no-bom", "UTF-32BE", "UTF-32BE");

        testHttpValid("text/xml", "no-bom", "US-ASCII", null);
        testHttpValid("text/xml;charset=UTF-8", "UTF-8-bom", "UTF-8", "UTF-8");
        testHttpValid("text/xml;charset=UTF-8", "UTF-8-bom", "UTF-8", null);
        testHttpValid("text/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", null);
        testHttpValid("text/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", "UTF-16");
        testHttpValid("text/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", "UTF-16BE");
        testHttpValid("text/xml;charset=UTF-32", "UTF-32BE-bom", "UTF-32BE", null);
        testHttpValid("text/xml;charset=UTF-32", "UTF-32BE-bom", "UTF-32BE", "UTF-32");
        testHttpValid("text/xml;charset=UTF-32", "UTF-32BE-bom", "UTF-32BE", "UTF-32BE");
        testHttpValid("text/xml", "UTF-8-bom", "US-ASCII", null);

        testAlternateDefaultEncoding("application/xml", "UTF-8-bom", "UTF-8", null, null);
        testAlternateDefaultEncoding("application/xml", "no-bom", "US-ASCII", null, "US-ASCII");
        testAlternateDefaultEncoding("application/xml", "UTF-8-bom", "UTF-8", null, "UTF-8");
        testAlternateDefaultEncoding("text/xml", "no-bom", "US-ASCII", null, null);
        testAlternateDefaultEncoding("text/xml", "no-bom", "US-ASCII", null, "US-ASCII");
        testAlternateDefaultEncoding("text/xml", "no-bom", "US-ASCII", null, "UTF-8");

        testHttpInvalid("text/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", null);
        testHttpInvalid("text/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", "UTF-16");
        testHttpInvalid("text/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", "UTF-16BE");
        testHttpInvalid("text/xml;charset=UTF-16", "no-bom", "UTF-16BE", "UTF-16BE");
        testHttpInvalid("text/xml;charset=UTF-16", "no-bom", "UTF-16BE", null);

        testHttpInvalid("text/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", null);
        testHttpInvalid("text/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", "UTF-32");
        testHttpInvalid("text/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", "UTF-32BE");
        testHttpInvalid("text/xml;charset=UTF-32", "no-bom", "UTF-32BE", "UTF-32BE");
        testHttpInvalid("text/xml;charset=UTF-32", "no-bom", "UTF-32BE", null);

        testHttpLenient("text/xml", "no-bom", "US-ASCII", null, "US-ASCII");
        testHttpLenient("text/xml;charset=UTF-8", "UTF-8-bom", "UTF-8", "UTF-8", "UTF-8");
        testHttpLenient("text/xml;charset=UTF-8", "UTF-8-bom", "UTF-8", null, "UTF-8");
        testHttpLenient("text/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", null, "UTF-16BE");
        testHttpLenient("text/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", "UTF-16", "UTF-16");
        testHttpLenient("text/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", "UTF-16BE", "UTF-16BE");
        testHttpLenient("text/xml;charset=UTF-32", "UTF-32BE-bom", "UTF-32BE", null, "UTF-32BE");
        testHttpLenient("text/xml;charset=UTF-32", "UTF-32BE-bom", "UTF-32BE", "UTF-32", "UTF-32");
        testHttpLenient("text/xml;charset=UTF-32", "UTF-32BE-bom", "UTF-32BE", "UTF-32BE", "UTF-32BE");
        testHttpLenient("text/xml", "UTF-8-bom", "US-ASCII", null, "US-ASCII");

        testHttpLenient("text/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", null, "UTF-16BE");
        testHttpLenient("text/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", "UTF-16", "UTF-16");
        testHttpLenient("text/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", "UTF-16BE", "UTF-16BE");
        testHttpLenient("text/xml;charset=UTF-16", "no-bom", "UTF-16BE", "UTF-16BE", "UTF-16BE");
        testHttpLenient("text/xml;charset=UTF-16", "no-bom", "UTF-16BE", null, "UTF-16");

        testHttpLenient("text/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", null, "UTF-32BE");
        testHttpLenient("text/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", "UTF-32", "UTF-32");
        testHttpLenient("text/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", "UTF-32BE", "UTF-32BE");
        testHttpLenient("text/xml;charset=UTF-32", "no-bom", "UTF-32BE", "UTF-32BE", "UTF-32BE");
        testHttpLenient("text/xml;charset=UTF-32", "no-bom", "UTF-32BE", null, "UTF-32");

        testHttpLenient("text/html", "no-bom", "US-ASCII", "US-ASCII", "US-ASCII");
        testHttpLenient("text/html", "no-bom", "US-ASCII", null, "US-ASCII");
        testHttpLenient("text/html;charset=UTF-8", "no-bom", "US-ASCII", "UTF-8", "UTF-8");
        testHttpLenient("text/html;charset=UTF-16BE", "no-bom", "US-ASCII", "UTF-8", "UTF-8");
        testHttpLenient("text/html;charset=UTF-32BE", "no-bom", "US-ASCII", "UTF-8", "UTF-8");
    }

    @Test
    public void testHttpContent() throws Exception {
        final String encoding = "UTF-8";
        final String xml = getXML("no-bom", XML3, encoding, encoding);
        try (final XmlStreamReader xmlReader = new XmlStreamReader(new StringInputStream(xml, encoding))) {
            assertEquals(xmlReader.getEncoding(), encoding, "Check encoding");
            assertEquals(xml, IOUtils.toString(xmlReader), "Check content");
        }
    }

    protected void testHttpInvalid(final String cT, final String bomEnc, final String streamEnc,
                                    final String prologEnc) throws Exception {
        final InputStream is = getXmlInputStream(bomEnc,
                prologEnc == null ? XML2 : XML3, streamEnc, prologEnc);
        try {
            (new XmlStreamReader(is, cT, false)).close();
            fail("It should have failed for HTTP Content-type " + cT + ", BOM "
                    + bomEnc + ", streamEnc " + streamEnc + " and prologEnc "
                    + prologEnc);
        } catch (final IOException ex) {
            assertTrue(ex.getMessage().contains("Invalid encoding,"));
        }
    }

    protected void testHttpLenient(final String cT, final String bomEnc, final String streamEnc,
                                    final String prologEnc, final String shouldbe) throws Exception {
        final InputStream is = getXmlInputStream(bomEnc,
                prologEnc == null ? XML2 : XML3, streamEnc, prologEnc);
        final XmlStreamReader xmlReader = new XmlStreamReader(is, cT, true);
        assertEquals(xmlReader.getEncoding(), shouldbe);
        xmlReader.close();
    }

    public void testHttpValid(final String cT, final String bomEnc, final String streamEnc,
                               final String prologEnc) throws Exception {
        final InputStream is = getXmlInputStream(bomEnc,
                prologEnc == null ? XML1 : XML3, streamEnc, prologEnc);
        final XmlStreamReader xmlReader = new XmlStreamReader(is, cT, false);
        if (!streamEnc.equals("UTF-16")) {
            // we can not assert things here because UTF-8, US-ASCII and
            // ISO-8859-1 look alike for the chars used for detection
            // (niallp 2010-10-06 - I re-instated the check below and removed the 2 tests that failed)
            assertEquals(xmlReader.getEncoding(), streamEnc);
        } else {
            assertEquals(xmlReader.getEncoding().substring(0,
                    streamEnc.length()), streamEnc);
        }
        xmlReader.close();
    }

    // Turkish language has specific rules to convert dotted and dottless i character.
    @Test
    @DefaultLocale(language = "tr")
    public void testLowerCaseEncodingWithTurkishLocale_IO_557() throws Exception {
        final String[] encodings = { "iso8859-1", "us-ascii", "utf-8" };
        for (final String encoding : encodings) {
            final String xml = getXML("no-bom", XML3, encoding, encoding);
            try (final ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes(encoding));
                    final XmlStreamReader xmlReader = new XmlStreamReader(is)) {
                assertTrue(encoding.equalsIgnoreCase(xmlReader.getEncoding()), "Check encoding : " + encoding);
                assertEquals(xml, IOUtils.toString(xmlReader), "Check content");
            }
        }
    }

    @Test
    protected void testNullFileInput() {
        assertThrows(NullPointerException.class, () -> new XmlStreamReader((File) null));
    }

    @Test
    protected void testNullInputStreamInput() {
        assertThrows(NullPointerException.class, () -> new XmlStreamReader((InputStream) null));
    }

    @Test
    protected void testNullPathInput() {
        assertThrows(NullPointerException.class, () -> new XmlStreamReader((Path) null));
    }

    // XML Stream generator

    @Test
    protected void testNullURLConnectionInput() {
        assertThrows(NullPointerException.class, () -> new XmlStreamReader((URLConnection) null, "US-ASCII"));
    }
    @Test
    protected void testNullURLInput() {
        assertThrows(NullPointerException.class, () -> new XmlStreamReader((URL) null));
    }
    protected void testRawBomInvalid(final String bomEnc, final String streamEnc,
        final String prologEnc) throws Exception {
        final InputStream is = getXmlInputStream(bomEnc, XML3, streamEnc, prologEnc);
        XmlStreamReader xmlReader = null;
        try {
            xmlReader = new XmlStreamReader(is, false);
            final String foundEnc = xmlReader.getEncoding();
            fail("Expected IOException for BOM " + bomEnc + ", streamEnc " + streamEnc + " and prologEnc " + prologEnc
                + ": found " + foundEnc);
        } catch (final IOException ex) {
            assertTrue(ex.getMessage().contains("Invalid encoding,"));
        }
        if (xmlReader != null) {
            xmlReader.close();
        }
    }
    @Test
    public void testRawBomUtf16() throws Exception {
        testRawBomValid("UTF-16BE");
        testRawBomValid("UTF-16LE");
        testRawBomValid("UTF-16");

        testRawBomInvalid("UTF-16BE-bom", "UTF-16BE", "UTF-16LE");
        testRawBomInvalid("UTF-16LE-bom", "UTF-16LE", "UTF-16BE");
        testRawBomInvalid("UTF-16LE-bom", "UTF-16LE", "UTF-8");
    }
    @Test
    public void testRawBomUtf32() throws Exception {
        testRawBomValid("UTF-32BE");
        testRawBomValid("UTF-32LE");
        testRawBomValid("UTF-32");

        testRawBomInvalid("UTF-32BE-bom", "UTF-32BE", "UTF-32LE");
        testRawBomInvalid("UTF-32LE-bom", "UTF-32LE", "UTF-32BE");
        testRawBomInvalid("UTF-32LE-bom", "UTF-32LE", "UTF-8");
    }
    @Test
    public void testRawBomUtf8() throws Exception {
        testRawBomValid("UTF-8");
        testRawBomInvalid("UTF-8-bom", "US-ASCII", "US-ASCII");
        testRawBomInvalid("UTF-8-bom", "ISO-8859-1", "ISO-8859-1");
        testRawBomInvalid("UTF-8-bom", "UTF-8", "UTF-16");
        testRawBomInvalid("UTF-8-bom", "UTF-8", "UTF-16BE");
        testRawBomInvalid("UTF-8-bom", "UTF-8", "UTF-16LE");
        testRawBomInvalid("UTF-16BE-bom", "UTF-16BE", "UTF-16LE");
        testRawBomInvalid("UTF-16LE-bom", "UTF-16LE", "UTF-16BE");
        testRawBomInvalid("UTF-16LE-bom", "UTF-16LE", "UTF-8");
        testRawBomInvalid("UTF-32BE-bom", "UTF-32BE", "UTF-32LE");
        testRawBomInvalid("UTF-32LE-bom", "UTF-32LE", "UTF-32BE");
        testRawBomInvalid("UTF-32LE-bom", "UTF-32LE", "UTF-8");
    }

    protected void testRawBomValid(final String encoding) throws Exception {
        final InputStream is = getXmlInputStream(encoding + "-bom", XML3, encoding, encoding);
        final XmlStreamReader xmlReader = new XmlStreamReader(is, false);
        if (!encoding.equals("UTF-16") && !encoding.equals("UTF-32")) {
            assertEquals(xmlReader.getEncoding(), encoding);
        } else {
            assertEquals(xmlReader.getEncoding().substring(0, encoding.length()), encoding);
        }
        xmlReader.close();
    }

    @Test
    public void testRawContent() throws Exception {
        final String encoding = "UTF-8";
        final String xml = getXML("no-bom", XML3, encoding, encoding);
        try (final XmlStreamReader xmlReader = new XmlStreamReader(new StringInputStream(xml, encoding))) {
            assertEquals(xmlReader.getEncoding(), encoding, "Check encoding");
            assertEquals(xml, IOUtils.toString(xmlReader), "Check content");
        }
    }

    @Test
    public void testRawNoBomCp1047() throws Exception {
        testRawNoBomValid("CP1047");
    }
    protected void testRawNoBomInvalid(final String encoding) throws Exception {
        final InputStream is = getXmlInputStream("no-bom", XML3, encoding, encoding);
        try {
            new XmlStreamReader(is, false).close();
            fail("It should have failed");
        } catch (final IOException ex) {
            assertTrue(ex.getMessage().contains("Invalid encoding,"));
        }
    }
    @Test
    public void testRawNoBomIso8859_1() throws Exception {
        testRawNoBomValid("ISO-8859-1");
    }
    @Test
    public void testRawNoBomUsAscii() throws Exception {
        testRawNoBomValid("US-ASCII");
    }
    @Test
    public void testRawNoBomUtf16BE() throws Exception {
        testRawNoBomValid("UTF-16BE");
    }

    @Test
    public void testRawNoBomUtf16LE() throws Exception {
        testRawNoBomValid("UTF-16LE");
    }

    @Test
    public void testRawNoBomUtf32BE() throws Exception {
        testRawNoBomValid("UTF-32BE");
    }

    @Test
    public void testRawNoBomUtf32LE() throws Exception {
        testRawNoBomValid("UTF-32LE");
    }

    @Test
    public void testRawNoBomUtf8() throws Exception {
        testRawNoBomValid("UTF-8");
    }

    protected void testRawNoBomValid(final String encoding) throws Exception {
        InputStream is = getXmlInputStream("no-bom", XML1, encoding, encoding);
        XmlStreamReader xmlReader = new XmlStreamReader(is, false);
        assertEquals(xmlReader.getEncoding(), "UTF-8");
        xmlReader.close();

        is = getXmlInputStream("no-bom", XML2, encoding, encoding);
        xmlReader = new XmlStreamReader(is);
        assertEquals(xmlReader.getEncoding(), "UTF-8");
        xmlReader.close();

        is = getXmlInputStream("no-bom", XML3, encoding, encoding);
        xmlReader = new XmlStreamReader(is);
        assertEquals(xmlReader.getEncoding(), encoding);
        xmlReader.close();

        is = getXmlInputStream("no-bom", XML4, encoding, encoding);
        xmlReader = new XmlStreamReader(is);
        assertEquals(xmlReader.getEncoding(), encoding);
        xmlReader.close();

        is = getXmlInputStream("no-bom", XML5, encoding, encoding);
        xmlReader = new XmlStreamReader(is);
        assertEquals(xmlReader.getEncoding(), encoding);
        xmlReader.close();
    }
}