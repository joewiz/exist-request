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

import org.exist.test.ExistEmbeddedServer;
import org.junit.ClassRule;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Comprehensive tests for all 28 functions in the EXQuery Request Module.
 *
 * Uses a single {@link ExistEmbeddedServer} instance with BrokerPool-based
 * execution and {@link MockRequestWrapper} for mock HTTP request injection,
 * avoiding BrokerPool singleton conflicts.
 */
public class RequestModuleTest {

    private static final Path CONFIG =
            Paths.get(System.getProperty("user.dir"), "src/test/resources/conf.xml");

    @ClassRule
    public static final ExistEmbeddedServer existEmbeddedServer =
            new ExistEmbeddedServer(null, CONFIG, null, true, true);

    // ========================================================================
    // Helper methods
    // ========================================================================

    private static String query(final String xquery) throws Exception {
        return RequestTestUtil.executeWithDefaults(existEmbeddedServer, xquery);
    }

    private static String query(final String xquery, final MockRequestWrapper request)
            throws Exception {
        return RequestTestUtil.executeWithRequest(existEmbeddedServer, xquery, request);
    }

    // ========================================================================
    // 1. General: exrequest:method(), exrequest:context-path()
    // ========================================================================

    @Test
    public void methodReturnsGet() throws Exception {
        assertEquals("GET", query("exrequest:method()"));
    }

    @Test
    public void methodReturnsPost() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withMethod("POST");
        assertEquals("POST", query("exrequest:method()", request));
    }

    @Test
    public void methodReturnsPut() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withMethod("PUT");
        assertEquals("PUT", query("exrequest:method()", request));
    }

    @Test
    public void methodReturnsDelete() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withMethod("DELETE");
        assertEquals("DELETE", query("exrequest:method()", request));
    }

    @Test
    public void contextPathEmpty() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withContextPath("");
        assertEquals("", query("exrequest:context-path()", request));
    }

    @Test
    public void contextPathWithValue() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withContextPath("/exist");
        assertEquals("/exist", query("exrequest:context-path()", request));
    }

    // ========================================================================
    // 2. URI: exrequest:scheme(), exrequest:hostname(), exrequest:port(),
    //         exrequest:path(), exrequest:query(), exrequest:uri()
    // ========================================================================

    @Test
    public void schemeReturnsHttp() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withScheme("http");
        assertEquals("http", query("exrequest:scheme()", request));
    }

    @Test
    public void schemeReturnsHttps() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withScheme("https");
        assertEquals("https", query("exrequest:scheme()", request));
    }

    @Test
    public void hostnameReturnsLocalhost() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withServerName("localhost");
        assertEquals("localhost", query("exrequest:hostname()", request));
    }

    @Test
    public void hostnameReturnsCustom() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withServerName("example.com");
        assertEquals("example.com", query("exrequest:hostname()", request));
    }

    @Test
    public void portReturnsCustom() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withServerPort(9090);
        assertEquals("9090", query("exrequest:port()", request));
    }

    @Test
    public void portReturnsStandard() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withServerPort(80);
        assertEquals("80", query("exrequest:port()", request));
    }

    @Test
    public void pathReturnsRequestUri() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withRequestURI("/app/api/data");
        assertEquals("/app/api/data", query("exrequest:path()", request));
    }

    @Test
    public void queryReturnsQueryString() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withQueryString("foo=bar&baz=qux");
        assertEquals("foo=bar&baz=qux", query("exrequest:query()", request));
    }

    @Test
    public void queryReturnsEmptyWhenAbsent() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withQueryString(null);
        assertEquals("", query(
                "let $q := exrequest:query()\n" +
                "return if (empty($q)) then '' else $q", request));
    }

    @Test
    public void uriReturnsFullUri() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withRequestURI("/rest/db")
                .withQueryString("x=1");
        final String result = query("exrequest:uri()", request);
        assertTrue("uri() should contain /rest/db, got: " + result,
                result.contains("/rest/db"));
    }

    // ========================================================================
    // 3. Connection: exrequest:address(), exrequest:remote-hostname(),
    //                exrequest:remote-address(), exrequest:remote-port()
    // ========================================================================

    @Test
    public void addressReturnsServerAddress() throws Exception {
        final String result = query("exrequest:address()");
        assertFalse("address() should not be empty", result.isEmpty());
    }

    @Test
    public void remoteHostnameReturnsValue() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withRemoteHost("localhost");
        assertEquals("localhost", query("exrequest:remote-hostname()", request));
    }

    @Test
    public void remoteAddressReturnsCustom() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withRemoteAddr("192.168.1.42");
        assertEquals("192.168.1.42", query("exrequest:remote-address()", request));
    }

    @Test
    public void remoteAddressReturnsDefault() throws Exception {
        // Default MockRequestWrapper has 127.0.0.1
        assertEquals("127.0.0.1", query("exrequest:remote-address()"));
    }

    @Test
    public void remotePortReturnsValue() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper().withRemotePort(54321);
        assertEquals("54321", query("exrequest:remote-port()", request));
    }

    // ========================================================================
    // 4. Parameters: exrequest:parameter-names(), exrequest:parameter(),
    //                exrequest:parameter($name, $default), exrequest:parameter-map()
    // ========================================================================

    @Test
    public void parameterNamesWithParams() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withParameter("color", "blue")
                .withParameter("size", "large");
        final String result = query(
                "string-join(exrequest:parameter-names(), ',')", request);
        assertTrue("Should contain color, got: " + result, result.contains("color"));
        assertTrue("Should contain size, got: " + result, result.contains("size"));
    }

    @Test
    public void parameterNamesEmpty() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper();
        assertEquals("0", query("count(exrequest:parameter-names())", request));
    }

    @Test
    public void parameterReturnsSingleValue() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withParameter("color", "blue");
        assertEquals("blue", query("exrequest:parameter('color')", request));
    }

    @Test
    public void parameterReturnsMissingAsEmpty() throws Exception {
        assertEquals("empty", query(
                "let $p := exrequest:parameter('nonexistent')\n" +
                "return if (empty($p)) then 'empty' else 'found'"));
    }

    @Test
    public void parameterReturnsMultiValue() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withParameter("item", "a", "b", "c");
        assertEquals("3", query("count(exrequest:parameter('item'))", request));
    }

    @Test
    public void parameterMultiValueOrder() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withParameter("item", "first", "second", "third");
        assertEquals("first second third", query(
                "string-join(exrequest:parameter('item'), ' ')", request));
    }

    @Test
    public void parameterWithDefaultReturnsPresent() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withParameter("color", "blue");
        assertEquals("blue", query("exrequest:parameter('color', 'red')", request));
    }

    @Test
    public void parameterWithDefaultReturnsMissing() throws Exception {
        assertEquals("fallback", query("exrequest:parameter('nonexistent', 'fallback')"));
    }

    @Test
    public void parameterMapBasic() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withParameter("color", "green");
        assertEquals("green", query(
                "let $m := exrequest:parameter-map()\n" +
                "return $m?color", request));
    }

    @Test
    public void parameterMapTypeCheck() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withParameter("x", "1");
        assertEquals("true", query(
                "exrequest:parameter-map() instance of map(*)", request));
    }

    @Test
    public void parameterMapAllParams() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withParameter("x", "1")
                .withParameter("y", "2");
        assertEquals("true", query(
                "let $m := exrequest:parameter-map()\n" +
                "return exists($m) and $m instance of map(*)", request));
    }

    @Test
    public void parameterMapMultiValued() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withParameter("item", "a", "b");
        assertEquals("2", query(
                "let $m := exrequest:parameter-map()\n" +
                "return count($m?item)", request));
    }

    // ========================================================================
    // 5. Headers: exrequest:header-names(), exrequest:header(),
    //             exrequest:header($name, $default), exrequest:header-map()
    // ========================================================================

    @Test
    public void headerNamesWithHeaders() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withHeader("Host", "localhost")
                .withHeader("Accept", "text/html");
        final String result = query(
                "string-join(exrequest:header-names(), ',')", request);
        assertTrue("Should contain Host, got: " + result, result.contains("Host"));
        assertTrue("Should contain Accept, got: " + result, result.contains("Accept"));
    }

    @Test
    public void headerNamesEmpty() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper();
        assertEquals("0", query("count(exrequest:header-names())", request));
    }

    @Test
    public void headerReturnsValue() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withHeader("Host", "localhost:8080");
        assertEquals("localhost:8080", query("exrequest:header('Host')", request));
    }

    @Test
    public void headerReturnsMissing() throws Exception {
        assertEquals("empty", query(
                "let $h := exrequest:header('X-Nonexistent')\n" +
                "return if (empty($h)) then 'empty' else 'found'"));
    }

    @Test
    public void headerReturnsCustom() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withHeader("X-Custom-Test", "hello-world");
        assertEquals("hello-world", query("exrequest:header('X-Custom-Test')", request));
    }

    @Test
    public void headerWithDefaultReturnsPresent() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withHeader("X-Custom", "actual");
        assertEquals("actual", query(
                "exrequest:header('X-Custom', 'fallback')", request));
    }

    @Test
    public void headerWithDefaultReturnsMissing() throws Exception {
        assertEquals("default-val", query(
                "exrequest:header('X-Missing', 'default-val')"));
    }

    @Test
    public void headerMapBasic() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withHeader("X-Map-Test", "map-value");
        assertEquals("map-value", query(
                "let $m := exrequest:header-map()\n" +
                "return $m?('X-Map-Test')", request));
    }

    @Test
    public void headerMapAllHeaders() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withHeader("Host", "localhost")
                .withHeader("Accept", "text/html");
        assertEquals("true", query(
                "let $m := exrequest:header-map()\n" +
                "return exists($m) and $m instance of map(*)", request));
    }

    @Test
    public void headerMapTypeCheck() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withHeader("Host", "localhost");
        assertEquals("true", query(
                "exrequest:header-map() instance of map(*)", request));
    }

    // ========================================================================
    // 6. Cookies: exrequest:cookie(), exrequest:cookie($name, $default),
    //             exrequest:cookie-names(), exrequest:cookie-map()
    // ========================================================================

    @Test
    public void cookieReturnsValue() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withCookie("session", "abc123");
        assertEquals("abc123", query("exrequest:cookie('session')", request));
    }

    @Test
    public void cookieReturnsMissing() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withCookie("other", "val");
        assertEquals("empty", query(
                "let $c := exrequest:cookie('nonexistent')\n" +
                "return if (empty($c)) then 'empty' else 'found'", request));
    }

    @Test
    public void cookieReturnsMissingNoCookies() throws Exception {
        // No cookies set at all (getCookies() returns null)
        assertEquals("empty", query(
                "let $c := exrequest:cookie('anything')\n" +
                "return if (empty($c)) then 'empty' else 'found'"));
    }

    @Test
    public void cookieWithDefaultReturnsPresent() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withCookie("token", "xyz789");
        assertEquals("xyz789", query(
                "exrequest:cookie('token', 'none')", request));
    }

    @Test
    public void cookieWithDefaultReturnsMissing() throws Exception {
        assertEquals("default-cookie", query(
                "exrequest:cookie('missing', 'default-cookie')"));
    }

    @Test
    public void cookieNamesWithCookies() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withCookie("a", "1")
                .withCookie("b", "2")
                .withCookie("c", "3");
        final String result = query(
                "string-join(exrequest:cookie-names(), ',')", request);
        assertTrue("Should contain a, got: " + result, result.contains("a"));
        assertTrue("Should contain b, got: " + result, result.contains("b"));
        assertTrue("Should contain c, got: " + result, result.contains("c"));
    }

    @Test
    public void cookieNamesEmpty() throws Exception {
        assertEquals("0", query("count(exrequest:cookie-names())"));
    }

    @Test
    public void cookieMapBasic() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withCookie("flavor", "chocolate");
        assertEquals("chocolate", query(
                "let $m := exrequest:cookie-map()\n" +
                "return $m?flavor", request));
    }

    @Test
    public void cookieMapAllCookies() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withCookie("x", "1")
                .withCookie("y", "2");
        assertEquals("true", query(
                "let $m := exrequest:cookie-map()\n" +
                "return exists($m?x) and exists($m?y)", request));
    }

    @Test
    public void cookieMapTypeCheck() throws Exception {
        final MockRequestWrapper request = new MockRequestWrapper()
                .withCookie("k", "v");
        assertEquals("true", query(
                "exrequest:cookie-map() instance of map(*)", request));
    }

    // ========================================================================
    // 7. Attributes: exrequest:set-attribute(), exrequest:attribute(),
    //                exrequest:attribute($name, $default),
    //                exrequest:attribute-names(), exrequest:attribute-map()
    // ========================================================================

    @Test
    public void setAttributeThenGetRoundtrip() throws Exception {
        assertEquals("test-value", query(
                "let $_ := exrequest:set-attribute('test-key', 'test-value')\n" +
                "return exrequest:attribute('test-key')"));
    }

    @Test
    public void setAttributeReturnsEmpty() throws Exception {
        assertEquals("empty", query(
                "let $result := exrequest:set-attribute('key', 'val')\n" +
                "return if (empty($result)) then 'empty' else 'not-empty'"));
    }

    @Test
    public void attributeGetMissing() throws Exception {
        assertEquals("empty", query(
                "let $a := exrequest:attribute('nonexistent-attr')\n" +
                "return if (empty($a)) then 'empty' else 'found'"));
    }

    @Test
    public void attributeWithDefaultReturnsPresent() throws Exception {
        assertEquals("myvalue", query(
                "let $_ := exrequest:set-attribute('myattr', 'myvalue')\n" +
                "return exrequest:attribute('myattr', 'fallback')"));
    }

    @Test
    public void attributeWithDefaultReturnsMissing() throws Exception {
        assertEquals("fallback", query(
                "exrequest:attribute('missing-attr', 'fallback')"));
    }

    @Test
    public void attributeNamesIncludesSet() throws Exception {
        assertEquals("true", query(
                "let $_ := exrequest:set-attribute('my-named-attr', 'val')\n" +
                "return exrequest:attribute-names() = 'my-named-attr'"));
    }

    @Test
    public void attributeMapBasic() throws Exception {
        assertEquals("map-val", query(
                "let $_ := exrequest:set-attribute('map-test', 'map-val')\n" +
                "let $m := exrequest:attribute-map()\n" +
                "return $m?('map-test')"));
    }

    @Test
    public void attributeMapTypeCheck() throws Exception {
        assertEquals("true", query(
                "let $_ := exrequest:set-attribute('k', 'v')\n" +
                "return exrequest:attribute-map() instance of map(*)"));
    }

    @Test
    public void multipleAttributesRoundTrip() throws Exception {
        assertEquals("1,2,3", query(
                "let $_ := (exrequest:set-attribute('a', '1'),\n" +
                "           exrequest:set-attribute('b', '2'),\n" +
                "           exrequest:set-attribute('c', '3'))\n" +
                "return string-join((exrequest:attribute('a'), exrequest:attribute('b'), exrequest:attribute('c')), ',')"));
    }
}
