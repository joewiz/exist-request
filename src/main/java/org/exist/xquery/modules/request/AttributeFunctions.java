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
import org.exist.xquery.functions.map.MapType;
import org.exist.xquery.value.*;

import javax.annotation.Nonnull;
import java.util.Enumeration;

import static org.exist.xquery.FunctionDSL.*;

/**
 * Implements request:attribute(), request:attribute-names(),
 * request:attribute-map(), and request:set-attribute().
 */
public class AttributeFunctions extends AbstractRequestFunction {

    private static final String FS_ATTRIBUTE_NAME = "attribute";
    public static final FunctionSignature[] FS_ATTRIBUTE = functionSignatures(
            RequestModule.qname(FS_ATTRIBUTE_NAME),
            "Returns the value of the named request attribute.",
            returnsOptMany(Type.ITEM, "the attribute value, or empty if not found"),
            arities(
                    arity(
                            param("name", Type.STRING, "the attribute name")
                    ),
                    arity(
                            param("name", Type.STRING, "the attribute name"),
                            param("default", Type.ITEM, "default value if attribute is not present")
                    )
            )
    );

    private static final String FS_ATTRIBUTE_NAMES_NAME = "attribute-names";
    public static final FunctionSignature FS_ATTRIBUTE_NAMES = functionSignature(
            RequestModule.qname(FS_ATTRIBUTE_NAMES_NAME),
            "Returns the names of all request attributes.",
            returnsOptMany(Type.STRING, "the attribute names")
    );

    private static final String FS_ATTRIBUTE_MAP_NAME = "attribute-map";
    public static final FunctionSignature FS_ATTRIBUTE_MAP = functionSignature(
            RequestModule.qname(FS_ATTRIBUTE_MAP_NAME),
            "Returns all request attributes as a map.",
            returns(Type.MAP_ITEM, "a map of attribute names to values")
    );

    private static final String FS_SET_ATTRIBUTE_NAME = "set-attribute";
    public static final FunctionSignature FS_SET_ATTRIBUTE = functionSignature(
            RequestModule.qname(FS_SET_ATTRIBUTE_NAME),
            "Sets a request attribute. Returns the empty sequence.",
            returns(Type.EMPTY_SEQUENCE, "empty sequence"),
            params(
                    param("name", Type.STRING, "the attribute name"),
                    param("value", Type.ITEM, "the attribute value")
            )
    );

    public AttributeFunctions(final XQueryContext context, final FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    protected Sequence eval(final Sequence[] args, @Nonnull final RequestWrapper request)
            throws XPathException {
        if (isCalledAs(FS_ATTRIBUTE_NAME)) {
            return getAttribute(args, request);

        } else if (isCalledAs(FS_ATTRIBUTE_NAMES_NAME)) {
            return getAttributeNames(request);

        } else if (isCalledAs(FS_ATTRIBUTE_MAP_NAME)) {
            return getAttributeMap(request);

        } else if (isCalledAs(FS_SET_ATTRIBUTE_NAME)) {
            return setAttribute(args, request);

        } else {
            throw new XPathException(this, "Unknown function: " + getSignature());
        }
    }

    private Sequence getAttribute(final Sequence[] args, final RequestWrapper request)
            throws XPathException {
        final String name = args[0].getStringValue();
        final Object value = request.getAttribute(name);

        if (value == null) {
            if (args.length > 1) {
                return args[1];
            }
            return Sequence.EMPTY_SEQUENCE;
        }

        return XPathUtil.javaObjectToXPath(value, context, this);
    }

    private Sequence getAttributeNames(final RequestWrapper request) {
        final Enumeration<String> names = request.getAttributeNames();
        if (!names.hasMoreElements()) {
            return Sequence.EMPTY_SEQUENCE;
        }

        final ValueSequence result = new ValueSequence();
        while (names.hasMoreElements()) {
            result.add(new StringValue(this, names.nextElement()));
        }
        return result;
    }

    private Sequence getAttributeMap(final RequestWrapper request) throws XPathException {
        final MapType map = new MapType(this, context);
        final Enumeration<String> names = request.getAttributeNames();

        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final Object value = request.getAttribute(name);
            if (value != null) {
                map.add(new StringValue(this, name),
                        XPathUtil.javaObjectToXPath(value, context, this));
            }
        }
        return map;
    }

    private Sequence setAttribute(final Sequence[] args, final RequestWrapper request)
            throws XPathException {
        final String name = args[0].getStringValue();
        final Sequence value = args[1];

        // Store the XQuery value as a Java object on the request
        if (value.isEmpty()) {
            request.removeAttribute(name);
        } else if (value.getItemCount() == 1) {
            request.setAttribute(name, value.getStringValue());
        } else {
            // Multi-valued: store as string for now (most common use case)
            request.setAttribute(name, value.getStringValue());
        }

        return Sequence.EMPTY_SEQUENCE;
    }
}
