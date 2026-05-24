package net.runelite.client.plugins.claudeassistant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessage
{
    private final boolean user;
    private final String text;
}
