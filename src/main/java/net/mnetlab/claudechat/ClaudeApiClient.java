package net.mnetlab.claudechat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ClaudeApiClient {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION = "2023-06-01";

    private final ClaudeChatPlugin plugin;
    private final HttpClient httpClient;

    public ClaudeApiClient(ClaudeChatPlugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public CompletableFuture<ApiResult> ask(String playerName, String question) {
        String apiKey = plugin.getConfig().getString("api-key", "");

        if (apiKey.isBlank() || apiKey.equals("WSTAW-TUTAJ-SWOJ-KLUCZ-API")) {
            return CompletableFuture.completedFuture(ApiResult.missingKey());
        }

        String model = plugin.getConfig().getString("model", "claude-haiku-4-5-20251001");
        String serverName = plugin.getConfig().getString("server-name", "Minecraft Server");
        String serverDescription = plugin.getConfig().getString("server-description", "Serwer Minecraft");
        String extra = plugin.getConfig().getString("system-prompt-extra", "");

        String systemPrompt = buildSystemPrompt(playerName, serverName, serverDescription, extra);

        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.addProperty("max_tokens", 1024);
        body.addProperty("system", systemPrompt);

        JsonArray messages = new JsonArray();
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", question);
        messages.add(userMsg);
        body.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("x-api-key", apiKey)
                .header("anthropic-version", API_VERSION)
                .header("content-type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        plugin.getLogger().warning("Błąd API Claude: HTTP " + response.statusCode() + " — " + response.body());
                        return ApiResult.apiError(response.statusCode());
                    }
                    JsonObject parsed = JsonParser.parseString(response.body()).getAsJsonObject();
                    String text = parsed.getAsJsonArray("content")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();
                    return ApiResult.success(text);
                })
                .exceptionally(ex -> {
                    plugin.getLogger().severe("Błąd połączenia z Claude API: " + ex.getMessage());
                    return ApiResult.connectionError();
                });
    }

    private String buildSystemPrompt(String playerName, String serverName, String serverDescription, String extra) {
        StringBuilder sb = new StringBuilder();
        sb.append("Jesteś pomocnym asystentem AI osadzonym na serwerze Minecraft.\n");
        sb.append("Nazwa serwera: ").append(serverName).append("\n");
        sb.append("Opis serwera: ").append(serverDescription).append("\n");
        sb.append("Wersja: Paper 1.21.1\n");
        sb.append("Gracz który pisze do Ciebie: ").append(playerName).append("\n");
        sb.append("Odpowiadaj zwięźle — twoje odpowiedzi wyświetlają się w czacie Minecraft. ");
        sb.append("Unikaj formatowania Markdown (nagłówki, pogrubienie itp.), pisz zwykłym tekstem.");
        if (!extra.isBlank()) {
            sb.append("\n").append(extra);
        }
        return sb.toString();
    }

    // Sealed result type — unika rzucania wyjątków przez granicę async
    public static final class ApiResult {
        public enum Type { SUCCESS, MISSING_KEY, API_ERROR, CONNECTION_ERROR }

        public final Type type;
        public final String text;
        public final int httpCode;

        private ApiResult(Type type, String text, int httpCode) {
            this.type = type;
            this.text = text;
            this.httpCode = httpCode;
        }

        public static ApiResult success(String text) {
            return new ApiResult(Type.SUCCESS, text, 200);
        }

        public static ApiResult missingKey() {
            return new ApiResult(Type.MISSING_KEY, null, 0);
        }

        public static ApiResult apiError(int code) {
            return new ApiResult(Type.API_ERROR, null, code);
        }

        public static ApiResult connectionError() {
            return new ApiResult(Type.CONNECTION_ERROR, null, 0);
        }
    }
}
