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

    private static final Image TITLE_IMG = new ImageIcon("assets/UI_Images/friends_title.png").getImage();
    private static final Image ADD_IMG   = new ImageIcon("assets/UI_Images/friends_add_btn.png").getImage();
    private static final Image ITEM_IMG  = new ImageIcon("assets/UI_Images/friend_item_bg.png").getImage();
    private static final Image WAIT_IMG  = new ImageIcon("assets/UI_Images/friend_item_waiting.png").getImage();
    private static final Image WAKE_IMG  = new ImageIcon("assets/UI_Images/friend_wake_btn.png").getImage();

    private Function<String, Boolean> onAddFriend;

    private final List<String>           itemOrder = new ArrayList<>();
    private final Map<String, ItemPanel> itemBgs   = new HashMap<>();

    public FriendsPanel() {
        setLayout(null);
        setOpaque(false);

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
        revalidate();
        repaint();
    }

    // ── item builders ─────────────────────────────────────────────────────────
    private ItemPanel buildItem(String name, Image img) {
        ItemPanel bg = new ItemPanel(img);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        nameLabel.setForeground(new Color(205, 214, 244));
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
        revalidate();
        repaint();
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

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(49, 50, 68));
        outer.setBorder(BorderFactory.createLineBorder(new Color(88, 91, 112), 1));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 30, 46));
        header.setBorder(new EmptyBorder(10, 18, 10, 18));
        JLabel headerTitle = new JLabel("Add Friend", SwingConstants.CENTER);
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        headerTitle.setForeground(new Color(137, 180, 250));
        header.add(headerTitle);
        addDrag(header, dialog);
        outer.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBorder(new EmptyBorder(16, 24, 16, 24));
        body.setBackground(new Color(49, 50, 68));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets    = new Insets(4, 4, 4, 4);
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.weightx   = 1.0;
        gbc.gridwidth = 2; gbc.gridx = 0; gbc.gridy = 0;

        JLabel prompt = new JLabel("Enter the name of the person to add:");
        prompt.setForeground(new Color(205, 214, 244));
        prompt.setFont(new Font("SansSerif", Font.PLAIN, 13));
        body.add(prompt, gbc);

        gbc.gridy = 1;
        JTextField nameField = new JTextField(16);
        nameField.setBackground(new Color(69, 71, 90));
        nameField.setForeground(new Color(205, 214, 244));
        nameField.setCaretColor(new Color(205, 214, 244));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(137, 180, 250), 1),
            new EmptyBorder(4, 8, 4, 8)));
        body.add(nameField, gbc);

        gbc.gridy = 2;
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(243, 139, 168));
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        body.add(errorLabel, gbc);

        gbc.gridy = 3; gbc.gridwidth = 1; gbc.insets = new Insets(8, 6, 4, 6);
        JButton confirmBtn = new JButton("Add");
        confirmBtn.setBackground(new Color(137, 180, 250));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        body.add(confirmBtn, gbc);

        gbc.gridx = 1;
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(88, 91, 112));
        cancelBtn.setForeground(new Color(205, 214, 244));
        cancelBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        body.add(cancelBtn, gbc);

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
