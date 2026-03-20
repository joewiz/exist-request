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

import static org.junit.Assert.*;

/**
 * Tests for request:parameter(), request:parameter-names(),
 * and request:parameter-map().
 */
public class ParameterFunctionsTest {

    @ClassRule
    public static final ExistWebServer existWebServer =
            new ExistWebServer(true, false, true, true);

    private static final String IMPORT =
            "import module namespace request = 'http://exquery.org/ns/request';\n";

    // ===== request:parameter-names() =====

    @Test
    public void parameterNamesReturnsNames() throws IOException {
        // _query is always present as a parameter in REST API GET requests
        final String result = RequestTestUtil.restQuery(existWebServer,
                IMPORT + "string-join(request:parameter-names(), ',')");
        assertTrue("parameter-names() should contain _query, got: " + result.trim(),
                result.trim().contains("_query"));
    }

    @Test
    public void parameterNamesIncludesCustomParam() throws IOException {
        final String result = RequestTestUtil.restQueryWithParams(existWebServer,
                IMPORT + "string-join(request:parameter-names(), ',')",
                "myParam=hello");
        assertTrue("parameter-names() should include myParam, got: " + result.trim(),
                result.trim().contains("myParam"));
    }

    // ===== request:parameter($name) =====

    @Test
    public void parameterReturnsValue() throws IOException {
        final String result = RequestTestUtil.restQueryWithParams(existWebServer,
                IMPORT + "request:parameter('color')",
                "color=blue");
        assertEquals("blue", result.trim());
    }

    @Test
    public void parameterReturnsMissingAsEmpty() throws IOException {
        final String result = RequestTestUtil.restQuery(existWebServer,
                IMPORT +
                "let $p := request:parameter('nonexistent')\n" +
                "return if (empty($p)) then 'empty' else 'found'");
        assertEquals("empty", result.trim());
    }

    @Test
    public void parameterReturnsMultipleValues() throws IOException {
        final String result = RequestTestUtil.restQueryWithParams(existWebServer,
                IMPORT + "count(request:parameter('item'))",
                "item=a&item=b&item=c");
        assertEquals("3", result.trim());
    }

    // ===== request:parameter($name, $default) =====

    @Test
    public void parameterWithDefaultReturnsValue() throws IOException {
        final String result = RequestTestUtil.restQueryWithParams(existWebServer,
                IMPORT + "request:parameter('color', 'red')",
                "color=blue");
        assertEquals("blue", result.trim());
    }

    @Test
    public void parameterWithDefaultReturnsFallback() throws IOException {
        final String result = RequestTestUtil.restQuery(existWebServer,
                IMPORT + "request:parameter('nonexistent', 'fallback')");
        assertEquals("fallback", result.trim());
    }

    // ===== request:parameter-map() =====

    @Test
    public void parameterMapReturnsMap() throws IOException {
        final String result = RequestTestUtil.restQueryWithParams(existWebServer,
                IMPORT +
                "let $m := request:parameter-map()\n" +
                "return $m?color",
                "color=green");
        assertEquals("green", result.trim());
    }

    @Test
    public void parameterMapContainsAllParams() throws IOException {
        final String result = RequestTestUtil.restQueryWithParams(existWebServer,
                IMPORT +
                "let $m := request:parameter-map()\n" +
                "return map:size($m) >= 2",
                "x=1&y=2");
        assertEquals("true", result.trim());
    }

    @Test
    public void parameterMapMultiValuedParam() throws IOException {
        final String result = RequestTestUtil.restQueryWithParams(existWebServer,
                IMPORT +
                "let $m := request:parameter-map()\n" +
                "return count($m?item)",
                "item=a&item=b");
        assertEquals("2", result.trim());
    }
}
