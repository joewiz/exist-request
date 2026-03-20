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
 * Tests for request:attribute(), request:attribute-names(),
 * request:attribute-map(), and request:set-attribute().
 */
public class AttributeFunctionsTest {

    @ClassRule
    public static final ExistWebServer existWebServer =
            new ExistWebServer(true, false, true, true);

    private static final String IMPORT =
            "import module namespace request = 'http://exquery.org/ns/request';\n";

    private String restQuery(final String xquery) throws IOException {
        return RequestTestUtil.restQuery(existWebServer, xquery);
    }

    // ===== request:set-attribute() + request:attribute() round-trip =====

    @Test
    public void setAttributeThenGetAttribute() throws IOException {
        final String result = restQuery(IMPORT +
                "let $_ := request:set-attribute('test-key', 'test-value')\n" +
                "return request:attribute('test-key')");
        assertEquals("test-value", result.trim());
    }

    @Test
    public void setAttributeReturnsEmptySequence() throws IOException {
        final String result = restQuery(IMPORT +
                "let $result := request:set-attribute('key', 'val')\n" +
                "return if (empty($result)) then 'empty' else 'not-empty'");
        assertEquals("empty", result.trim());
    }

    // ===== request:attribute($name) =====

    @Test
    public void attributeReturnsMissingAsEmpty() throws IOException {
        final String result = restQuery(IMPORT +
                "let $a := request:attribute('nonexistent-attr')\n" +
                "return if (empty($a)) then 'empty' else 'found'");
        assertEquals("empty", result.trim());
    }

    // ===== request:attribute($name, $default) =====

    @Test
    public void attributeWithDefaultReturnsValue() throws IOException {
        final String result = restQuery(IMPORT +
                "let $_ := request:set-attribute('myattr', 'myvalue')\n" +
                "return request:attribute('myattr', 'fallback')");
        assertEquals("myvalue", result.trim());
    }

    @Test
    public void attributeWithDefaultReturnsFallback() throws IOException {
        final String result = restQuery(IMPORT +
                "request:attribute('missing-attr', 'fallback')");
        assertEquals("fallback", result.trim());
    }

    // ===== request:attribute-names() =====

    @Test
    public void attributeNamesIncludesSetAttribute() throws IOException {
        final String result = restQuery(IMPORT +
                "let $_ := request:set-attribute('my-named-attr', 'val')\n" +
                "return request:attribute-names() = 'my-named-attr'");
        assertEquals("true", result.trim());
    }

    // ===== request:attribute-map() =====

    @Test
    public void attributeMapContainsSetAttributes() throws IOException {
        final String result = restQuery(IMPORT +
                "let $_ := request:set-attribute('map-test', 'map-val')\n" +
                "let $m := request:attribute-map()\n" +
                "return $m?('map-test')");
        assertEquals("map-val", result.trim());
    }

    @Test
    public void attributeMapReturnsMapType() throws IOException {
        final String result = restQuery(IMPORT +
                "let $_ := request:set-attribute('k', 'v')\n" +
                "return request:attribute-map() instance of map(*)");
        assertEquals("true", result.trim());
    }

    // ===== Multiple attributes =====

    @Test
    public void multipleAttributesRoundTrip() throws IOException {
        final String result = restQuery(IMPORT +
                "let $_ := (request:set-attribute('a', '1'),\n" +
                "           request:set-attribute('b', '2'),\n" +
                "           request:set-attribute('c', '3'))\n" +
                "return string-join((request:attribute('a'), request:attribute('b'), request:attribute('c')), ',')");
        assertEquals("1,2,3", result.trim());
    }
}
