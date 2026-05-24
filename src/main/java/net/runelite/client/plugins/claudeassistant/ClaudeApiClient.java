package net.runelite.client.plugins.claudeassistant;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class ClaudeApiClient
{
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final String SYSTEM_PROMPT =
        "You are Claude, an AI assistant embedded directly inside the RuneLite OSRS client. " +
        "You have access to the player's current game state which will be provided with each message. " +
        "You are an expert on Old School RuneScape — quests, skills, combat, gear progression, " +
        "money making methods, bosses, minigames, and efficient training routes. " +
        "Keep responses concise and practical since you are displayed in a small sidebar panel. " +
        "Use bullet points for lists. When referencing items or skills, be specific with numbers. " +
        "If the player's game state is relevant to their question, use it to give personalized advice.";

    private final Gson gson = new Gson();
    private final OkHttpClient httpClient;

    // Use RuneLite's injected OkHttpClient (already configured, no client-thread blocking)
    public ClaudeApiClient(OkHttpClient httpClient)
    {
        this.httpClient = httpClient;
    }

    public void sendMessage(
        String apiKey,
        String model,
        int maxTokens,
        String gameContext,
        List<ChatMessage> history,
        String userMessage,
        Consumer<String> onSuccess,
        Consumer<String> onError
    )
    {
        JsonArray messages = new JsonArray();

        int start = Math.max(0, history.size() - 20);
        for (int i = start; i < history.size(); i++)
        {
            ChatMessage msg = history.get(i);
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.isUser() ? "user" : "assistant");
            msgObj.addProperty("content", msg.getText());
            messages.add(msgObj);
        }

        String fullUserMessage = gameContext.isEmpty()
            ? userMessage
            : "=== CURRENT GAME STATE ===\n" + gameContext + "\n=== PLAYER MESSAGE ===\n" + userMessage;

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", fullUserMessage);
        messages.add(userMsg);

        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.addProperty("max_tokens", maxTokens);
        body.addProperty("system", SYSTEM_PROMPT);
        body.add("messages", messages);

        RequestBody requestBody = RequestBody.create(JSON, gson.toJson(body));

        Request request = new Request.Builder()
            .url(API_URL)
            .header("x-api-key", apiKey)
            .header("anthropic-version", ANTHROPIC_VERSION)
            .header("content-type", "application/json")
            .post(requestBody)
            .build();

        httpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                log.error("Claude API request failed", e);
                onError.accept("Connection error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                try (ResponseBody responseBody = response.body())
                {
                    if (responseBody == null)
                    {
                        onError.accept("Empty response from API");
                        return;
                    }

                    String rawBody = responseBody.string();

                    if (!response.isSuccessful())
                    {
                        log.error("Claude API error {}: {}", response.code(), rawBody);
                        try
                        {
                            JsonObject errObj = gson.fromJson(rawBody, JsonObject.class);
                            if (errObj.has("error"))
                            {
                                String errMsg = errObj.getAsJsonObject("error")
                                    .get("message").getAsString();
                                onError.accept("API Error " + response.code() + ": " + errMsg);
                            }
                            else
                            {
                                onError.accept("API Error " + response.code());
                            }
                        }
                        catch (Exception ex)
                        {
                            onError.accept("API Error " + response.code());
                        }
                        return;
                    }

                    JsonObject respObj = gson.fromJson(rawBody, JsonObject.class);
                    JsonArray content = respObj.getAsJsonArray("content");
                    if (content == null || content.size() == 0)
                    {
                        onError.accept("No content in response");
                        return;
                    }

                    String text = content.get(0).getAsJsonObject()
                        .get("text").getAsString();
                    onSuccess.accept(text);
                }
            }
        });
    }
}
