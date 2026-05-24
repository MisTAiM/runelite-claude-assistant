package net.runelite.client.plugins.claudeassistant;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("claudeassistant")
public interface ClaudeAssistantConfig extends Config
{
    @ConfigSection(
        name = "API Settings",
        description = "Configure your Anthropic API connection",
        position = 0
    )
    String apiSection = "apiSection";

    @ConfigItem(
        keyName = "apiKey",
        name = "Anthropic API Key",
        description = "Your Anthropic API key (sk-ant-...)",
        section = apiSection,
        position = 0,
        secret = true
    )
    default String apiKey()
    {
        return "";
    }

    @ConfigItem(
        keyName = "model",
        name = "Model",
        description = "Claude model to use",
        section = apiSection,
        position = 1
    )
    default String model()
    {
        return "claude-sonnet-4-20250514";
    }

    @ConfigSection(
        name = "Context Settings",
        description = "Control what game data is sent to Claude",
        position = 1
    )
    String contextSection = "contextSection";

    @ConfigItem(
        keyName = "sendPlayerStats",
        name = "Send Player Stats",
        description = "Include your skill levels in each message",
        section = contextSection,
        position = 0
    )
    default boolean sendPlayerStats()
    {
        return true;
    }

    @ConfigItem(
        keyName = "sendInventory",
        name = "Send Inventory",
        description = "Include your current inventory items",
        section = contextSection,
        position = 1
    )
    default boolean sendInventory()
    {
        return true;
    }

    @ConfigItem(
        keyName = "sendEquipment",
        name = "Send Equipment",
        description = "Include your equipped gear",
        section = contextSection,
        position = 2
    )
    default boolean sendEquipment()
    {
        return true;
    }

    @ConfigItem(
        keyName = "sendLocation",
        name = "Send Location",
        description = "Include your current map location",
        section = contextSection,
        position = 3
    )
    default boolean sendLocation()
    {
        return true;
    }

    @ConfigItem(
        keyName = "maxTokens",
        name = "Max Response Tokens",
        description = "Maximum length of Claude's responses (256-2048)",
        section = apiSection,
        position = 2
    )
    default int maxTokens()
    {
        return 1024;
    }
}
