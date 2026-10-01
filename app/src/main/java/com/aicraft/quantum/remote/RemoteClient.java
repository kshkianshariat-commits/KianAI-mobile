package com.aicraft.quantum.remote;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public final class RemoteClient {
    public static final class Response {
        public final int status;
        public final String body;
        Response(int status, String body) { this.status = status; this.body = body; }
    }

    private final String baseUrl;
    private final String token;

    public RemoteClient(String host, String port, String token) {
        String h = host.trim();
        if (!h.startsWith("http://") && !h.startsWith("https://")) h = "http://" + h;
        if ((h.startsWith("http://") && h.substring(7).indexOf(':') < 0)
                || (h.startsWith("https://") && h.substring(8).indexOf(':') < 0)) {
            h = h + ":" + (port == null || port.isBlank() ? "4772" : port.trim());
        }
        while (h.endsWith("/")) h = h.substring(0, h.length() - 1);
        this.baseUrl = h;
        this.token = token == null ? "" : token.trim();
    }

    private Response request(String method, String path, String json) throws Exception {
        URL url = new URL(baseUrl + path);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod(method);
        c.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(8));
        c.setReadTimeout((int) TimeUnit.SECONDS.toMillis(20));
        c.setUseCaches(false);
        c.setDoInput(true);
        c.setRequestProperty("Accept", "application/json");
        if (!token.isEmpty()) c.setRequestProperty("Authorization", "Bearer " + token);
        if (json != null) {
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            c.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream os = c.getOutputStream()) { os.write(bytes); }
        }
        int code = c.getResponseCode();
        InputStream stream = code >= 400 ? c.getErrorStream() : c.getInputStream();
        String body = "";
        if (stream != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append('\n');
                body = sb.toString().trim();
            }
        }
        c.disconnect();
        return new Response(code, body);
    }

    public Response pair(String code) throws Exception {
        JSONObject o = new JSONObject().put("code", code == null ? "" : code.trim());
        return request("POST", "/api/remote/pair", o.toString());
    }

    public Response status() throws Exception { return request("GET", "/api/remote/status", null); }
    public Response health() throws Exception { return request("GET", "/health", null); }
    public Response shutdown() throws Exception { return request("POST", "/api/remote/shutdown", "{}"); }

    public Response chat(String message, String model) throws Exception {
        JSONObject o = new JSONObject().put("message", message);
        if (model != null && !model.isBlank()) o.put("model", model);
        return request("POST", "/api/remote/chat", o.toString());
    }

    public Response models() throws Exception { return request("GET", "/api/remote/models", null); }

    public Response downloadModel(String modelId) throws Exception {
        return request("POST", "/api/remote/models/download",
                new JSONObject().put("id", modelId).toString());
    }

    public static JSONObject parseObject(Response r) throws Exception {
        if (r.body == null || r.body.isBlank()) return new JSONObject();
        return new JSONObject(r.body);
    }

    public static JSONArray parseArray(Response r) throws Exception {
        if (r.body == null || r.body.isBlank()) return new JSONArray();
        return new JSONArray(r.body);
    }
}
