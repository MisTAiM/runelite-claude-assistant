package net.runelite.client.plugins.claudeassistant;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ClaudeAssistantPanel extends PluginPanel
{
    private static final Color BG_DARK       = ColorScheme.DARKER_GRAY_COLOR;
    private static final Color BG_PANEL      = ColorScheme.DARK_GRAY_COLOR;
    private static final Color BG_INPUT      = ColorScheme.MEDIUM_GRAY_COLOR;
    private static final Color MSG_USER_BG   = new Color(50, 60, 80);
    private static final Color MSG_CLAUDE_BG = new Color(45, 45, 45);
    private static final Color ACCENT        = new Color(180, 30, 30);
    private static final Color TEXT_PRIMARY  = ColorScheme.TEXT_COLOR;
    private static final Color TEXT_MUTED    = ColorScheme.LIGHT_GRAY_COLOR;
    private static final Color TEXT_CLAUDE   = new Color(200, 200, 255);
    private static final Color SEND_BTN      = new Color(160, 25, 25);
    private static final Color SEND_BTN_HOV  = new Color(200, 35, 35);

    private static final Font FONT_TITLE  = new Font("Dialog", Font.BOLD, 13);
    private static final Font FONT_BOLD   = new Font("Dialog", Font.BOLD, 11);
    private static final Font FONT_NORMAL = new Font("Dialog", Font.PLAIN, 11);
    private static final Font FONT_SMALL  = new Font("Dialog", Font.PLAIN, 10);

    private final JPanel chatContainer;
    private final JScrollPane scrollPane;
    private final JTextArea inputField;
    private final JButton sendButton;
    private final JLabel statusLabel;

    private final List<ChatMessage> history = new ArrayList<>();
    private BiConsumer<String, List<ChatMessage>> onSend;
    private boolean isLoading = false;
    private JPanel typingIndicator;

    public ClaudeAssistantPanel()
    {
        super(false);
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_DARK);

        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel title = new JLabel("Claude Assistant");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(TEXT_MUTED);

        header.add(title, BorderLayout.WEST);
        header.add(statusLabel, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(ACCENT);
        sep.setBackground(ACCENT);

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setBackground(BG_DARK);
        headerWrapper.add(header, BorderLayout.CENTER);
        headerWrapper.add(sep, BorderLayout.SOUTH);

        // ── Chat area ───────────────────────────────────────────────────────
        chatContainer = new JPanel();
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setBackground(BG_DARK);
        chatContainer.setBorder(new EmptyBorder(8, 6, 8, 6));

        addSystemMessage("Ask me anything about OSRS! I can see your game state to give personalized advice.");

        scrollPane = new JScrollPane(chatContainer);
        scrollPane.setBackground(BG_DARK);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // ── Input area ──────────────────────────────────────────────────────
        JPanel inputPanel = new JPanel(new BorderLayout(6, 0));
        inputPanel.setBackground(BG_PANEL);
        inputPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        inputField = new JTextArea(3, 20);
        inputField.setBackground(BG_INPUT);
        inputField.setForeground(TEXT_PRIMARY);
        inputField.setCaretColor(TEXT_PRIMARY);
        inputField.setFont(FONT_NORMAL);
        inputField.setLineWrap(true);
        inputField.setWrapStyleWord(true);
        inputField.setBorder(new EmptyBorder(6, 8, 6, 8));

        inputField.setText("Ask a question...");
        inputField.setForeground(TEXT_MUTED);
        inputField.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                if (inputField.getText().equals("Ask a question..."))
                {
                    inputField.setText("");
                    inputField.setForeground(TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                if (inputField.getText().isEmpty())
                {
                    inputField.setText("Ask a question...");
                    inputField.setForeground(TEXT_MUTED);
                }
            }
        });

        inputField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown())
                {
                    e.consume();
                    handleSend();
                }
            }
        });

        JScrollPane inputScroll = new JScrollPane(inputField);
        inputScroll.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
        inputScroll.setBackground(BG_INPUT);

        sendButton = new JButton("Send");
        sendButton.setBackground(SEND_BTN);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(FONT_BOLD);
        sendButton.setBorder(new EmptyBorder(8, 14, 8, 14));
        sendButton.setFocusPainted(false);
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                if (sendButton.isEnabled()) sendButton.setBackground(SEND_BTN_HOV);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                sendButton.setBackground(SEND_BTN);
            }
        });
        sendButton.addActionListener(e -> handleSend());

        JButton clearButton = new JButton("Clear");
        clearButton.setBackground(new Color(60, 60, 60));
        clearButton.setForeground(TEXT_MUTED);
        clearButton.setFont(FONT_SMALL);
        clearButton.setBorder(new EmptyBorder(4, 8, 4, 8));
        clearButton.setFocusPainted(false);
        clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearButton.addActionListener(e -> clearChat());

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        buttonPanel.setBackground(BG_PANEL);
        buttonPanel.add(sendButton);
        buttonPanel.add(clearButton);

        inputPanel.add(inputScroll, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        JLabel hint = new JLabel("Enter to send  \u2022  Shift+Enter for newline");
        hint.setFont(FONT_SMALL);
        hint.setForeground(TEXT_MUTED);
        hint.setBorder(new EmptyBorder(2, 8, 4, 8));
        hint.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BG_PANEL);
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(hint, BorderLayout.SOUTH);

        add(headerWrapper, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void setOnSend(BiConsumer<String, List<ChatMessage>> handler)
    {
        this.onSend = handler;
    }

    private void handleSend()
    {
        if (isLoading) return;
        String text = inputField.getText().trim();
        if (text.isEmpty() || text.equals("Ask a question...")) return;

        inputField.setText("");
        inputField.setForeground(TEXT_PRIMARY);

        addUserMessage(text);
        history.add(new ChatMessage(true, text));
        setLoading(true);

        if (onSend != null)
        {
            onSend.accept(text, new ArrayList<>(history));
        }
    }

    public void onResponse(String response)
    {
        SwingUtilities.invokeLater(() ->
        {
            removeTypingIndicator();
            addClaudeMessage(response);
            history.add(new ChatMessage(false, response));
            setLoading(false);
        });
    }

    public void onError(String error)
    {
        SwingUtilities.invokeLater(() ->
        {
            removeTypingIndicator();
            addErrorMessage(error);
            setLoading(false);
        });
    }

    private void setLoading(boolean loading)
    {
        isLoading = loading;
        sendButton.setEnabled(!loading);
        inputField.setEnabled(!loading);
        statusLabel.setText(loading ? "Thinking..." : "Ready");
        statusLabel.setForeground(loading ? new Color(200, 180, 80) : TEXT_MUTED);
        if (loading) addTypingIndicator();
    }

    private void addTypingIndicator()
    {
        typingIndicator = createBubble("Claude is thinking...", MSG_CLAUDE_BG, TEXT_MUTED, "Claude");
        chatContainer.add(typingIndicator);
        chatContainer.add(Box.createVerticalStrut(6));
        chatContainer.revalidate();
        scrollToBottom();
    }

    private void removeTypingIndicator()
    {
        if (typingIndicator != null)
        {
            int idx = getComponentIndex(typingIndicator);
            chatContainer.remove(typingIndicator);
            if (idx >= 0 && idx < chatContainer.getComponentCount())
            {
                Component strut = chatContainer.getComponent(idx);
                if (strut instanceof Box.Filler) chatContainer.remove(strut);
            }
            typingIndicator = null;
        }
    }

    private int getComponentIndex(Component c)
    {
        for (int i = 0; i < chatContainer.getComponentCount(); i++)
        {
            if (chatContainer.getComponent(i) == c) return i;
        }
        return -1;
    }

    private void addUserMessage(String text)
    {
        SwingUtilities.invokeLater(() ->
        {
            chatContainer.add(createBubble(text, MSG_USER_BG, TEXT_PRIMARY, "You"));
            chatContainer.add(Box.createVerticalStrut(6));
            chatContainer.revalidate();
            scrollToBottom();
        });
    }

    private void addClaudeMessage(String text)
    {
        chatContainer.add(createBubble(text, MSG_CLAUDE_BG, TEXT_CLAUDE, "Claude"));
        chatContainer.add(Box.createVerticalStrut(6));
        chatContainer.revalidate();
        scrollToBottom();
    }

    private void addSystemMessage(String text)
    {
        chatContainer.add(createBubble(text, new Color(35, 35, 35), TEXT_MUTED, null));
        chatContainer.add(Box.createVerticalStrut(6));
        chatContainer.revalidate();
    }

    private void addErrorMessage(String text)
    {
        chatContainer.add(createBubble("\u26a0 " + text, new Color(70, 30, 30), new Color(255, 140, 140), "Error"));
        chatContainer.add(Box.createVerticalStrut(6));
        chatContainer.revalidate();
        scrollToBottom();
    }

    private JPanel createBubble(String text, Color bg, Color fg, String sender)
    {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(BG_DARK);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel bubble = new JPanel(new BorderLayout(0, 3));
        bubble.setBackground(bg);
        bubble.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.brighter(), 1),
            new EmptyBorder(7, 10, 7, 10)
        ));

        if (sender != null)
        {
            JLabel senderLabel = new JLabel(sender);
            senderLabel.setFont(FONT_BOLD);
            senderLabel.setForeground(
                "Claude".equals(sender) ? new Color(150, 150, 220)
                : "You".equals(sender)  ? new Color(180, 180, 180)
                : TEXT_MUTED
            );
            bubble.add(senderLabel, BorderLayout.NORTH);
        }

        JTextArea msgText = new JTextArea(text);
        msgText.setBackground(bg);
        msgText.setForeground(fg);
        msgText.setFont(FONT_NORMAL);
        msgText.setLineWrap(true);
        msgText.setWrapStyleWord(true);
        msgText.setEditable(false);
        msgText.setOpaque(true);
        msgText.setBorder(null);
        msgText.setFocusable(false);
        bubble.add(msgText, BorderLayout.CENTER);

        wrapper.add(bubble);
        return wrapper;
    }

    private void clearChat()
    {
        history.clear();
        chatContainer.removeAll();
        addSystemMessage("Chat cleared. Ask me anything about OSRS!");
        chatContainer.revalidate();
        chatContainer.repaint();
    }

    private void scrollToBottom()
    {
        SwingUtilities.invokeLater(() ->
        {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }
}
