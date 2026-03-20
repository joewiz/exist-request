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
import org.exist.xquery.*;
import org.exist.xquery.value.Sequence;

import javax.annotation.Nonnull;

/**
 * Base class for all EXQuery Request Module functions.
 *
 * <p>Retrieves the HTTP request from the XQuery context via
 * {@code context.getHttpContext().getRequest()}. Throws XPDY0002
 * if no HTTP context is available (e.g., when called outside an HTTP request).</p>
 */
public abstract class AbstractRequestFunction extends BasicFunction {

    public AbstractRequestFunction(final XQueryContext context, final FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    public final Sequence eval(final Sequence[] args, final Sequence contextSequence)
            throws XPathException {
        final XQueryContext.HttpContext httpContext = context.getHttpContext();
        if (httpContext == null) {
            throw new XPathException(this, ErrorCodes.XPDY0002,
                    "No HTTP request context available. " +
                    "The request module functions can only be called within an HTTP request.");
        }

        final RequestWrapper request = httpContext.getRequest();
        if (request == null) {
            throw new XPathException(this, ErrorCodes.XPDY0002,
                    "No HTTP request object found in the current XQuery context.");
        }

        return eval(args, request);
    }

    /**
     * Evaluate the function with the HTTP request.
     *
     * @param args    the arguments to the function
     * @param request the HTTP request wrapper
     * @return the result of the function
     * @throws XPathException an XPath Exception
     */
    protected abstract Sequence eval(final Sequence[] args, @Nonnull final RequestWrapper request)
            throws XPathException;
}
