package com.cloud.agent.resource.consoleproxy;

import static com.cloud.utils.AutoCloseableUtil.closeAutoCloseable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleProxyAjaxHandler implements HttpHandler {
    private static final Logger s_logger = LoggerFactory.getLogger(ConsoleProxyAjaxHandler.class);

    public ConsoleProxyAjaxHandler() {
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        try {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("AjaxHandler " + t.getRequestURI());
            }

            final long startTick = System.currentTimeMillis();

            doHandle(t);

            if (s_logger.isTraceEnabled()) {
                s_logger.trace(t.getRequestURI() + " process time " + (System.currentTimeMillis() - startTick) + " ms");
            }
        } catch (final IllegalArgumentException e) {
            s_logger.warn("Exception, ", e);
            final Headers hds = t.getResponseHeaders();
            hds.set("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            t.sendResponseHeaders(400, -1);     // bad request
        } finally {
            t.close();
        }
    }

    private void doHandle(final HttpExchange t) throws IllegalArgumentException, IOException {
        final String queries = t.getRequestURI().getQuery();
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Handle AJAX request: " + queries);
        }

        final Map<String, String> queryMap = ConsoleProxyHttpHandlerHelper.getQueryMap(queries);

        final String host = queryMap.get("host");
        final String portStr = queryMap.get("port");
        final String sid = queryMap.get("sid");
        String tag = queryMap.get("tag");
        final String ticket = queryMap.get("ticket");
        final String ajaxSessionIdStr = queryMap.get("sess");
        final String eventStr = queryMap.get("event");
        final String console_url = queryMap.get("consoleurl");
        final String console_host_session = queryMap.get("sessionref");
        final String vm_locale = queryMap.get("locale");
        final String username = queryMap.get("username");
        final String password = queryMap.get("password");
        final String tokenCreationTimestampString = queryMap.get("tokenCreationTimestamp");

        // Assume expiry when no timestamp is found
        if (tokenCreationTimestampString == null) {
            s_logger.warn("tokenCreationTimestampString is null: session expired");
            sendResponse(t, "text/html", "Expired ajax client session id");
            return;
        }

        // Check timestamp
        try {
            final Long currentTimestamp = System.currentTimeMillis() / 1000;
            final long tokenCreationTimestamp = Long.parseLong(tokenCreationTimestampString);

            if ((currentTimestamp - tokenCreationTimestamp) > 3600) {
                s_logger.warn("tokenCreationTimestamp " + tokenCreationTimestamp + " - currentTimestamp " + currentTimestamp + " > 3600 seconds: session expired");
                sendResponse(t, "text/html", "Expired ajax client session id");
                return;
            }
            s_logger.debug("tokenCreationTimestamp " + tokenCreationTimestamp + " - currentTimestamp " + currentTimestamp + " <= 3600 seconds: session still valid");

        } catch (final NumberFormatException e) {
            s_logger.warn("Exception " + e + " when checking timestamp: session expired");
            sendResponse(t, "text/html", "Expired ajax client session id");
            return;
        }

        if (tag == null) {
            tag = "";
        }

        long ajaxSessionId = 0;
        int event = 0;

        final int port;

        if (host == null || portStr == null || sid == null) {
            throw new IllegalArgumentException();
        }

        try {
            port = Integer.parseInt(portStr);
        } catch (final NumberFormatException e) {
            s_logger.warn("Invalid number parameter in query string: " + portStr);
            throw new IllegalArgumentException(e);
        }

        if (ajaxSessionIdStr != null) {
            try {
                ajaxSessionId = Long.parseLong(ajaxSessionIdStr);
            } catch (final NumberFormatException e) {
                s_logger.warn("Invalid number parameter in query string: " + ajaxSessionIdStr);
                throw new IllegalArgumentException(e);
            }
        }

        if (eventStr != null) {
            try {
                event = Integer.parseInt(eventStr);
            } catch (final NumberFormatException e) {
                s_logger.warn("Invalid number parameter in query string: " + eventStr);
                throw new IllegalArgumentException(e);
            }
        }

        ConsoleProxyClient viewer = null;
        try {
            final ConsoleProxyClientParam param = new ConsoleProxyClientParam();
            param.setClientHostAddress(host);
            param.setClientHostPort(port);
            param.setClientHostPassword(sid);
            param.setClientTag(tag);
            param.setTicket(ticket);
            param.setClientTunnelUrl(console_url);
            param.setClientTunnelSession(console_host_session);
            param.setLocale(vm_locale);
            param.setUsername(username);
            param.setPassword(password);
            param.setTokenCreationTimestamp(Long.parseLong(tokenCreationTimestampString));

            viewer = ConsoleProxy.getAjaxVncViewer(param, ajaxSessionIdStr);
        } catch (final Exception e) {

            s_logger.warn("Failed to create viewer due to " + e.getMessage(), e);

            final String[] content =
                    new String[]{"<html><head></head><body>", "<div id=\"main_panel\" tabindex=\"1\">",
                            "<p>Access is denied for the console session. Please close the window and retry again</p>", "</div></body></html>"};

            final StringBuffer sb = new StringBuffer();
            for (int i = 0; i < content.length; i++) {
                sb.append(content[i]);
            }

            sendResponse(t, "text/html", sb.toString());
            return;
        }

        if (event != 0) {
            if (ajaxSessionId != 0 && ajaxSessionId == viewer.getAjaxSessionId()) {
                if (event == 7) {
                    // client send over an event bag
                    final InputStream is = t.getRequestBody();
                    handleClientEventBag(viewer, convertStreamToString(is, true));
                } else {
                    handleClientEvent(viewer, event, queryMap);
                }
                sendResponse(t, "text/html", "OK");
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Ajax request comes from a different session, id in request: " + ajaxSessionId + ", id in viewer: " + viewer.getAjaxSessionId());
                }

                sendResponse(t, "text/html", "Invalid ajax client session id");
            }
        } else {
            if (ajaxSessionId != 0 && ajaxSessionId != viewer.getAjaxSessionId()) {
                s_logger.info("Ajax request comes from a different session, id in request: " + ajaxSessionId + ", id in viewer: " + viewer.getAjaxSessionId());
                handleClientKickoff(t, viewer);
            } else if (ajaxSessionId == 0) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Ajax request indicates a fresh client start");
                }

                final String title = queryMap.get("t");
                final String guest = queryMap.get("guest");
                handleClientStart(t, viewer, title != null ? title : "", guest);
            } else {

                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("Ajax request indicates client update");
                }

                handleClientUpdate(t, viewer);
            }
        }
    }

    private void sendResponse(final HttpExchange t, final String contentType, final String response) throws IOException {
        final Headers hds = t.getResponseHeaders();
        hds.set("Content-Type", contentType);
        hds.set("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        t.sendResponseHeaders(200, response.length());
        final OutputStream os = t.getResponseBody();
        try {
            os.write(response.getBytes());
        } finally {
            os.close();
        }
    }

    private void handleClientEventBag(final ConsoleProxyClient viewer, final String requestData) {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Handle event bag, event bag: " + requestData);
        }

        int start = requestData.indexOf("=");
        if (start < 0) {
            start = 0;
        } else if (start > 0) {
            start++;
        }
        final String data = URLDecoder.decode(requestData.substring(start));
        final String[] tokens = data.split("\\|");
        if (tokens != null && tokens.length > 0) {
            int count = 0;
            try {
                count = Integer.parseInt(tokens[0]);
                int parsePos = 1;
                int type, event, x, y, code, modifiers;
                for (int i = 0; i < count; i++) {
                    type = Integer.parseInt(tokens[parsePos++]);
                    if (type == 1) {
                        // mouse event
                        event = Integer.parseInt(tokens[parsePos++]);
                        x = Integer.parseInt(tokens[parsePos++]);
                        y = Integer.parseInt(tokens[parsePos++]);
                        code = Integer.parseInt(tokens[parsePos++]);
                        modifiers = Integer.parseInt(tokens[parsePos++]);

                        final Map<String, String> queryMap = new HashMap<>();
                        queryMap.put("event", String.valueOf(event));
                        queryMap.put("x", String.valueOf(x));
                        queryMap.put("y", String.valueOf(y));
                        queryMap.put("code", String.valueOf(code));
                        queryMap.put("modifier", String.valueOf(modifiers));
                        handleClientEvent(viewer, event, queryMap);
                    } else {
                        // keyboard event
                        event = Integer.parseInt(tokens[parsePos++]);
                        code = Integer.parseInt(tokens[parsePos++]);
                        modifiers = Integer.parseInt(tokens[parsePos++]);

                        final Map<String, String> queryMap = new HashMap<>();
                        queryMap.put("event", String.valueOf(event));
                        queryMap.put("code", String.valueOf(code));
                        queryMap.put("modifier", String.valueOf(modifiers));
                        handleClientEvent(viewer, event, queryMap);
                    }
                }
            } catch (final NumberFormatException e) {
                s_logger.warn("Exception in handle client event bag: " + data + ", ", e);
            } catch (final Exception e) {
                s_logger.warn("Exception in handle client event bag: " + data + ", ", e);
            } catch (final OutOfMemoryError e) {
                s_logger.error("Unrecoverable OutOfMemory Error, exit and let it be re-launched");
                System.exit(1);
            }
        }
    }

    private static String convertStreamToString(final InputStream is, final boolean closeStreamAfterRead) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        final StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (final IOException e) {
            s_logger.warn("Exception while reading request body: ", e);
        } finally {
            if (closeStreamAfterRead) {
                closeAutoCloseable(is, "error closing stream after read");
            }
        }
        return sb.toString();
    }

    private void handleClientEvent(final ConsoleProxyClient viewer, final int event, final Map<String, String> queryMap) {
        int code = 0;
        int x = 0, y = 0;
        int modifiers = 0;

        String str;
        switch (event) {
            case 1:     // mouse move
            case 2:     // mouse down
            case 3:     // mouse up
            case 8:     // mouse double click
                str = queryMap.get("x");
                if (str != null) {
                    try {
                        x = Integer.parseInt(str);
                    } catch (final NumberFormatException e) {
                        s_logger.warn("Invalid number parameter in query string: " + str);
                        throw new IllegalArgumentException(e);
                    }
                }
                str = queryMap.get("y");
                if (str != null) {
                    try {
                        y = Integer.parseInt(str);
                    } catch (final NumberFormatException e) {
                        s_logger.warn("Invalid number parameter in query string: " + str);
                        throw new IllegalArgumentException(e);
                    }
                }

                if (event != 1) {
                    str = queryMap.get("code");
                    try {
                        code = Integer.parseInt(str);
                    } catch (final NumberFormatException e) {
                        s_logger.warn("Invalid number parameter in query string: " + str);
                        throw new IllegalArgumentException(e);
                    }

                    str = queryMap.get("modifier");
                    try {
                        modifiers = Integer.parseInt(str);
                    } catch (final NumberFormatException e) {
                        s_logger.warn("Invalid number parameter in query string: " + str);
                        throw new IllegalArgumentException(e);
                    }

                    if (s_logger.isTraceEnabled()) {
                        s_logger.trace("Handle client mouse event. event: " + event + ", x: " + x + ", y: " + y + ", button: " + code + ", modifier: " + modifiers);
                    }
                } else {
                    if (s_logger.isTraceEnabled()) {
                        s_logger.trace("Handle client mouse move event. x: " + x + ", y: " + y);
                    }
                }
                viewer.sendClientMouseEvent(InputEventType.fromEventCode(event), x, y, code, modifiers);
                break;

            case 4:     // key press
            case 5:     // key down
            case 6:     // key up
                str = queryMap.get("code");
                try {
                    code = Integer.parseInt(str);
                } catch (final NumberFormatException e) {
                    s_logger.warn("Invalid number parameter in query string: " + str);
                    throw new IllegalArgumentException(e);
                }

                str = queryMap.get("modifier");
                try {
                    modifiers = Integer.parseInt(str);
                } catch (final NumberFormatException e) {
                    s_logger.warn("Invalid number parameter in query string: " + str);
                    throw new IllegalArgumentException(e);
                }

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Handle client keyboard event. event: " + event + ", code: " + code + ", modifier: " + modifiers);
                }
                viewer.sendClientRawKeyboardEvent(InputEventType.fromEventCode(event), code, modifiers);
                break;

            default:
                break;
        }
    }

    private void handleClientKickoff(final HttpExchange t, final ConsoleProxyClient viewer) throws IOException {
        final String response = viewer.onAjaxClientKickoff();
        final Headers hds = t.getResponseHeaders();
        hds.set("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        t.sendResponseHeaders(200, response.length());
        final OutputStream os = t.getResponseBody();
        try {
            os.write(response.getBytes());
        } finally {
            os.close();
        }
    }

    private void handleClientStart(final HttpExchange t, final ConsoleProxyClient viewer, final String title, final String guest) throws IOException {
        final List<String> languages = t.getRequestHeaders().get("Accept-Language");
        final String response = viewer.onAjaxClientStart(title, languages, guest);

        final Headers hds = t.getResponseHeaders();
        hds.set("Content-Type", "text/html");
        hds.set("Cache-Control", "no-cache");
        hds.set("Cache-Control", "no-store");
        hds.set("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        t.sendResponseHeaders(200, response.length());

        final OutputStream os = t.getResponseBody();
        try {
            os.write(response.getBytes());
        } finally {
            os.close();
        }
    }

    private void handleClientUpdate(final HttpExchange t, final ConsoleProxyClient viewer) throws IOException {
        final String response = viewer.onAjaxClientUpdate();

        final Headers hds = t.getResponseHeaders();
        hds.set("Content-Type", "text/javascript");
        hds.set("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        t.sendResponseHeaders(200, response.length());

        final OutputStream os = t.getResponseBody();
        try {
            os.write(response.getBytes());
        } finally {
            os.close();
        }
    }
}
