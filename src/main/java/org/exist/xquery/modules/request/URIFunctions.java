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

import org.exist.http.servlets.RequestWrapper;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.*;

import javax.annotation.Nonnull;

import static org.exist.xquery.FunctionDSL.*;

/**
 * Implements request:scheme(), request:hostname(), request:port(),
 * request:path(), request:query(), and request:uri().
 */
public class URIFunctions extends AbstractRequestFunction {

    private static final String FS_SCHEME_NAME = "scheme";
    public static final FunctionSignature FS_SCHEME = functionSignature(
            RequestModule.qname(FS_SCHEME_NAME),
            "Returns the scheme of the current request URI (e.g., http, https).",
            returns(Type.STRING, "the URI scheme")
    );

    private static final String FS_HOSTNAME_NAME = "hostname";
    public static final FunctionSignature FS_HOSTNAME = functionSignature(
            RequestModule.qname(FS_HOSTNAME_NAME),
            "Returns the hostname of the current request.",
            returns(Type.STRING, "the hostname")
    );

    private static final String FS_PORT_NAME = "port";
    public static final FunctionSignature FS_PORT = functionSignature(
            RequestModule.qname(FS_PORT_NAME),
            "Returns the port number of the current request.",
            returns(Type.INTEGER, "the port number")
    );

    private static final String FS_PATH_NAME = "path";
    public static final FunctionSignature FS_PATH = functionSignature(
            RequestModule.qname(FS_PATH_NAME),
            "Returns the path component of the current request URI.",
            returns(Type.STRING, "the request path")
    );

    private static final String FS_QUERY_NAME = "query";
    public static final FunctionSignature FS_QUERY = functionSignature(
            RequestModule.qname(FS_QUERY_NAME),
            "Returns the query string of the current request, or the empty sequence if none.",
            returnsOptMany(Type.STRING, "the query string, or empty")
    );

    private static final String FS_URI_NAME = "uri";
    public static final FunctionSignature FS_URI = functionSignature(
            RequestModule.qname(FS_URI_NAME),
            "Returns the request URI.",
            returns(Type.ANY_URI, "the request URI")
    );

    public URIFunctions(final XQueryContext context, final FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    protected Sequence eval(final Sequence[] args, @Nonnull final RequestWrapper request)
            throws XPathException {
        if (isCalledAs(FS_SCHEME_NAME)) {
            return new StringValue(this, request.getScheme());

        } else if (isCalledAs(FS_HOSTNAME_NAME)) {
            return new StringValue(this, request.getServerName());

        } else if (isCalledAs(FS_PORT_NAME)) {
            return new IntegerValue(this, request.getServerPort());

        } else if (isCalledAs(FS_PATH_NAME)) {
            return new StringValue(this, request.getRequestURI());

        } else if (isCalledAs(FS_QUERY_NAME)) {
            final String query = request.getQueryString();
            return query == null ? Sequence.EMPTY_SEQUENCE : new StringValue(this, query);

        } else if (isCalledAs(FS_URI_NAME)) {
            return new AnyURIValue(this, request.getRequestURI());

        } else {
            throw new XPathException(this, "Unknown function: " + getSignature());
        }
    }
}
