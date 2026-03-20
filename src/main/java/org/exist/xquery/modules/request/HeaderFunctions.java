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
import java.util.Enumeration;

import static org.exist.xquery.FunctionDSL.*;

/**
 * Implements request:header-names(), request:header(), and request:header-map().
 */
public class HeaderFunctions extends AbstractRequestFunction {

    private static final String FS_HEADER_NAMES_NAME = "header-names";
    public static final FunctionSignature FS_HEADER_NAMES = functionSignature(
            RequestModule.qname(FS_HEADER_NAMES_NAME),
            "Returns the names of all HTTP headers in the current request.",
            returnsOptMany(Type.STRING, "the header names")
    );

    private static final String FS_HEADER_NAME = "header";
    public static final FunctionSignature[] FS_HEADER = functionSignatures(
            RequestModule.qname(FS_HEADER_NAME),
            "Returns the value of the named HTTP request header.",
            returnsOptMany(Type.STRING, "the header value(s)"),
            arities(
                    arity(
                            param("name", Type.STRING, "the header name")
                    ),
                    arity(
                            param("name", Type.STRING, "the header name"),
                            param("default", Type.STRING, "default value if header is not present")
                    )
            )
    );

    private static final String FS_HEADER_MAP_NAME = "header-map";
    public static final FunctionSignature FS_HEADER_MAP = functionSignature(
            RequestModule.qname(FS_HEADER_MAP_NAME),
            "Returns all HTTP headers as a map. Each key is a header name, " +
            "each value is the header value string.",
            returns(Type.MAP, "a map of header names to values")
    );

    public HeaderFunctions(final XQueryContext context, final FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    protected Sequence eval(final Sequence[] args, @Nonnull final RequestWrapper request)
            throws XPathException {
        if (isCalledAs(FS_HEADER_NAMES_NAME)) {
            return getHeaderNames(request);

        } else if (isCalledAs(FS_HEADER_NAME)) {
            return getHeader(args, request);

        } else if (isCalledAs(FS_HEADER_MAP_NAME)) {
            return getHeaderMap(request);

        } else {
            throw new XPathException(this, "Unknown function: " + getSignature());
        }
    }

    private Sequence getHeaderNames(final RequestWrapper request) {
        final Enumeration<String> names = request.getHeaderNames();
        if (names == null || !names.hasMoreElements()) {
            return Sequence.EMPTY_SEQUENCE;
        }

        final ValueSequence result = new ValueSequence();
        while (names.hasMoreElements()) {
            result.add(new StringValue(this, names.nextElement()));
        }
        return result;
    }

    private Sequence getHeader(final Sequence[] args, final RequestWrapper request)
            throws XPathException {
        final String name = args[0].getStringValue();
        final String value = request.getHeader(name);

        if (value == null) {
            if (args.length > 1) {
                return args[1];
            }
            return Sequence.EMPTY_SEQUENCE;
        }
        return new StringValue(this, value);
    }

    private Sequence getHeaderMap(final RequestWrapper request) throws XPathException {
        final MapType map = new MapType(this, context);
        final Enumeration<String> names = request.getHeaderNames();

        if (names != null) {
            while (names.hasMoreElements()) {
                final String name = names.nextElement();
                final Enumeration<String> values = request.getHeaders(name);

                if (values != null && values.hasMoreElements()) {
                    final String firstValue = values.nextElement();
                    if (!values.hasMoreElements()) {
                        // Single value — return as string
                        map.add(new StringValue(this, name), new StringValue(this, firstValue));
                    } else {
                        // Multiple values — return as sequence
                        final ValueSequence seq = new ValueSequence();
                        seq.add(new StringValue(this, firstValue));
                        while (values.hasMoreElements()) {
                            seq.add(new StringValue(this, values.nextElement()));
                        }
                        map.add(new StringValue(this, name), seq);
                    }
                }
            }
        }
        return map;
    }
}
