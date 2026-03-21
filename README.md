# EXQuery Request Module for eXist-db

A standalone XAR package implementing the [EXQuery Request Module](https://exquery.github.io/expath-specs-playground/request-module-1.0-specification.html) for eXist-db. Provides 28 functions for inspecting HTTP requests — method, URI components, parameters, headers, cookies, and attributes — compatible with BaseX's Request Module API. Uses eXist's native `XQueryContext.getHttpContext()` — no external dependencies.

This module **coexists** with eXist's built-in request module (`http://exist-db.org/xquery/request`). They use different namespaces and different default prefixes, so both are available simultaneously with no conflicts.

## Install

Download the `.xar` from CI build artifacts and install with the eXist-db Package Manager or the `xst` CLI:

```bash
xst package install exist-request-0.9.0-SNAPSHOT.xar
```

## Coexistence with the built-in request module

eXist-db ships a built-in request module at `http://exist-db.org/xquery/request` (prefix: `request`). This package uses a different namespace (`http://exquery.org/ns/request`) and a different default prefix (`exrequest`), so both modules are available at the same time:

```xquery
import module namespace request = "http://exist-db.org/xquery/request";
import module namespace exrequest = "http://exquery.org/ns/request";

(: eXist-native — some functions unique to eXist :)
request:get-parameter("id", ())

(: EXQuery standard — portable across eXist and BaseX :)
exrequest:parameter("id")
exrequest:parameter-map()
exrequest:header-map()
```

The `exrequest:*` functions follow the [EXQuery Request Module specification](https://exquery.github.io/expath-specs-playground/request-module-1.0-specification.html) — the same API implemented by BaseX. Code using `exrequest:*` is portable across both engines.

The `request:*` functions are eXist-native. Some overlap with the EXQuery API (e.g., `request:get-method()` vs `exrequest:method()`), but eXist's built-in module also has functions with no EXQuery equivalent (e.g., `request:get-uploaded-file-data()`).

### What's new in this module

This module provides 9 functions not available in eXist's built-in request module:

| Function | Description |
|----------|-------------|
| `exrequest:context-path()` | Servlet context path |
| `exrequest:parameter-map()` | All parameters as XDM map |
| `exrequest:header-map()` | All headers as XDM map |
| `exrequest:cookie-map()` | All cookies as XDM map |
| `exrequest:attribute-map()` | All attributes as XDM map |
| `exrequest:attribute($name)` | Read a request attribute |
| `exrequest:attribute($name, $default)` | Attribute with fallback |
| `exrequest:attribute-names()` | All attribute names |
| `exrequest:set-attribute($name, $value)` | Set a request attribute |

### Migration timeline

| Release | Status |
|---------|--------|
| **eXist 7.x** | Coexistence — both modules available, `request:*` is eXist-native, `exrequest:*` is EXQuery standard |
| **eXist 8.0** | Deprecation of original `request:*` module (with compatibility shim) |
| **eXist 9.0** | Prefix swap — `request:*` resolves to the EXQuery standard, original module available as `request-legacy:*` |

## Functions

| Function | Description |
|----------|-------------|
| `exrequest:method()` | HTTP request method (GET, POST, etc.) |
| `exrequest:context-path()` | Servlet context path |
| `exrequest:scheme()` | URI scheme (http or https) |
| `exrequest:hostname()` | Server hostname |
| `exrequest:port()` | Server port number |
| `exrequest:path()` | Request path |
| `exrequest:query()` | Raw query string |
| `exrequest:uri()` | Full request URI |
| `exrequest:address()` | Server address |
| `exrequest:remote-hostname()` | Client hostname |
| `exrequest:remote-address()` | Client IP address |
| `exrequest:remote-port()` | Client port number |
| `exrequest:parameter($name)` | Query/form parameter value(s) |
| `exrequest:parameter($name, $default)` | Parameter with fallback |
| `exrequest:parameter-names()` | All parameter names |
| `exrequest:parameter-map()` | All parameters as XDM map |
| `exrequest:header($name)` | HTTP header value |
| `exrequest:header($name, $default)` | Header with fallback |
| `exrequest:header-names()` | All header names |
| `exrequest:header-map()` | All headers as XDM map |
| `exrequest:cookie($name)` | Cookie value |
| `exrequest:cookie($name, $default)` | Cookie with fallback |
| `exrequest:cookie-names()` | All cookie names |
| `exrequest:cookie-map()` | All cookies as XDM map |
| `exrequest:attribute($name)` | Request attribute value |
| `exrequest:attribute($name, $default)` | Attribute with fallback |
| `exrequest:attribute-names()` | All attribute names |
| `exrequest:attribute-map()` | All attributes as XDM map |
| `exrequest:set-attribute($name, $value)` | Set a request attribute |

**Module namespace:** `http://exquery.org/ns/request`

## Examples

### Basic usage

```xquery
import module namespace exrequest = "http://exquery.org/ns/request";

<request>
    <method>{exrequest:method()}</method>
    <path>{exrequest:path()}</path>
    <host>{exrequest:hostname()}:{exrequest:port()}</host>
</request>
```

### Parameters with defaults

```xquery
import module namespace exrequest = "http://exquery.org/ns/request";

let $page := xs:integer(exrequest:parameter("page", "1"))
let $limit := xs:integer(exrequest:parameter("limit", "20"))
return
    <pagination offset="{($page - 1) * $limit}" limit="{$limit}"/>
```

### Headers and cookies

```xquery
import module namespace exrequest = "http://exquery.org/ns/request";

let $accept := exrequest:header("Accept", "*/*")
let $session := exrequest:cookie("JSESSIONID", "")
return
    <context accept="{$accept}" has-session="{$session != ''}"/>
```

### Attributes (inter-stage communication)

```xquery
import module namespace exrequest = "http://exquery.org/ns/request";

(: Set in controller.xq :)
let $_ := exrequest:set-attribute("route.action", "list")

(: Read in target XQuery :)
return exrequest:attribute("route.action")
```

## Build

```bash
JAVA_HOME=/path/to/java-21 mvn clean package -DskipTests
```

Run integration tests (requires exist-core 7.0.0-SNAPSHOT in your local Maven repo):

```bash
mvn test -Pintegration-tests
```

## License

[GNU Lesser General Public License v2.1](https://opensource.org/licenses/LGPL-2.1)
