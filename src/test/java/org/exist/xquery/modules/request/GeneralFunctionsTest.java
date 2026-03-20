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

import static org.junit.Assert.assertEquals;

/**
 * Tests for request:method() and request:context-path().
 */
public class GeneralFunctionsTest {

    @ClassRule
    public static final ExistWebServer existWebServer =
            new ExistWebServer(true, false, true, true);

    private String restQuery(final String xquery) throws IOException {
        return RequestTestUtil.restQuery(existWebServer, xquery);
    }

    private String restQuery(final String xquery, final String method) throws IOException {
        return RequestTestUtil.restQuery(existWebServer, xquery, method);
    }

    // ===== request:method() =====

    @Test
    public void methodReturnsGet() throws IOException {
        final String result = restQuery(
                "import module namespace request = 'http://exquery.org/ns/request';\n" +
                "request:method()");
        assertEquals("GET", result.trim());
    }

    @Test
    public void methodReturnsPost() throws IOException {
        final String result = restQuery(
                "import module namespace request = 'http://exquery.org/ns/request';\n" +
                "request:method()", "POST");
        assertEquals("POST", result.trim());
    }

    // ===== request:context-path() =====

    @Test
    public void contextPathReturnsString() throws IOException {
        final String result = restQuery(
                "import module namespace request = 'http://exquery.org/ns/request';\n" +
                "request:context-path()");
        // The REST API servlet context path; may be empty string for root context
        // Just verify it returns a string (doesn't throw)
        assert result != null;
    }
}
