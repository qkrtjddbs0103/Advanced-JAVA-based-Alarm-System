package smartalarm.view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PhoneFrame extends JFrame {

    private static final String CARD_ALARM = "alarm";
    private static final String CARD_FRIENDS = "friends";
    private static final String CARD_SETTINGS = "settings";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel screens = new JPanel(cardLayout);

    private final AlarmPanel alarmPanel = new AlarmPanel();
    private final FriendsPanel friendsPanel = new FriendsPanel();
    private final SettingsPanel settingsPanel = new SettingsPanel();

    public PhoneFrame(String title) {
        setTitle(title);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(420, 380);
        setResizable(false);
        setLocationRelativeTo(null);

        screens.add(alarmPanel, CARD_ALARM);
        screens.add(friendsPanel, CARD_FRIENDS);
        screens.add(settingsPanel, CARD_SETTINGS);

        add(screens, BorderLayout.CENTER);
        add(createNavBar(), BorderLayout.SOUTH);

        initTray();
    }

    private JPanel createNavBar() {
        JPanel nav = new JPanel(new GridLayout(1, 3));
        nav.setBackground(new Color(49, 50, 68));

        nav.add(createNavButton("Alarm", CARD_ALARM));
        nav.add(createNavButton("Friends", CARD_FRIENDS));
        nav.add(createNavButton("Settings", CARD_SETTINGS));

        return nav;
    }

    private JButton createNavButton(String label, String cardName) {
        JButton button = new JButton(label);
        button.setBackground(new Color(49, 50, 68));
        button.setForeground(new Color(205, 214, 244));
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> cardLayout.show(screens, cardName));
        return button;
    }

    private void initTray() {
        if (!SystemTray.isSupported()) return;

        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        PopupMenu popup = new PopupMenu();
        MenuItem showItem = new MenuItem("Open");
        MenuItem exitItem = new MenuItem("Exit");

        showItem.addActionListener(e -> showWindow());
        exitItem.addActionListener(e -> System.exit(0));

        popup.add(showItem);
        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(image, getTitle(), popup);
        trayIcon.addActionListener(e -> showWindow());

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }

    private void showWindow() {
        setVisible(true);
        setExtendedState(JFrame.NORMAL);
        toFront();
    }

    public AlarmPanel getAlarmPanel() { return alarmPanel; }
    public FriendsPanel getFriendsPanel() { return friendsPanel; }
    public SettingsPanel getSettingsPanel() { return settingsPanel; }
}
