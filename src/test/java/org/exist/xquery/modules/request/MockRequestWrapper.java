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

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.Cookie;
import org.exist.http.servlets.RequestWrapper;
import org.exist.http.servlets.SessionWrapper;

import java.io.InputStream;
import java.nio.file.Path;
import java.security.Principal;
import java.util.*;

/**
 * Mock implementation of {@link RequestWrapper} for unit testing the Request module
 * without a real HTTP server.
 */
public class MockRequestWrapper implements RequestWrapper {

    private String method = "GET";
    private String scheme = "http";
    private String serverName = "localhost";
    private int serverPort = 8080;
    private String requestURI = "/rest/db";
    private String contextPath = "";
    private String queryString = null;
    private String remoteAddr = "127.0.0.1";
    private String remoteHost = "localhost";
    private int remotePort = 54321;

    private final Map<String, String[]> parameters = new LinkedHashMap<>();
    private final Map<String, List<String>> headers = new LinkedHashMap<>();
    private final List<Cookie> cookies = new ArrayList<>();
    private final Map<String, Object> attributes = new LinkedHashMap<>();

    // ===== Builder methods =====

    public MockRequestWrapper withMethod(final String method) {
        this.method = method;
        return this;
    }

    public MockRequestWrapper withScheme(final String scheme) {
        this.scheme = scheme;
        return this;
    }

    public MockRequestWrapper withServerName(final String serverName) {
        this.serverName = serverName;
        return this;
    }

    public MockRequestWrapper withServerPort(final int serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    public MockRequestWrapper withRequestURI(final String requestURI) {
        this.requestURI = requestURI;
        return this;
    }

    public MockRequestWrapper withContextPath(final String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    public MockRequestWrapper withQueryString(final String queryString) {
        this.queryString = queryString;
        return this;
    }

    public MockRequestWrapper withRemoteAddr(final String remoteAddr) {
        this.remoteAddr = remoteAddr;
        return this;
    }

    public MockRequestWrapper withRemoteHost(final String remoteHost) {
        this.remoteHost = remoteHost;
        return this;
    }

    public MockRequestWrapper withRemotePort(final int remotePort) {
        this.remotePort = remotePort;
        return this;
    }

    public MockRequestWrapper withParameter(final String name, final String... values) {
        parameters.put(name, values);
        return this;
    }

    public MockRequestWrapper withHeader(final String name, final String... values) {
        headers.put(name, Arrays.asList(values));
        return this;
    }

    public MockRequestWrapper withCookie(final String name, final String value) {
        cookies.add(new Cookie(name, value));
        return this;
    }

    // ===== RequestWrapper implementation =====

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }

    @Override
    public String getRemoteHost() {
        return remoteHost;
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String getParameter(final String name) {
        final String[] values = parameters.get(name);
        return (values != null && values.length > 0) ? values[0] : null;
    }

    @Override
    public String[] getParameterValues(final String name) {
        return parameters.get(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    @Override
    public String getHeader(final String name) {
        final List<String> values = headers.get(name);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    @Override
    public Enumeration<String> getHeaders(final String name) {
        final List<String> values = headers.get(name);
        return Collections.enumeration(values != null ? values : Collections.emptyList());
    }

    @Override
    public Cookie[] getCookies() {
        return cookies.isEmpty() ? null : cookies.toArray(new Cookie[0]);
    }

    @Override
    public Object getAttribute(final String name) {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(final String name, final Object o) {
        attributes.put(name, o);
    }

    @Override
    public void removeAttribute(final String name) {
        attributes.remove(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    // ===== Unimplemented methods (not needed for request module tests) =====

    @Override
    public String getCharacterEncoding() { return "UTF-8"; }

    @Override
    public void setCharacterEncoding(final String env) { }

    @Override
    public long getContentLength() { return -1; }

    @Override
    public long getContentLengthLong() { return -1; }

    @Override
    public InputStream getInputStream() { return InputStream.nullInputStream(); }

    @Override
    public String getContentType() { return null; }

    @Override
    public String getProtocol() { return "HTTP/1.1"; }

    @Override
    public boolean isSecure() { return "https".equals(scheme); }

    @Override
    public RequestDispatcher getRequestDispatcher(final String path) { return null; }

    @Override
    public String getPathInfo() { return null; }

    @Override
    public String getPathTranslated() { return null; }

    @Override
    public String getRemoteUser() { return null; }

    @Override
    public String getRequestedSessionId() { return null; }

    @Override
    public boolean isRequestedSessionIdFromCookie() { return false; }

    @Override
    public boolean isRequestedSessionIdFromURL() { return false; }

    @Override
    public boolean isRequestedSessionIdValid() { return false; }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(scheme + "://" + serverName + ":" + serverPort + requestURI);
    }

    @Override
    public String getServletPath() { return ""; }

    @Override
    public SessionWrapper getSession() { return null; }

    @Override
    public SessionWrapper getSession(final boolean create) { return null; }

    @Override
    public Principal getUserPrincipal() { return null; }

    @Override
    public boolean isUserInRole(final String role) { return false; }

    @Override
    public boolean isMultipartContent() { return false; }

    @Override
    public List<Path> getFileUploadParam(final String name) { return Collections.emptyList(); }

    @Override
    public List<String> getUploadedFileName(final String name) { return Collections.emptyList(); }
}
