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

import org.exist.source.Source;
import org.exist.source.StringSource;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.test.ExistEmbeddedServer;
import org.exist.xquery.CompiledXQuery;
import org.exist.xquery.XQuery;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceIterator;

import java.util.Optional;

/**
 * Shared utilities for Request module tests.
 * Executes XQuery via the embedded BrokerPool with a mock HTTP request context.
 */
public final class RequestTestUtil {

    static final String IMPORT =
            "import module namespace exrequest = 'http://exquery.org/ns/request';\n";

    private RequestTestUtil() {
        // utility class
    }

    /**
     * Execute an XQuery with a mock HTTP request injected into the XQuery context.
     * Each call gets a fresh broker and context to avoid module caching issues.
     */
    public static String executeWithRequest(final ExistEmbeddedServer server,
                                             final String xquery,
                                             final MockRequestWrapper request)
            throws Exception {
        final BrokerPool pool = server.getBrokerPool();

        // Each test gets a fresh broker to avoid module caching issues
        final DBBroker broker = pool.get(
                Optional.of(pool.getSecurityManager().getSystemSubject()));
        try {
            final XQuery xqueryService = pool.getXQueryService();
            final XQueryContext context = new XQueryContext(pool);
            context.setHttpContext(new XQueryContext.HttpContext(request, null));
            final String fullQuery = IMPORT + xquery;
            final CompiledXQuery compiled = xqueryService.compile(context, fullQuery);
            final Sequence result = xqueryService.execute(broker, compiled, null);
            return sequenceToString(result);
        } finally {
            broker.close();
        }
    }

    /**
     * Execute an XQuery with a default mock HTTP request (GET to localhost:8080).
     */
    public static String executeWithDefaults(final ExistEmbeddedServer server,
                                              final String xquery)
            throws Exception {
        return executeWithRequest(server, xquery, new MockRequestWrapper());
    }

    /**
     * Convert a Sequence result to a space-separated string of item string values.
     */
    private static String sequenceToString(final Sequence sequence) throws Exception {
        if (sequence.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        final SequenceIterator it = sequence.iterate();
        boolean first = true;
        while (it.hasNext()) {
            final Item item = it.nextItem();
            if (!first) {
                sb.append(' ');
            }
            sb.append(item.getStringValue());
            first = false;
        }
        return sb.toString();
    }
}
