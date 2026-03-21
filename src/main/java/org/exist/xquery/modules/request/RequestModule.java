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

import org.exist.dom.QName;
import org.exist.xquery.AbstractInternalModule;
import org.exist.xquery.ErrorCodes.ErrorCode;
import org.exist.xquery.FunctionDef;

import java.util.List;
import java.util.Map;

import static org.exist.xquery.FunctionDSL.functionDefs;

/**
 * Native EXQuery Request Module for eXist-db.
 *
 * <p>Implements the full 28-function EXQuery Request Module API,
 * compatible with BaseX's implementation. Uses the namespace
 * {@code http://exquery.org/ns/request}.</p>
 *
 * <p>This module accesses the HTTP request via eXist's native
 * {@code XQueryContext.getHttpContext()} mechanism, which works with
 * the REST API, XQueryServlet, and URL rewriting.</p>
 */
public class RequestModule extends AbstractInternalModule {

    public static final String NAMESPACE_URI = "http://exquery.org/ns/request";

    public static final String PREFIX = "exrequest";

    public static final String RELEASE = "1.0.0";

    public static final ErrorCode PARAMETER = new ErrorCode(
            new QName("parameter", NAMESPACE_URI, PREFIX),
            "Request has invalid parameters"
    );

    public static final FunctionDef[] functions = functionDefs(
            functionDefs(GeneralFunctions.class,
                    GeneralFunctions.FS_METHOD,
                    GeneralFunctions.FS_CONTEXT_PATH),
            functionDefs(URIFunctions.class,
                    URIFunctions.FS_SCHEME,
                    URIFunctions.FS_HOSTNAME,
                    URIFunctions.FS_PORT,
                    URIFunctions.FS_PATH,
                    URIFunctions.FS_QUERY,
                    URIFunctions.FS_URI),
            functionDefs(ConnectionFunctions.class,
                    ConnectionFunctions.FS_ADDRESS,
                    ConnectionFunctions.FS_REMOTE_HOSTNAME,
                    ConnectionFunctions.FS_REMOTE_ADDRESS,
                    ConnectionFunctions.FS_REMOTE_PORT),
            // ParameterFunctions — split because FS_PARAMETER is FunctionSignature[]
            functionDefs(ParameterFunctions.class,
                    ParameterFunctions.FS_PARAMETER_NAMES,
                    ParameterFunctions.FS_PARAMETER_MAP),
            functionDefs(ParameterFunctions.class,
                    ParameterFunctions.FS_PARAMETER),
            // HeaderFunctions — split because FS_HEADER is FunctionSignature[]
            functionDefs(HeaderFunctions.class,
                    HeaderFunctions.FS_HEADER_NAMES,
                    HeaderFunctions.FS_HEADER_MAP),
            functionDefs(HeaderFunctions.class,
                    HeaderFunctions.FS_HEADER),
            // CookieFunctions — split because FS_COOKIE is FunctionSignature[]
            functionDefs(CookieFunctions.class,
                    CookieFunctions.FS_COOKIE_NAMES,
                    CookieFunctions.FS_COOKIE_MAP),
            functionDefs(CookieFunctions.class,
                    CookieFunctions.FS_COOKIE),
            // AttributeFunctions — split because FS_ATTRIBUTE is FunctionSignature[]
            functionDefs(AttributeFunctions.class,
                    AttributeFunctions.FS_ATTRIBUTE_NAMES,
                    AttributeFunctions.FS_ATTRIBUTE_MAP,
                    AttributeFunctions.FS_SET_ATTRIBUTE),
            functionDefs(AttributeFunctions.class,
                    AttributeFunctions.FS_ATTRIBUTE)
    );

    public RequestModule(final Map<String, List<?>> parameters) {
        super(functions, parameters, false);
    }

    @Override
    public String getNamespaceURI() {
        return NAMESPACE_URI;
    }

    @Override
    public String getDefaultPrefix() {
        return PREFIX;
    }

    @Override
    public String getDescription() {
        return "EXQuery Request Module — native implementation of HTTP request introspection functions";
    }

    @Override
    public String getReleaseVersion() {
        return RELEASE;
    }

    static QName qname(final String localPart) {
        return new QName(localPart, NAMESPACE_URI, PREFIX);
    }
}
