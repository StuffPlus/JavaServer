package src;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientGUI {
    private Client client;
    private String username;
    private JFrame frame;
    private JTextPane textPaneMessages;
    private JTextField textFieldInput;
    private JButton buttonSend;
    private JButton buttonRequestDetails; // Added button for requesting member details
    private JPanel panelInput;
    private JScrollPane scrollPane;
    private StyledDocument doc;
    private SimpleDateFormat dateFormat;
    private boolean isTyping = false;
    private JLabel typingStatusLabel;

    public ClientGUI(Client client, String username) {
        this.client = client;
        this.username = username;
        this.dateFormat = new SimpleDateFormat("HH:mm:ss");
        initializeGUI();
        welcomeUser();
    }

    private void initializeGUI() {
        try {
            // Set a modern cross-platform UI look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame(username + "'s Chat");
        textPaneMessages = new JTextPane();
        textPaneMessages.setEditable(false);
        doc = textPaneMessages.getStyledDocument();

        textFieldInput = new JTextField();
        textFieldInput.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Add padding to the input field

        buttonSend = new JButton("Send");
        JButton buttonQuit = new JButton("Quit");
        buttonRequestDetails = new JButton("Request Member Details");

        // Update button styling
        buttonSend.setFocusPainted(false);
        buttonQuit.setFocusPainted(false);
        buttonRequestDetails.setFocusPainted(false);

        typingStatusLabel = new JLabel(" ");
        typingStatusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        typingStatusLabel.setForeground(Color.DARK_GRAY);

        panelInput = new JPanel(new BorderLayout(5, 5)); // Add spacing between components
        panelInput.add(typingStatusLabel, BorderLayout.NORTH);
        panelInput.add(textFieldInput, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(buttonSend);
        panelInput.add(buttonPanel, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(buttonRequestDetails);
        topPanel.add(buttonQuit);

        scrollPane = new JScrollPane(textPaneMessages);
        frame.setLayout(new BorderLayout(5, 5)); // Add spacing between components
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(panelInput, BorderLayout.SOUTH);
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        ActionListener sendListener = e -> sendMessageFromGUI();
        buttonSend.addActionListener(sendListener);
        textFieldInput.addActionListener(sendListener);

        buttonQuit.addActionListener(e -> {
            client.sendMessage("QUIT");
            client.closeEverything();
            closeWindow();
        });

        buttonRequestDetails.addActionListener(e -> {
            client.sendMessage("REQUEST_DETAILS");
        });

        textFieldInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    isTyping = false;
                    client.sendTypingStatus(false);
                } else {
                    isTyping = true;
                    client.sendTypingStatus(true);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_ENTER) {
                    isTyping = false;
                    client.sendTypingStatus(false);
                }
            }
        });
    }

    private void welcomeUser() {
        SwingUtilities.invokeLater(() -> {
            appendToMessages("Welcome to the chat, " + username + "!\n", false);
        });
    }

    private void sendMessageFromGUI() {
        String messageToSend = textFieldInput.getText();
        if (!messageToSend.equals("")) {
            client.sendMessage(messageToSend);
            String formattedMessage = "(" + getFormattedTime() + ") You: " + messageToSend;
            appendToMessages(formattedMessage + "\n", true);
            textFieldInput.setText("");
        }
    }

    public String getFormattedTime() {
        return dateFormat.format(new Date());
    }

    public void appendToMessages(String message, boolean isSelf) {
        SwingUtilities.invokeLater(() -> {
            try {
                Style style = doc.addStyle("Style", null);
                if (isSelf) {
                    StyleConstants.setBold(style, true);
                    StyleConstants.setForeground(style, Color.BLUE);
                } else {
                    StyleConstants.setBold(style, false);
                    StyleConstants.setForeground(style, Color.BLACK);
                }
                doc.insertString(doc.getLength(), message, style);
                textPaneMessages.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    public void handleMessage(String message) {
        if (message.startsWith("TYPING")) {
            handleTypingStatus(message);
        } else if (message.startsWith("PRIVATE_MESSAGE")) {
            handlePrivateMessage(message);
        } else if (message.startsWith("PUBLIC_MESSAGE")) {
            String actualMessage = message.substring("PUBLIC_MESSAGE ".length());
            appendToMessages(actualMessage + "\n", false);
        } else {
            appendToMessages(message + "\n", false);
        }
    }

    private void handlePrivateMessage(String privateMessage) {
        String[] parts = privateMessage.split(" ", 3);
    
        if (parts.length == 3 && parts[1].equals(username)) {
            String formattedMessage = parts[2];
            int timeEndIndex = formattedMessage.indexOf(")");
            String time = formattedMessage.substring(0, timeEndIndex + 1);
            String message = formattedMessage.substring(timeEndIndex + 2);
            appendToMessages(time + " " + message + "\n", false);
        }
    }

    private void handleTypingStatus(String statusMessage) {
        String[] parts = statusMessage.split(" ");
        if (parts.length >= 3 && !parts[1].equals(username)) {
            boolean typingStatus = parts[2].equals("TRUE");
            SwingUtilities.invokeLater(() -> typingStatusLabel.setText(typingStatus ? parts[1] + " is typing..." : " "));
        }
    }

    public void closeWindow() {
        frame.dispose();
    }

    public static String showInputDialog(String message) {
        return JOptionPane.showInputDialog(null, message);
    }

    public static void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}