package net.runelite.client.plugins.claudeassistant;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import okhttp3.OkHttpClient;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.List;

@Slf4j
@PluginDescriptor(
    name = "Claude Assistant",
    description = "AI-powered OSRS assistant using Claude by Anthropic",
    tags = {"claude", "ai", "assistant", "helper", "anthropic"},
    enabledByDefault = true
)
public class ClaudeAssistantPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ClaudeAssistantConfig config;

    @Inject
    private ItemManager itemManager;

    @Inject
    private OkHttpClient okHttpClient;

    private ClaudeAssistantPanel panel;
    private NavigationButton navButton;
    private ClaudeApiClient apiClient;
    private GameContextBuilder contextBuilder;

    @Override
    protected void startUp()
    {
        log.info("Claude Assistant starting up");

        apiClient = new ClaudeApiClient(okHttpClient);
        contextBuilder = new GameContextBuilder(client, itemManager);

        panel = new ClaudeAssistantPanel();
        panel.setOnSend(this::handleUserMessage);

        BufferedImage icon;
        try
        {
            icon = ImageUtil.loadImageResource(getClass(), "/com/claudeassistant/icon.png");
        }
        catch (Exception e)
        {
            // Generate a simple red "C" icon at runtime
            icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = icon.createGraphics();
            g.setColor(new java.awt.Color(180, 30, 30));
            g.fillOval(0, 0, 15, 15);
            g.setColor(java.awt.Color.WHITE);
            g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 10f));
            g.drawString("C", 4, 11);
            g.dispose();
        }

        navButton = NavigationButton.builder()
            .tooltip("Claude Assistant")
            .icon(icon)
            .priority(7)
            .panel(panel)
            .build();

        clientToolbar.addNavigation(navButton);
        log.info("Claude Assistant started");
    }

    @Override
    protected void shutDown()
    {
        log.info("Claude Assistant shutting down");
        clientToolbar.removeNavigation(navButton);
    }

    private void handleUserMessage(String message, List<ChatMessage> history)
    {
        String apiKey = config.apiKey();
        if (apiKey == null || apiKey.trim().isEmpty())
        {
            panel.onError("No API key set. Go to Plugin Hub → Claude Assistant config and enter your Anthropic API key.");
            return;
        }

        String gameContext = "";
        if (client.getGameState() == GameState.LOGGED_IN)
        {
            try
            {
                gameContext = contextBuilder.build(config);
            }
            catch (Exception ex)
            {
                log.warn("Failed to build game context", ex);
            }
        }

        final String model = config.model().isEmpty() ? "claude-sonnet-4-20250514" : config.model();
        final int maxTokens = Math.max(256, Math.min(2048, config.maxTokens()));

        apiClient.sendMessage(
            apiKey,
            model,
            maxTokens,
            gameContext,
            history,
            message,
            response -> panel.onResponse(response),
            error -> panel.onError(error)
        );
    }

    @Provides
    ClaudeAssistantConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ClaudeAssistantConfig.class);
    }
}
