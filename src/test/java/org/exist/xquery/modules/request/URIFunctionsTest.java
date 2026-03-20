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
 * Tests for request:scheme(), request:hostname(), request:port(),
 * request:path(), request:query(), and request:uri().
 */
public class URIFunctionsTest {

    @ClassRule
    public static final ExistWebServer existWebServer =
            new ExistWebServer(true, false, true, true);

    private static final String IMPORT =
            "import module namespace request = 'http://exquery.org/ns/request';\n";

    private String restQuery(final String xquery) throws IOException {
        return RequestTestUtil.restQuery(existWebServer, xquery);
    }

    // ===== request:scheme() =====

    @Test
    public void schemeReturnsHttp() throws IOException {
        final String result = restQuery(IMPORT + "request:scheme()");
        assertEquals("http", result.trim());
    }

    // ===== request:hostname() =====

    @Test
    public void hostnameReturnsLocalhost() throws IOException {
        final String result = restQuery(IMPORT + "request:hostname()");
        assertEquals("localhost", result.trim());
    }

    // ===== request:port() =====

    @Test
    public void portReturnsServerPort() throws IOException {
        final String result = restQuery(IMPORT + "request:port()");
        assertEquals(String.valueOf(existWebServer.getPort()), result.trim());
    }

    // ===== request:path() =====

    @Test
    public void pathReturnsRequestPath() throws IOException {
        final String result = restQuery(IMPORT + "request:path()");
        // The REST API path includes /rest/db
        assertTrue("path() should contain /rest/db, got: " + result.trim(),
                result.trim().contains("/rest/db") || result.trim().contains("/db"));
    }

    // ===== request:query() =====

    @Test
    public void queryReturnsQueryString() throws IOException {
        // The _query parameter itself will be part of the query string
        final String result = restQuery(IMPORT +
                "let $q := request:query()\n" +
                "return if (exists($q)) then 'has-query' else 'no-query'");
        assertEquals("has-query", result.trim());
    }

    @Test
    public void queryContainsParameter() throws IOException {
        final String result = RequestTestUtil.restQueryWithParams(existWebServer,
                IMPORT + "request:query()",
                "foo=bar");
        assertTrue("query() should contain foo=bar, got: " + result.trim(),
                result.trim().contains("foo=bar"));
    }

    // ===== request:uri() =====

    @Test
    public void uriReturnsFullUri() throws IOException {
        final String result = restQuery(IMPORT + "request:uri()");
        // URI should contain the path portion
        assertTrue("uri() should contain path info, got: " + result.trim(),
                result.trim().contains("/rest/db") || result.trim().contains("/db"));
    }
}
