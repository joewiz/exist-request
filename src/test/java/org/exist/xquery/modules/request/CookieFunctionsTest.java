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
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for request:cookie(), request:cookie-names(), and request:cookie-map().
 */
public class CookieFunctionsTest {

    @ClassRule
    public static final ExistWebServer existWebServer =
            new ExistWebServer(true, false, true, true);

    private static final String IMPORT =
            "import module namespace request = 'http://exquery.org/ns/request';\n";

    // ===== request:cookie($name) =====

    @Test
    public void cookieReturnsValue() throws IOException {
        final String result = RequestTestUtil.restQueryWithHeaders(existWebServer,
                IMPORT + "request:cookie('session')",
                Map.of("Cookie", "session=abc123"));
        assertEquals("abc123", result.trim());
    }

    @Test
    public void cookieReturnsMissingAsEmpty() throws IOException {
        final String result = RequestTestUtil.restQuery(existWebServer,
                IMPORT +
                "let $c := request:cookie('nonexistent')\n" +
                "return if (empty($c)) then 'empty' else 'found'");
        assertEquals("empty", result.trim());
    }

    // ===== request:cookie($name, $default) =====

    @Test
    public void cookieWithDefaultReturnsValue() throws IOException {
        final String result = RequestTestUtil.restQueryWithHeaders(existWebServer,
                IMPORT + "request:cookie('token', 'none')",
                Map.of("Cookie", "token=xyz789"));
        assertEquals("xyz789", result.trim());
    }

    @Test
    public void cookieWithDefaultReturnsFallback() throws IOException {
        final String result = RequestTestUtil.restQuery(existWebServer,
                IMPORT + "request:cookie('missing', 'default-cookie')");
        assertEquals("default-cookie", result.trim());
    }

    // ===== request:cookie-names() =====

    @Test
    public void cookieNamesReturnsNames() throws IOException {
        final String result = RequestTestUtil.restQueryWithHeaders(existWebServer,
                IMPORT + "string-join(request:cookie-names(), ',')",
                Map.of("Cookie", "a=1; b=2; c=3"));
        final String trimmed = result.trim();
        assertTrue("cookie-names() should contain a, got: " + trimmed, trimmed.contains("a"));
        assertTrue("cookie-names() should contain b, got: " + trimmed, trimmed.contains("b"));
        assertTrue("cookie-names() should contain c, got: " + trimmed, trimmed.contains("c"));
    }

    @Test
    public void cookieNamesEmptyWhenNoCookies() throws IOException {
        final String result = RequestTestUtil.restQuery(existWebServer,
                IMPORT +
                "let $names := request:cookie-names()\n" +
                "return count($names)");
        assertEquals("0", result.trim());
    }

    // ===== request:cookie-map() =====

    @Test
    public void cookieMapReturnsMap() throws IOException {
        final String result = RequestTestUtil.restQueryWithHeaders(existWebServer,
                IMPORT +
                "let $m := request:cookie-map()\n" +
                "return $m?flavor",
                Map.of("Cookie", "flavor=chocolate"));
        assertEquals("chocolate", result.trim());
    }

    @Test
    public void cookieMapContainsAllCookies() throws IOException {
        final String result = RequestTestUtil.restQueryWithHeaders(existWebServer,
                IMPORT +
                "let $m := request:cookie-map()\n" +
                "return map:size($m)",
                Map.of("Cookie", "x=1; y=2"));
        assertEquals("2", result.trim());
    }
}
