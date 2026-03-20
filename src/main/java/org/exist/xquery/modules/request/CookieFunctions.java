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

import jakarta.servlet.http.Cookie;
import org.exist.http.servlets.RequestWrapper;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.*;

import javax.annotation.Nonnull;

import static org.exist.xquery.FunctionDSL.*;

/**
 * Implements request:cookie(), request:cookie-names(), and request:cookie-map().
 */
public class CookieFunctions extends AbstractRequestFunction {

    private static final String FS_COOKIE_NAME = "cookie";
    public static final FunctionSignature[] FS_COOKIE = functionSignatures(
            RequestModule.qname(FS_COOKIE_NAME),
            "Returns the value of the named cookie.",
            returnsOpt(Type.STRING, "the cookie value, or empty if not found"),
            arities(
                    arity(
                            param("name", Type.STRING, "the cookie name")
                    ),
                    arity(
                            param("name", Type.STRING, "the cookie name"),
                            param("default", Type.STRING, "default value if cookie is not present")
                    )
            )
    );

    private static final String FS_COOKIE_NAMES_NAME = "cookie-names";
    public static final FunctionSignature FS_COOKIE_NAMES = functionSignature(
            RequestModule.qname(FS_COOKIE_NAMES_NAME),
            "Returns the names of all cookies in the current request.",
            returnsOptMany(Type.STRING, "the cookie names")
    );

    private static final String FS_COOKIE_MAP_NAME = "cookie-map";
    public static final FunctionSignature FS_COOKIE_MAP = functionSignature(
            RequestModule.qname(FS_COOKIE_MAP_NAME),
            "Returns all cookies as a map. Each key is a cookie name, each value is the cookie value.",
            returns(Type.MAP, "a map of cookie names to values")
    );

    public CookieFunctions(final XQueryContext context, final FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    protected Sequence eval(final Sequence[] args, @Nonnull final RequestWrapper request)
            throws XPathException {
        if (isCalledAs(FS_COOKIE_NAME)) {
            return getCookie(args, request);

        } else if (isCalledAs(FS_COOKIE_NAMES_NAME)) {
            return getCookieNames(request);

        } else if (isCalledAs(FS_COOKIE_MAP_NAME)) {
            return getCookieMap(request);

        } else {
            throw new XPathException(this, "Unknown function: " + getSignature());
        }
    }

    private Sequence getCookie(final Sequence[] args, final RequestWrapper request)
            throws XPathException {
        final String name = args[0].getStringValue();
        final Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return new StringValue(this, cookie.getValue());
                }
            }
        }

        // Not found — return default if provided, else empty
        if (args.length > 1) {
            return args[1];
        }
        return Sequence.EMPTY_SEQUENCE;
    }

    private Sequence getCookieNames(final RequestWrapper request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return Sequence.EMPTY_SEQUENCE;
        }

        final ValueSequence result = new ValueSequence();
        for (final Cookie cookie : cookies) {
            result.add(new StringValue(this, cookie.getName()));
        }
        return result;
    }

    private Sequence getCookieMap(final RequestWrapper request) throws XPathException {
        final MapType map = new MapType(this, context);
        final Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                map.add(new StringValue(this, cookie.getName()),
                        new StringValue(this, cookie.getValue()));
            }
        }
        return map;
    }
}
