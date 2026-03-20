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
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.exist.xquery.FunctionDSL.*;

/**
 * Implements request:address(), request:remote-hostname(),
 * request:remote-address(), and request:remote-port().
 */
public class ConnectionFunctions extends AbstractRequestFunction {

    private static final String FS_ADDRESS_NAME = "address";
    public static final FunctionSignature FS_ADDRESS = functionSignature(
            RequestModule.qname(FS_ADDRESS_NAME),
            "Returns the IP address of the server that received the request.",
            returns(Type.STRING, "the server IP address")
    );

    private static final String FS_REMOTE_HOSTNAME_NAME = "remote-hostname";
    public static final FunctionSignature FS_REMOTE_HOSTNAME = functionSignature(
            RequestModule.qname(FS_REMOTE_HOSTNAME_NAME),
            "Returns the fully qualified hostname of the client or last proxy that sent the request.",
            returns(Type.STRING, "the remote hostname")
    );

    private static final String FS_REMOTE_ADDRESS_NAME = "remote-address";
    public static final FunctionSignature FS_REMOTE_ADDRESS = functionSignature(
            RequestModule.qname(FS_REMOTE_ADDRESS_NAME),
            "Returns the IP address of the client or last proxy that sent the request.",
            returns(Type.STRING, "the remote IP address")
    );

    private static final String FS_REMOTE_PORT_NAME = "remote-port";
    public static final FunctionSignature FS_REMOTE_PORT = functionSignature(
            RequestModule.qname(FS_REMOTE_PORT_NAME),
            "Returns the TCP port of the client or last proxy that sent the request.",
            returns(Type.INTEGER, "the remote port number")
    );

    public ConnectionFunctions(final XQueryContext context, final FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    protected Sequence eval(final Sequence[] args, @Nonnull final RequestWrapper request)
            throws XPathException {
        if (isCalledAs(FS_ADDRESS_NAME)) {
            // Server address — use the server name to resolve, or fall back to local address
            try {
                return new StringValue(this, InetAddress.getLocalHost().getHostAddress());
            } catch (final UnknownHostException e) {
                return new StringValue(this, "127.0.0.1");
            }

        } else if (isCalledAs(FS_REMOTE_HOSTNAME_NAME)) {
            return new StringValue(this, request.getRemoteHost());

        } else if (isCalledAs(FS_REMOTE_ADDRESS_NAME)) {
            return new StringValue(this, request.getRemoteAddr());

        } else if (isCalledAs(FS_REMOTE_PORT_NAME)) {
            return new IntegerValue(this, request.getRemotePort());

        } else {
            throw new XPathException(this, "Unknown function: " + getSignature());
        }
    }
}
