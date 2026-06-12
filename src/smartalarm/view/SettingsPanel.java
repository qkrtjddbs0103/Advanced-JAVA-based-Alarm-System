package smartalarm.view;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 46));

        JLabel label = new JLabel("Settings screen (to be implemented)", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(new Color(166, 173, 200));
        add(label, BorderLayout.CENTER);
    }
}
