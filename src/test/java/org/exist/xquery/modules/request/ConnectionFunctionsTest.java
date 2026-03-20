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
 * Tests for request:address(), request:remote-hostname(),
 * request:remote-address(), and request:remote-port().
 */
public class ConnectionFunctionsTest {

    @ClassRule
    public static final ExistWebServer existWebServer =
            new ExistWebServer(true, false, true, true);

    private static final String IMPORT =
            "import module namespace request = 'http://exquery.org/ns/request';\n";

    private String restQuery(final String xquery) throws IOException {
        return RequestTestUtil.restQuery(existWebServer, xquery);
    }

    // ===== request:address() =====

    @Test
    public void addressReturnsServerAddress() throws IOException {
        final String result = restQuery(IMPORT + "request:address()");
        // Server address should be a valid IP or hostname
        assertFalse("address() should not be empty", result.trim().isEmpty());
    }

    // ===== request:remote-hostname() =====

    @Test
    public void remoteHostnameReturnsValue() throws IOException {
        final String result = restQuery(IMPORT + "request:remote-hostname()");
        // From localhost connection, should be localhost or 127.0.0.1
        assertTrue("remote-hostname() should be localhost or 127.0.0.1, got: " + result.trim(),
                result.trim().equals("localhost") || result.trim().equals("127.0.0.1") ||
                result.trim().equals("0:0:0:0:0:0:0:1"));
    }

    // ===== request:remote-address() =====

    @Test
    public void remoteAddressReturnsIpAddress() throws IOException {
        final String result = restQuery(IMPORT + "request:remote-address()");
        // From localhost connection, should be 127.0.0.1 or ::1
        assertTrue("remote-address() should be a loopback address, got: " + result.trim(),
                result.trim().equals("127.0.0.1") || result.trim().equals("0:0:0:0:0:0:0:1"));
    }

    // ===== request:remote-port() =====

    @Test
    public void remotePortReturnsPositiveInteger() throws IOException {
        final String result = restQuery(IMPORT + "request:remote-port()");
        final int port = Integer.parseInt(result.trim());
        assertTrue("remote-port() should be positive, got: " + port, port > 0);
    }
}
