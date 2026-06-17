package smartalarm.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.awt.RenderingHints.*;

public class FriendsPanel extends JPanel {

    private static final int ITEM_X       = 10;
    private static final int ITEM_W       = 370;
    private static final int ITEM_H       = 64;
    private static final int ITEM_START_Y = 108;
    private static final int WAKE_W       = 76;
    private static final int WAKE_H       = 34;
    private static final int WAKE_RIGHT   = 20;

    private static final Image DIALOG_BG      = new ImageIcon("assets/UI_Images/background.png").getImage();
    private static final Image EMPTY_ICON     = new ImageIcon("assets/UI_Images/friends_center_icon.png").getImage();
    private static final Image INPUT_BOX_IMG  = new ImageIcon("assets/UI_Images/input_box.png").getImage();
    private static final Image ADD_FRIEND_IMG = new ImageIcon("assets/UI_Images/addfriend_add_btn.png").getImage();
    private static final Image CANCEL_IMG     = new ImageIcon("assets/UI_Images/addfriend_cancel_btn.png").getImage();
    private static final Image TITLE_IMG  = new ImageIcon("assets/UI_Images/friends_title.png").getImage();
    private static final Image ADD_IMG   = new ImageIcon("assets/UI_Images/friends_add_btn.png").getImage();
    private static final Image ITEM_IMG  = new ImageIcon("assets/UI_Images/friend_item_bg.png").getImage();
    private static final Image WAIT_IMG  = new ImageIcon("assets/UI_Images/friend_item_waiting.png").getImage();
    private static final Image WAKE_IMG  = new ImageIcon("assets/UI_Images/friend_wake_btn.png").getImage();

    private JLabel miniTimeLbl;
    private Function<String, Boolean> onAddFriend;

    private final List<String>           itemOrder  = new ArrayList<>();
    private final Map<String, ItemPanel> itemBgs    = new HashMap<>();
    private JPanel                       emptyState;

    public FriendsPanel() {
        setLayout(null);
        setOpaque(false);

        miniTimeLbl = new JLabel("00:00", SwingConstants.LEFT);
        miniTimeLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        miniTimeLbl.setForeground(new Color(37, 99, 168));
        miniTimeLbl.setOpaque(false);
        miniTimeLbl.setBounds(25, 23, 80, 18);
        add(miniTimeLbl);

        emptyState = buildEmptyState();
        emptyState.setBounds(0, 108, 390, 430);
        add(emptyState);

        JButton addBtn = imageButton(ADD_IMG);
        addBtn.setBounds(308, 57, 72, 34);
        addBtn.addActionListener(e -> showAddFriendDialog());
        add(addBtn);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
        g2.setRenderingHint(KEY_ANTIALIASING,  VALUE_ANTIALIAS_ON);
        g2.drawImage(TITLE_IMG, 24, 49, 130, 56, this);
    }

    public void setMiniTime(String text) { miniTimeLbl.setText(text); }

    public void setOnAddFriend(Function<String, Boolean> callback) {
        this.onAddFriend = callback;
    }

    // ── confirmed friend (blank item + Wake button) ──────────────────────────
    public void addFriend(String name, ActionListener onWake) {
        ItemPanel bg = buildItem(name, ITEM_IMG);
        attachWakeButton(bg, onWake);
        placeItem(name, bg);
    }

    // ── pending friend (waiting image — text baked into image) ────────────────
    public void addFriendPending(String name) {
        ItemPanel bg = buildItem(name, WAIT_IMG);
        placeItem(name, bg);
    }

    // ── upgrade pending → confirmed ──────────────────────────────────────────
    public void confirmFriend(String name, ActionListener onWake) {
        ItemPanel bg = itemBgs.get(name);
        if (bg == null) return;
        bg.setImage(ITEM_IMG);
        attachWakeButton(bg, onWake);
        bg.revalidate();
        bg.repaint();
    }

    // ── update friend name after rename ──────────────────────────────────────
    public void updateFriendName(String oldName, String newName) {
        ItemPanel bg = itemBgs.remove(oldName);
        if (bg == null) return;
        int idx = itemOrder.indexOf(oldName);
        if (idx >= 0) itemOrder.set(idx, newName);
        itemBgs.put(newName, bg);
        for (Component c : bg.getComponents()) {
            if (c instanceof JLabel) {
                ((JLabel) c).setText(newName);
                break;
            }
        }
        bg.repaint();
    }

    // ── remove pending row ───────────────────────────────────────────────────
    public void removeFriendPending(String name) {
        ItemPanel bg = itemBgs.remove(name);
        if (bg == null) return;

        int idx = itemOrder.indexOf(name);
        itemOrder.remove(idx);
        remove(bg);

        for (int i = idx; i < itemOrder.size(); i++) {
            ItemPanel item = itemBgs.get(itemOrder.get(i));
            if (item != null) item.setLocation(ITEM_X, ITEM_START_Y + i * ITEM_H);
        }
        emptyState.setVisible(itemOrder.isEmpty());
        revalidate();
        repaint();
    }

    // ── item builders ─────────────────────────────────────────────────────────
    private ItemPanel buildItem(String name, Image img) {
        ItemPanel bg = new ItemPanel(img);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        nameLabel.setForeground(new Color(166, 173, 200));
        nameLabel.setBounds(60, (ITEM_H - 20) / 2, 180, 20);
        bg.add(nameLabel);

        return bg;
    }

    private void attachWakeButton(ItemPanel bg, ActionListener onWake) {
        int wakeX = ITEM_W - WAKE_RIGHT - WAKE_W;
        int wakeY = (ITEM_H - WAKE_H) / 2;

        JButton wakeBtn = imageButton(WAKE_IMG);
        wakeBtn.setBounds(wakeX, wakeY, WAKE_W, WAKE_H);
        wakeBtn.addActionListener(onWake);
        bg.add(wakeBtn);
    }

    private void placeItem(String name, ItemPanel bg) {
        int y = ITEM_START_Y + itemOrder.size() * ITEM_H;
        bg.setBounds(ITEM_X, y, ITEM_W, ITEM_H);
        itemOrder.add(name);
        itemBgs.put(name, bg);
        add(bg);
        emptyState.setVisible(false);
        revalidate();
        repaint();
    }

    // ── empty state ──────────────────────────────────────────────────────────
    private JPanel buildEmptyState() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel iconLbl = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
                g2.drawImage(EMPTY_ICON, 0, 0, getWidth(), getHeight(), this);
            }
        };
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLbl.setPreferredSize(new Dimension(80, 80));
        iconLbl.setMaximumSize(new Dimension(80, 80));

        JLabel titleLbl = new JLabel("Friends", SwingConstants.CENTER);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLbl.setForeground(new Color(90, 127, 168));
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLbl = new JLabel("Add your friends and wake them up!", SwingConstants.CENTER);
        subtitleLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLbl.setForeground(new Color(90, 127, 168));
        subtitleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(iconLbl);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(titleLbl);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));
        panel.add(subtitleLbl);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // ── image button (bicubic rendering) ──────────────────────────────────────
    private static JButton imageButton(Image img) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
                g2.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        };
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── item panel (draws background with bicubic) ────────────────────────────
    private static class ItemPanel extends JPanel {
        private Image img;

        ItemPanel(Image img) {
            this.img = img;
            setLayout(null);
            setOpaque(false);
        }

        void setImage(Image img) { this.img = img; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
            g2.setRenderingHint(KEY_ANTIALIASING,  VALUE_ANTIALIAS_ON);
            g2.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // ── Add Friend dialog ─────────────────────────────────────────────────────
    private void showAddFriendDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.DOCUMENT_MODAL);
        dialog.setUndecorated(true);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel outer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
                g2.drawImage(DIALOG_BG, 0, 0, getWidth(), getHeight(), this);
            }
        };
        outer.setBorder(BorderFactory.createLineBorder(new Color(59, 139, 255, 77), 1));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(10, 18, 10, 18));
        JLabel headerTitle = new JLabel("Add Friend", SwingConstants.CENTER);
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        headerTitle.setForeground(new Color(137, 180, 250));
        header.add(headerTitle);
        addDrag(header, dialog);
        outer.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBorder(new EmptyBorder(16, 24, 16, 24));
        body.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets    = new Insets(4, 4, 4, 4);
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.weightx   = 1.0;
        gbc.gridwidth = 2; gbc.gridx = 0; gbc.gridy = 0;

        JLabel prompt = new JLabel("Enter the name of the person to add:");
        prompt.setForeground(new Color(166, 173, 200));
        prompt.setFont(new Font("SansSerif", Font.PLAIN, 13));
        body.add(prompt, gbc);

        gbc.gridy = 1;
        JTextField nameField = new JTextField(16) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
                g2.drawImage(INPUT_BOX_IMG, 0, 0, getWidth(), getHeight(), this);
                super.paintComponent(g);
            }
        };
        nameField.setOpaque(false);
        nameField.setForeground(new Color(90, 127, 168));
        nameField.setCaretColor(new Color(205, 214, 244));
        nameField.setBorder(new EmptyBorder(4, 8, 4, 8));
        body.add(nameField, gbc);

        gbc.gridy = 2;
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(224, 53, 53));
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        body.add(errorLabel, gbc);

        JButton confirmBtn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
                g2.drawImage(ADD_FRIEND_IMG, 0, 0, getWidth(), getHeight(), this);
            }
        };
        confirmBtn.setBorderPainted(false);
        confirmBtn.setContentAreaFilled(false);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setPreferredSize(new Dimension(90, 34));
        confirmBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton cancelBtn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
                g2.drawImage(CANCEL_IMG, 0, 0, getWidth(), getHeight(), this);
            }
        };
        cancelBtn.setBorderPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setPreferredSize(new Dimension(90, 34));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        gbc.gridy = 3; gbc.gridwidth = 2; gbc.gridx = 0;
        gbc.insets = new Insets(8, 0, 4, 0);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(confirmBtn);
        btnRow.add(cancelBtn);
        body.add(btnRow, gbc);

        outer.add(body, BorderLayout.CENTER);
        dialog.add(outer);

        confirmBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (onAddFriend != null && Boolean.TRUE.equals(onAddFriend.apply(name))) {
                dialog.dispose();
            } else {
                errorLabel.setText("That name does not exist!");
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        nameField.addActionListener(e -> confirmBtn.doClick());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private static void addDrag(Component handle, Window window) {
        int[] ox = {0}, oy = {0};
        handle.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { ox[0] = e.getX(); oy[0] = e.getY(); }
        });
        handle.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point p = window.getLocation();
                window.setLocation(p.x + e.getX() - ox[0], p.y + e.getY() - oy[0]);
            }
        });
    }
}
