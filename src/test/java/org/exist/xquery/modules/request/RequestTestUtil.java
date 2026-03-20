/*
 * eXist-db Open Source Native XML Database
 * Copyright (C) 2001 The eXist-db Authors
 *
 * info@exist-db.org
 * http://www.exist-db.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.exist.xquery.modules.request;

import org.exist.test.ExistWebServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Shared utilities for Request module integration tests.
 * Executes XQuery via the eXist-db REST API and returns the response body.
 */
public final class RequestTestUtil {

    private RequestTestUtil() {
        // utility class
    }

    /**
     * Execute an XQuery via the REST API using GET.
     */
    public static String restQuery(final ExistWebServer server, final String xquery)
            throws IOException {
        return restQuery(server, xquery, "GET");
    }

    /**
     * Execute an XQuery via the REST API with the specified HTTP method.
     */
    public static String restQuery(final ExistWebServer server, final String xquery,
                                    final String method) throws IOException {
        return restQueryWithHeaders(server, xquery, method, Map.of(), null);
    }

    /**
     * Execute an XQuery via GET with additional query parameters.
     */
    public static String restQueryWithParams(final ExistWebServer server, final String xquery,
                                              final String extraParams) throws IOException {
        final String encodedQuery = URLEncoder.encode(xquery, StandardCharsets.UTF_8);
        final String url = "http://localhost:" + server.getPort() +
                "/rest/db?_query=" + encodedQuery + "&" + extraParams;

        final HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", basicAuth("admin", ""));
        return readResponse(conn);
    }

    /**
     * Execute an XQuery via GET with custom HTTP headers.
     */
    public static String restQueryWithHeaders(final ExistWebServer server, final String xquery,
                                               final Map<String, String> headers) throws IOException {
        return restQueryWithHeaders(server, xquery, "GET", headers, null);
    }

    /**
     * Full-featured REST query executor.
     */
    public static String restQueryWithHeaders(final ExistWebServer server, final String xquery,
                                               final String method,
                                               final Map<String, String> headers,
                                               final String extraParams) throws IOException {
        if ("POST".equals(method)) {
            return restQueryPost(server, xquery, headers);
        }

        final String encodedQuery = URLEncoder.encode(xquery, StandardCharsets.UTF_8);
        String url = "http://localhost:" + server.getPort() + "/rest/db?_query=" + encodedQuery;
        if (extraParams != null && !extraParams.isEmpty()) {
            url += "&" + extraParams;
        }

        final HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", basicAuth("admin", ""));
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
        return readResponse(conn);
    }

    /**
     * Execute an XQuery via POST (the query is sent in the request body).
     */
    private static String restQueryPost(final ExistWebServer server, final String xquery,
                                         final Map<String, String> headers) throws IOException {
        final String url = "http://localhost:" + server.getPort() + "/rest/db";
        final HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", basicAuth("admin", ""));
        conn.setRequestProperty("Content-Type", "application/xml");
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
        conn.setDoOutput(true);

        // Wrap the XQuery in an exist:query XML envelope
        final String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<query xmlns=\"http://exist.sourceforge.net/NS/exist\">\n" +
                "  <text><![CDATA[" + xquery + "]]></text>\n" +
                "</query>";

        try (final OutputStream out = conn.getOutputStream()) {
            out.write(body.getBytes(StandardCharsets.UTF_8));
        }

        return readResponse(conn);
    }

    private static String readResponse(final HttpURLConnection conn) throws IOException {
        final int status = conn.getResponseCode();
        final InputStream is = (status >= 200 && status < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        if (is == null) {
            throw new IOException("No response body, HTTP status: " + status);
        }

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            final String body = sb.toString();

            if (status >= 400) {
                throw new IOException("HTTP " + status + ": " + body);
            }

            // The REST API wraps results in <exist:result> XML. Extract the text content.
            return extractResultText(body);
        }
    }

    /**
     * Extract the text content from the REST API response.
     * The REST API returns results wrapped in {@code <exist:result>} XML for non-XML results,
     * or as plain XML for XML results. For simple string/number results, we need to strip
     * the wrapper.
     */
    private static String extractResultText(final String body) {
        // If it looks like an exist:result wrapper, extract the text
        if (body.contains("<exist:result")) {
            // Try to extract text between result tags
            final int startTag = body.indexOf(">", body.indexOf("<exist:result"));
            final int endTag = body.indexOf("</exist:result>");
            if (startTag >= 0 && endTag >= 0) {
                return body.substring(startTag + 1, endTag).trim();
            }
        }

        // For _wrap=no or plain text results, strip XML declaration if present
        String result = body.trim();
        if (result.startsWith("<?xml")) {
            final int end = result.indexOf("?>");
            if (end >= 0) {
                result = result.substring(end + 2).trim();
            }
        }
        return result;
    }

    private static String basicAuth(final String user, final String password) {
        final String credentials = user + ":" + password;
        return "Basic " + java.util.Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));
    }
}
