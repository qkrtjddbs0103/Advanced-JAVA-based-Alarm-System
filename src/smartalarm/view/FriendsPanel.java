package smartalarm.view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class FriendsPanel extends JPanel {

    private final JPanel listPanel;

    public FriendsPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 46));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Friends", SwingConstants.LEFT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(205, 214, 244));
        titleLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
        add(titleLabel, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(30, 30, 46));
        add(listPanel, BorderLayout.CENTER);
    }

    public void addFriend(String displayName, ActionListener onWake) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(new Color(49, 50, 68));
        row.setBorder(new EmptyBorder(12, 14, 12, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        nameLabel.setForeground(new Color(205, 214, 244));

        JButton wakeButton = new JButton("Wake");
        wakeButton.setBackground(new Color(137, 180, 250));
        wakeButton.setForeground(Color.WHITE);
        wakeButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        wakeButton.setFocusPainted(false);
        wakeButton.setBorderPainted(false);
        wakeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        wakeButton.addActionListener(onWake);

        row.add(nameLabel, BorderLayout.CENTER);
        row.add(wakeButton, BorderLayout.EAST);

        listPanel.add(Box.createVerticalStrut(8));
        listPanel.add(row);
    }
}
