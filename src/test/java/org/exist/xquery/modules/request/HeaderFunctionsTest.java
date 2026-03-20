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
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for request:header(), request:header-names(), and request:header-map().
 */
public class HeaderFunctionsTest {

    @ClassRule
    public static final ExistWebServer existWebServer =
            new ExistWebServer(true, false, true, true);

    private static final String IMPORT =
            "import module namespace request = 'http://exquery.org/ns/request';\n";

    // ===== request:header-names() =====

    @Test
    public void headerNamesReturnsNames() throws IOException {
        final String result = RequestTestUtil.restQuery(existWebServer,
                IMPORT + "string-join(request:header-names(), ',')");
        // At minimum, Host header should be present
        assertTrue("header-names() should contain Host (case-insensitive), got: " + result.trim(),
                result.trim().toLowerCase().contains("host"));
    }

    // ===== request:header($name) =====

    @Test
    public void headerReturnsHostValue() throws IOException {
        final String result = RequestTestUtil.restQuery(existWebServer,
                IMPORT + "request:header('Host')");
        assertTrue("header('Host') should contain localhost, got: " + result.trim(),
                result.trim().contains("localhost"));
    }

    @Test
    public void headerReturnsCustomHeader() throws IOException {
        final String result = RequestTestUtil.restQueryWithHeaders(existWebServer,
                IMPORT + "request:header('X-Custom-Test')",
                Map.of("X-Custom-Test", "hello-world"));
        assertEquals("hello-world", result.trim());
    }

    @Test
    public void headerReturnsMissingAsEmpty() throws IOException {
        final String result = RequestTestUtil.restQuery(existWebServer,
                IMPORT +
                "let $h := request:header('X-Nonexistent-Header')\n" +
                "return if (empty($h)) then 'empty' else 'found'");
        assertEquals("empty", result.trim());
    }

    // ===== request:header($name, $default) =====

    @Test
    public void headerWithDefaultReturnsValue() throws IOException {
        final String result = RequestTestUtil.restQueryWithHeaders(existWebServer,
                IMPORT + "request:header('X-Custom-Test', 'fallback')",
                Map.of("X-Custom-Test", "actual-value"));
        assertEquals("actual-value", result.trim());
    }

    @Test
    public void headerWithDefaultReturnsFallback() throws IOException {
        final String result = RequestTestUtil.restQuery(existWebServer,
                IMPORT + "request:header('X-Missing', 'default-val')");
        assertEquals("default-val", result.trim());
    }

    // ===== request:header-map() =====

    @Test
    public void headerMapReturnsMap() throws IOException {
        final String result = RequestTestUtil.restQueryWithHeaders(existWebServer,
                IMPORT +
                "let $m := request:header-map()\n" +
                "return $m?('X-Map-Test')",
                Map.of("X-Map-Test", "map-value"));
        assertEquals("map-value", result.trim());
    }

    @Test
    public void headerMapContainsHostKey() throws IOException {
        final String result = RequestTestUtil.restQuery(existWebServer,
                IMPORT +
                "let $m := request:header-map()\n" +
                "return exists($m?Host)");
        assertEquals("true", result.trim());
    }
}
