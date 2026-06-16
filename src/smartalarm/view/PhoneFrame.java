package smartalarm.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class PhoneFrame extends JFrame {

    private static final String CARD_ALARM    = "alarm";
    private static final String CARD_FRIENDS  = "friends";
    private static final String CARD_SETTINGS = "settings";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel screens = new JPanel(cardLayout);

    private final AlarmPanel    alarmPanel    = new AlarmPanel();
    private final FriendsPanel  friendsPanel  = new FriendsPanel();
    private final SettingsPanel settingsPanel = new SettingsPanel();

    private final ImageIcon navAlarmIcon    = AlarmPanel.loadHQ("assets/UI_Images/nav_alarm_active.png",    390, 62);
    private final ImageIcon navFriendsIcon  = AlarmPanel.loadHQ("assets/UI_Images/nav_friends_active.png",  390, 62);
    private final ImageIcon navSettingsIcon = AlarmPanel.loadHQ("assets/UI_Images/nav_settings_active.png", 390, 62);
    private JLabel navBar;

    public PhoneFrame(String title) {
        setTitle(title);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setResizable(false);

        JPanel content = new JPanel(null);
        content.setBackground(new Color(20, 20, 30));
        content.setPreferredSize(new Dimension(390, 720));
        setContentPane(content);

        screens.setOpaque(false);
        screens.add(alarmPanel,    CARD_ALARM);
        screens.add(friendsPanel,  CARD_FRIENDS);
        screens.add(settingsPanel, CARD_SETTINGS);
        screens.setBounds(0, 0, 390, 614);
        content.add(screens);

        navBar = buildNavBar();
        content.add(navBar);

        JLabel bgLabel = new JLabel(AlarmPanel.loadHQ("assets/UI_Images/background.png", 390, 720));
        bgLabel.setBounds(0, 0, 390, 720);
        content.add(bgLabel);
        content.setComponentZOrder(bgLabel, content.getComponentCount() - 1);

        pack();
        setLocationRelativeTo(null);
        initTray();
    }

    private JLabel buildNavBar() {
        JLabel nav = new JLabel(navAlarmIcon);
        nav.setBounds(0, 657, 390, 62);
        nav.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nav.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int third = nav.getWidth() / 3;
                if (e.getX() < third) {
                    cardLayout.show(screens, CARD_ALARM);
                    nav.setIcon(navAlarmIcon);
                } else if (e.getX() < third * 2) {
                    cardLayout.show(screens, CARD_FRIENDS);
                    nav.setIcon(navFriendsIcon);
                } else {
                    cardLayout.show(screens, CARD_SETTINGS);
                    nav.setIcon(navSettingsIcon);
                }
            }
        });
        return nav;
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

    public AlarmPanel    getAlarmPanel()    { return alarmPanel; }
    public FriendsPanel  getFriendsPanel()  { return friendsPanel; }
    public SettingsPanel getSettingsPanel() { return settingsPanel; }
}
