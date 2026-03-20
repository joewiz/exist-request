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
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;

import javax.annotation.Nonnull;

import static org.exist.xquery.FunctionDSL.*;

/**
 * Implements request:method() and request:context-path().
 */
public class GeneralFunctions extends AbstractRequestFunction {

    private static final String FS_METHOD_NAME = "method";
    public static final FunctionSignature FS_METHOD = functionSignature(
            RequestModule.qname(FS_METHOD_NAME),
            "Returns the HTTP method of the current request (e.g., GET, POST, PUT, DELETE).",
            returns(Type.STRING, "the HTTP method")
    );

    private static final String FS_CONTEXT_PATH_NAME = "context-path";
    public static final FunctionSignature FS_CONTEXT_PATH = functionSignature(
            RequestModule.qname(FS_CONTEXT_PATH_NAME),
            "Returns the servlet context path. Returns an empty string for the root context.",
            returns(Type.STRING, "the servlet context path")
    );

    public GeneralFunctions(final XQueryContext context, final FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    protected Sequence eval(final Sequence[] args, @Nonnull final RequestWrapper request)
            throws XPathException {
        if (isCalledAs(FS_METHOD_NAME)) {
            return new StringValue(this, request.getMethod());
        } else if (isCalledAs(FS_CONTEXT_PATH_NAME)) {
            final String contextPath = request.getContextPath();
            return new StringValue(this, contextPath != null ? contextPath : "");
        } else {
            throw new XPathException(this, "Unknown function: " + getSignature());
        }
    }
}
