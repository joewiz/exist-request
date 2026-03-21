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
import org.exist.xquery.functions.map.MapType;
import org.exist.xquery.value.*;

import javax.annotation.Nonnull;
import java.util.Enumeration;

import static org.exist.xquery.FunctionDSL.*;

/**
 * Implements request:parameter-names(), request:parameter(), and request:parameter-map().
 */
public class ParameterFunctions extends AbstractRequestFunction {

    private static final String FS_PARAMETER_NAMES_NAME = "parameter-names";
    public static final FunctionSignature FS_PARAMETER_NAMES = functionSignature(
            RequestModule.qname(FS_PARAMETER_NAMES_NAME),
            "Returns the names of all query and form parameters.",
            returnsOptMany(Type.STRING, "the parameter names")
    );

    private static final String FS_PARAMETER_NAME = "parameter";
    public static final FunctionSignature[] FS_PARAMETER = functionSignatures(
            RequestModule.qname(FS_PARAMETER_NAME),
            "Returns the value(s) of the named request parameter.",
            returnsOptMany(Type.STRING, "the parameter value(s)"),
            arities(
                    arity(
                            param("name", Type.STRING, "the parameter name")
                    ),
                    arity(
                            param("name", Type.STRING, "the parameter name"),
                            param("default", Type.STRING, "default value if parameter is not present")
                    )
            )
    );

    private static final String FS_PARAMETER_MAP_NAME = "parameter-map";
    public static final FunctionSignature FS_PARAMETER_MAP = functionSignature(
            RequestModule.qname(FS_PARAMETER_MAP_NAME),
            "Returns all query and form parameters as a map. Each key is a parameter name, " +
            "each value is one or more parameter values.",
            returns(Type.MAP_ITEM, "a map of parameter names to values")
    );

    public ParameterFunctions(final XQueryContext context, final FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    protected Sequence eval(final Sequence[] args, @Nonnull final RequestWrapper request)
            throws XPathException {
        if (isCalledAs(FS_PARAMETER_NAMES_NAME)) {
            return getParameterNames(request);

        } else if (isCalledAs(FS_PARAMETER_NAME)) {
            return getParameter(args, request);

        } else if (isCalledAs(FS_PARAMETER_MAP_NAME)) {
            return getParameterMap(request);

        } else {
            throw new XPathException(this, "Unknown function: " + getSignature());
        }
    }

    private Sequence getParameterNames(final RequestWrapper request) {
        final Enumeration<String> names = request.getParameterNames();
        if (!names.hasMoreElements()) {
            return Sequence.EMPTY_SEQUENCE;
        }

        final ValueSequence result = new ValueSequence();
        while (names.hasMoreElements()) {
            result.add(new StringValue(this, names.nextElement()));
        }
        return result;
    }

    private Sequence getParameter(final Sequence[] args, final RequestWrapper request)
            throws XPathException {
        final String name = args[0].getStringValue();
        final String[] values = request.getParameterValues(name);

        if (values == null || values.length == 0) {
            // Return default if provided, else empty sequence
            if (args.length > 1) {
                return args[1];
            }
            return Sequence.EMPTY_SEQUENCE;
        }

        if (values.length == 1) {
            return new StringValue(this, values[0]);
        }

        final ValueSequence result = new ValueSequence();
        for (final String value : values) {
            result.add(new StringValue(this, value));
        }
        return result;
    }

    private Sequence getParameterMap(final RequestWrapper request) throws XPathException {
        final MapType map = new MapType(this, context);
        final Enumeration<String> names = request.getParameterNames();

        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final String[] values = request.getParameterValues(name);

            if (values != null && values.length > 0) {
                final Sequence seq;
                if (values.length == 1) {
                    seq = new StringValue(this, values[0]);
                } else {
                    final ValueSequence vs = new ValueSequence();
                    for (final String v : values) {
                        vs.add(new StringValue(this, v));
                    }
                    seq = vs;
                }
                map.add(new StringValue(this, name), seq);
            }
        }
        return map;
    }
}
