import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class CareerExplorer {

    static final String APP = "CareerExplorer";

    static final String[][] ADMINS = {
        {"90028299", "12345678", "Мөнгө"},
        {"95558748", "10203040", "Чингүн"}};
    static boolean isAdmin;
    static String adminName = "";
    static boolean isAdminPhone(String ph) { for (String[] a : ADMINS) if (a[0].equals(ph)) return true; return false; }

    static final boolean DEV_MODE = true;

    static final Color GRAD1 = new Color(0x6D28D9), GRAD2 = new Color(0x2563EB),
            PRIMARY = new Color(0x5B4BDB), ACCENT = new Color(0xF59E0B), PINK = new Color(0xEC4899),
            GREEN = new Color(0x10B981), RED = new Color(0xDC2626),
            BG = new Color(0xF3F4FF), TEXT = new Color(0x1E1B4B), MUTED = new Color(0x7C80A6), LINE = new Color(0xD9DBF0);
    static final Font F = new Font("Segoe UI", Font.PLAIN, 14);

    static class User implements Serializable {
        private static final long serialVersionUID = 3L;
        String role, name, phone, regno, photo, contact, location, salt, hash;
        boolean banned, termsOk;
    }
    static final File DB = new File("careerexplorer_users.dat"), FLAG = new File("careerexplorer_seeded.flag"), PHOTOS = new File("photos");
    static List<User> users = new ArrayList<>();
    static User me;
    static String startTab = "home";

    @SuppressWarnings("unchecked")
    static void load() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DB))) { users = (List<User>) in.readObject(); }
        catch (Exception e) { users = new ArrayList<>(); }
        seedDemo();
    }
    
    static void save() {
        File tmp = new File(DB.getPath() + ".tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tmp))) {
            out.writeObject(users);
        } catch (IOException e) { e.printStackTrace(); return; }
        try { Files.move(tmp.toPath(), DB.toPath(), StandardCopyOption.REPLACE_EXISTING); }
        catch (IOException e) { e.printStackTrace(); }
    }
  
    static String hash(String pass, String salt) {
        try {
            byte[] d = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(new PBEKeySpec(pass.toCharArray(), salt.getBytes("UTF-8"), 65000, 256)).getEncoded();
            StringBuilder s = new StringBuilder();
            for (byte b : d) s.append(String.format("%02x", b));
            return s.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    static String copyPhoto(File src) throws IOException {
        PHOTOS.mkdirs();
        Path dst = PHOTOS.toPath().resolve(System.currentTimeMillis() + "_" + src.getName());
        Files.copy(src.toPath(), dst);
        return dst.toString();
    }
    static User byPhone(String ph) { for (User u : users) if (u.phone.equals(ph)) return u; return null; }

    static boolean validPhone(String ph) { return ph.matches("\\d{8}"); }
    static boolean validReg(String r) { return r.matches("[А-ЯӨҮЁA-Z]{2}\\d{8}"); }

    static void seedDemo() {
        if (DB.exists() && FLAG.exists()) return;
        String[][] orgs = {
            {"Алтан Булаг Кафе", "Сарнай", "Сүхбаатар дүүрэг, 1-р хороо", "99110001"},
            {"Гоё Дизайн Студио", "Тэмүүлэн", "Хан-Уул дүүрэг, Зайсан", "99110002"},
            {"Монгол Тех ХХК", "Бат-Эрдэнэ", "Сүхбаатар дүүрэг, Төв талбай", "99110003"},
            {"Хөх Тэнгэр Ферм", "Алтанцэцэг", "Төв аймаг, Зуунмод", "99110004"},
            {"Ирээдүй Сургалтын Төв", "Мөнхбат", "Баянзүрх дүүрэг, 13-р хороолол", "99110005"}};
        String[][] people = {
            {"Билгүүн", "УБ90010101", "99220001"}, {"Сарангэрэл", "ЧЭ95020202", "99220002"},
            {"Төгөлдөр", "УВ98030303", "99220003"}, {"Оюунчимэг", "ЖА00040404", "99220004"},
            {"Ганзориг", "ДА02050505", "99220005"}};
        for (String[] o : orgs) if (byPhone(o[3]) == null) {
            User u = new User(); u.role = "org"; u.name = o[0]; u.contact = o[1]; u.location = o[2]; u.phone = o[3];
            u.salt = UUID.randomUUID().toString(); u.hash = hash("123456", u.salt); users.add(u);
        }
        for (String[] s : people) if (byPhone(s[2]) == null) {
            User u = new User(); u.role = "seeker"; u.name = s[0]; u.regno = s[1]; u.phone = s[2];
            u.salt = UUID.randomUUID().toString(); u.hash = hash("123456", u.salt); users.add(u);
        }
        save();
        try { FLAG.createNewFile(); } catch (IOException e) { }
    }

    static final String TERMS =
        "1. " + APP + " нь 1 өдрийн туршилтаар ажил хийж үзэх боломжийг холбогч платформ юм.\n\n"
      + "2. Хэрэглэгч үнэн зөв мэдээлэл оруулна. Хуурамч мэдээлэл оруулбал бүртгэлийг хаана.\n\n"
      + "3. Нэр, утас, регистрийн дугаар, e-Mongolia зургийг зөвхөн хоёр талын найдвартай байдлыг хангах зорилгоор ашиглана.\n\n"
      + "4. Туршилтын ажлыг зөвхөн хоёр тал урьдчилан тохирсон байршил, хугацаанд хийнэ.\n\n"
      + "5. Хүн дагалдан явах үед тухайн хүний мэдээллийг (нэр, утас) байгууллагад харуулна.\n\n"
      + "6. Хэрэглэгч бусдын эрх, аюулгүй байдлыг хүндэтгэнэ. Дарамт, хууран мэхлэлтийг хатуу хориглоно.\n\n"
      + "7. Хувийн мэдээллийг гуравдагч этгээдэд зөвшөөрөлгүй дамжуулахгүй.\n\n"
      + "8. Дүрэм зөрчсөн хэрэглэгчийг хар жагсаалтад оруулж, бүртгэлийг нь хаана. "
      + "Хүнд зөрчлийн үед цагдаа болон эрх бүхий байгууллагад хандана.";

    static Color shade(Color c, float f) { return new Color((int) (c.getRed() * f), (int) (c.getGreen() * f), (int) (c.getBlue() * f)); }
    static void pin(Graphics2D g2, double px, double py, double r) {
        g2.setColor(ACCENT); g2.fill(new Ellipse2D.Double(px - r, py - r, 2 * r, 2 * r));
        Path2D tip = new Path2D.Float();
        tip.moveTo(px - r * 0.85, py + r * 0.5); tip.lineTo(px, py + r * 2.2); tip.lineTo(px + r * 0.85, py + r * 0.5); tip.closePath();
        g2.fill(tip); g2.setColor(Color.WHITE); g2.fill(new Ellipse2D.Double(px - r * 0.4, py - r * 0.4, r * 0.8, r * 0.8));
    }

    static class Gradient extends JPanel {
        boolean animated; javax.swing.Timer timer; final long t0 = System.nanoTime();
        Gradient() { setOpaque(true); }
        Gradient animated() { animated = true; return this; }
        @Override public void addNotify() { super.addNotify(); if (animated) { timer = new javax.swing.Timer(40, e -> repaint()); timer.start(); } }
        @Override public void removeNotify() { if (timer != null) timer.stop(); super.removeNotify(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, GRAD1, w, h, GRAD2)); g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(255, 255, 255, 18));
            g2.fillOval(w - 140, -60, 220, 220); g2.fillOval(-60, h - 80, 160, 160);
            if (animated) {
                double t = (System.nanoTime() - t0) / 1e9;
                for (int i = 0; i < 7; i++) {
                    double x = w * ((i * 0.23 + 0.015 * t * (i + 1)) % 1.0), y = h * (0.5 + 0.38 * Math.sin(t * 0.5 + i * 1.7)), r = 10 + i * 4;
                    g2.setColor(new Color(255, 255, 255, 26)); g2.fill(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
                }
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[]{2, 9}, 0));
                g2.setColor(new Color(255, 255, 255, 150));
                Path2D path = new Path2D.Double();
                for (int i = 0; i <= 80; i++) {
                    double u = i / 80.0, y = h * 0.88 + Math.sin(u * Math.PI * 3) * 10;
                    if (i == 0) path.moveTo(u * w, y); else path.lineTo(u * w, y);
                }
                g2.draw(path);
                double u = (t * 0.1) % 1.0;
                pin(g2, u * w, h * 0.88 + Math.sin(u * Math.PI * 3) * 10 - 22, 9);
            }
            g2.dispose();
        }
    }

    static class Logo extends JComponent {
        int s;
        Logo(int size) { s = size; setPreferredSize(new Dimension(size, size)); setMaximumSize(new Dimension(size, size)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE); g2.fillOval(0, 0, s, s);
            g2.setColor(PRIMARY);
            g2.setStroke(new BasicStroke(s / 18f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[]{s / 14f, s / 14f}, 0));
            Path2D path = new Path2D.Float();
            path.moveTo(s * 0.22, s * 0.80); path.curveTo(s * 0.20, s * 0.52, s * 0.62, s * 0.74, s * 0.60, s * 0.50);
            g2.draw(path);
            pin(g2, s * 0.62, s * 0.30, s * 0.15);
            g2.dispose();
        }
    }

    static class Scene {
        static void paint(Graphics2D g, int i, int w, int h, double t) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            switch (i % 4) { case 0: steppe(g, w, h, t); break; case 1: city(g, w, h, t); break; case 2: trail(g, w, h, t); break; default: summit(g, w, h, t); }
        }
        static void ridge(Graphics2D g, int w, int h, double base, double amp, double freq, double ph, Color c) {
            Path2D p = new Path2D.Double(); p.moveTo(0, h);
            for (int x = 0; x <= w; x += 6) p.lineTo(x, base - amp * (0.55 + 0.45 * Math.sin(x * freq + ph)) - amp * 0.35 * Math.sin(x * freq * 2.7 + ph * 1.9));
            p.lineTo(w, h); p.closePath(); g.setColor(c); g.fill(p);
        }
        static void cloud(Graphics2D g, double x, double y, double s, int a) {
            g.setColor(new Color(255, 255, 255, a));
            g.fill(new Ellipse2D.Double(x, y, s, s * 0.45)); g.fill(new Ellipse2D.Double(x + s * 0.25, y - s * 0.2, s * 0.5, s * 0.5)); g.fill(new Ellipse2D.Double(x + s * 0.5, y, s * 0.7, s * 0.4));
        }
        static void glow(Graphics2D g, double x, double y, double r, Color c) {
            g.setPaint(new RadialGradientPaint(new Point2D.Double(x, y), (float) r, new float[]{0f, 1f}, new Color[]{c, new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)}));
            g.fill(new Rectangle2D.Double(x - r, y - r, 2 * r, 2 * r));
        }
        static void bird(Graphics2D g, double x, double y, double s, double ph) {
            g.setColor(new Color(30, 27, 75, 190)); g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            double f = Math.sin(ph) * s * 0.5;
            g.draw(new Path2D.Double() {{ moveTo(x - s, y - f); quadTo(x - s * 0.4, y - s * 0.5, x, y); quadTo(x + s * 0.4, y - s * 0.5, x + s, y - f); }});
        }
        static void tree(Graphics2D g, double x, double y, double s) {
            g.setColor(new Color(0x7C4A21)); g.fill(new Rectangle2D.Double(x - s * 0.05, y - s * 0.3, s * 0.1, s * 0.3));
            g.setColor(new Color(0x2F7D4F)); g.fill(new Ellipse2D.Double(x - s * 0.22, y - s * 0.75, s * 0.44, s * 0.55));
            g.setColor(new Color(0x3E9A63)); g.fill(new Ellipse2D.Double(x - s * 0.16, y - s * 0.95, s * 0.32, s * 0.4));
        }
        static void walker(Graphics2D g, double x, double y, double s, double ph, Color c) {
            double bob = Math.abs(Math.sin(ph)) * 3 * s;
            g.setColor(c); g.fill(new Ellipse2D.Double(x - 6 * s, y - 56 * s - bob, 12 * s, 12 * s));
            g.fill(new RoundRectangle2D.Double(x - 7 * s, y - 42 * s - bob, 14 * s, 28 * s, 8 * s, 8 * s));
            g.setStroke(new BasicStroke((float) (4 * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            double sw = 8 * s * Math.sin(ph);
            g.draw(new Line2D.Double(x, y - 16 * s - bob, x + sw, y)); g.draw(new Line2D.Double(x, y - 16 * s - bob, x - sw, y));
        }

        static void steppe(Graphics2D g, int w, int h, double t) {
            g.setPaint(new GradientPaint(0, 0, new Color(0x312E81), 0, (float) (h * 0.72), new Color(0xFDBA74))); g.fillRect(0, 0, w, h);
            double sx = w * 0.76, sy = h * 0.64 - h * 0.10 * (0.5 + 0.5 * Math.sin(t * 0.3));
            glow(g, sx, sy, h * 0.6, new Color(255, 236, 170, 215));
            g.setColor(new Color(0xFFE9A8)); g.fill(new Ellipse2D.Double(sx - h * 0.08, sy - h * 0.08, h * 0.16, h * 0.16));
            for (int i = 0; i < 4; i++) cloud(g, ((i * 0.31 + t * 0.012 * (i + 1)) % 1.4 - 0.2) * w, h * (0.10 + 0.07 * (i % 3)), h * (0.30 + 0.05 * i), 150);
            ridge(g, w, h, h * 0.68, h * 0.16, 0.011, 1.0, new Color(0x8A78C4)); ridge(g, w, h, h * 0.74, h * 0.12, 0.016, 3.0, new Color(0x5B4B9E));
            ridge(g, w, h, h * 0.80, h * 0.08, 0.02, 5.0, new Color(0x3D3480));
            Path2D gr = new Path2D.Double(); gr.moveTo(0, h);
            for (int x = 0; x <= w; x += 8) gr.lineTo(x, h * 0.84 + Math.sin(x * 0.02 + t * 0.6) * 2.5);
            gr.lineTo(w, h); gr.closePath();
            g.setPaint(new GradientPaint(0, (float) (h * 0.82), new Color(0x58A56B), 0, h, new Color(0x1F5233))); g.fill(gr);
            double gx = w * 0.60, gy = h * 0.93, gw = h * 0.36, gh = h * 0.14;
            g.setColor(new Color(0xFAFAFA)); g.fill(new RoundRectangle2D.Double(gx - gw / 2, gy - gh, gw, gh, 8, 8));
            Path2D roof = new Path2D.Double(); roof.moveTo(gx - gw * 0.58, gy - gh); roof.quadTo(gx - gw * 0.3, gy - gh * 1.6, gx, gy - gh * 1.75);
            roof.quadTo(gx + gw * 0.3, gy - gh * 1.6, gx + gw * 0.58, gy - gh); roof.closePath();
            g.setColor(new Color(0xF1F5F9)); g.fill(roof); g.setColor(new Color(0x2563EB)); g.setStroke(new BasicStroke(2.5f)); g.draw(roof);
            g.setColor(new Color(0x2563EB)); g.fill(new Rectangle2D.Double(gx - gw / 2, gy - gh * 1.02, gw, gh * 0.12));
            g.setColor(new Color(0xD97706)); g.fill(new RoundRectangle2D.Double(gx - gw * 0.11, gy - gh * 0.78, gw * 0.22, gh * 0.78, 4, 4));
            g.setColor(new Color(0xDC2626)); g.fill(new Rectangle2D.Double(gx - gw * 0.03, gy - gh * 1.95, gw * 0.06, gh * 0.25));
            for (int i = 0; i < 3; i++) bird(g, ((t * 0.05 + i * 0.22) % 1.3 - 0.15) * w, h * (0.22 + 0.06 * i) + Math.sin(t + i) * 4, h * 0.035, t * 6 + i);
        }

        static void city(Graphics2D g, int w, int h, double t) {
            g.setPaint(new LinearGradientPaint(new Point2D.Float(0, 0), new Point2D.Float(0, h), new float[]{0f, 0.6f, 1f},
                    new Color[]{new Color(0x0B1026), new Color(0x6D28D9), new Color(0xF472B6)}));
            g.fillRect(0, 0, w, h);
            for (int i = 0; i < 36; i++) {
                int a = (int) (110 + 110 * Math.sin(t * 2 + i));
                g.setColor(new Color(255, 255, 255, Math.max(30, Math.min(255, a))));
                g.fill(new Ellipse2D.Double((i * 97) % Math.max(1, w), (i * 53) % Math.max(1, (int) (h * 0.5)), 2.2, 2.2));
            }
            glow(g, w * 0.78, h * 0.2, h * 0.3, new Color(255, 240, 200, 160));
            g.setColor(new Color(0xFEF3C7)); g.fill(new Ellipse2D.Double(w * 0.78 - h * 0.07, h * 0.2 - h * 0.07, h * 0.14, h * 0.14));
            for (int layer = 0; layer < 2; layer++) {
                Random r = new Random(11 + layer * 5); int x = -10;
                while (x < w) {
                    int bw = 30 + r.nextInt(30); double bh = h * (layer == 0 ? 0.26 : 0.34) + r.nextInt((int) (h * (layer == 0 ? 0.20 : 0.26)));
                    g.setColor(layer == 0 ? new Color(0x2B2A6E) : new Color(0x14123B)); g.fill(new Rectangle2D.Double(x, h - bh, bw, bh));
                    for (int wy = (int) (h - bh) + 8; wy < h - 8; wy += 12)
                        for (int wx = x + 6; wx < x + bw - 8; wx += 10) {
                            boolean lit = ((wx * 31 + wy * 17 + layer * 7 + (int) (t * 0.8)) % 7) < 3;
                            g.setColor(lit ? new Color(253, 230, 138, layer == 0 ? 150 : 235) : new Color(255, 255, 255, 14));
                            g.fill(new Rectangle2D.Double(wx, wy, 5, 6));
                        }
                    x += bw + 2 + r.nextInt(6);
                }
            }
            double cx = ((t * 0.12) % 1.2 - 0.1) * w;
            g.setColor(new Color(0xFDE68A)); g.fill(new RoundRectangle2D.Double(cx, h - 9, 22, 6, 4, 4));
            glow(g, cx + 22, h - 6, 14, new Color(255, 240, 160, 150));
        }

        static void trail(Graphics2D g, int w, int h, double t) {
            g.setPaint(new GradientPaint(0, 0, new Color(0x60A5FA), 0, (float) (h * 0.7), new Color(0xFDE7C4))); g.fillRect(0, 0, w, h);
            glow(g, w * 0.22, h * 0.28, h * 0.55, new Color(255, 244, 200, 190));
            for (int i = 0; i < 3; i++) cloud(g, ((i * 0.4 + t * 0.01 * (i + 1)) % 1.4 - 0.2) * w, h * (0.12 + 0.08 * i), h * 0.3, 190);
            ridge(g, w, h, h * 0.60, h * 0.14, 0.012, 2.0, new Color(0x86C79A)); ridge(g, w, h, h * 0.68, h * 0.12, 0.018, 0.5, new Color(0x4FA571));
            g.setPaint(new GradientPaint(0, (float) (h * 0.7), new Color(0x3E8E5A), 0, h, new Color(0x23603D))); g.fillRect(0, (int) (h * 0.72), w, h);
            Path2D road = new Path2D.Double(); road.moveTo(w * 0.53, h * 0.64); road.quadTo(w * 0.62, h * 0.78, w * 0.80, h);
            road.lineTo(w * 0.20, h); road.quadTo(w * 0.50, h * 0.80, w * 0.51, h * 0.64); road.closePath();
            g.setColor(new Color(0xE6CF9B)); g.fill(road);
            g.setColor(new Color(255, 255, 255, 200)); g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[]{6, 10}, (float) (160 - (t * 25) % 160)));
            g.draw(new Path2D.Double() {{ moveTo(w * 0.52, h * 0.65); quadTo(w * 0.55, h * 0.8, w * 0.50, h); }});
            tree(g, w * 0.92, h * 0.84, h * 0.34); tree(g, w * 0.07, h * 0.90, h * 0.30); tree(g, w * 0.84, h * 0.72, h * 0.20);
            double s = h / 230.0, x = w * 0.30 + w * 0.34 * ((t * 0.06) % 1.0), ph = t * 6;
            walker(g, x, h * 0.93, s, ph, new Color(0x1E1B4B)); walker(g, x + 30 * s, h * 0.93, s, ph + 1.3, new Color(0x9D174D));
            g.setColor(new Color(0x7C4A21)); g.fill(new Rectangle2D.Double(w * 0.62, h * 0.66, 3, h * 0.17));
            g.setColor(new Color(0xFDE68A)); g.fill(new Path2D.Double() {{ moveTo(w * 0.62 - 26, h * 0.67); lineTo(w * 0.62 + 18, h * 0.67); lineTo(w * 0.62 + 28, h * 0.70); lineTo(w * 0.62 + 18, h * 0.73); lineTo(w * 0.62 - 26, h * 0.73); closePath(); }});
            for (int i = 0; i < 3; i++) bird(g, ((t * 0.04 + i * 0.25) % 1.3 - 0.15) * w, h * (0.2 + 0.05 * i), h * 0.03, t * 6 + i);
        }

        static void summit(Graphics2D g, int w, int h, double t) {
            g.setPaint(new LinearGradientPaint(new Point2D.Float(0, 0), new Point2D.Float(0, h), new float[]{0f, 0.65f, 1f},
                    new Color[]{new Color(0x2563EB), new Color(0x67E8F9), new Color(0xFEF3C7)}));
            g.fillRect(0, 0, w, h);
            double rx = w * 0.80, ry = h * 0.22; Graphics2D r = (Graphics2D) g.create(); r.translate(rx, ry);
            for (int i = 0; i < 12; i++) {
                double a1 = t * 0.12 + i * Math.PI / 6, a2 = a1 + Math.PI / 18, R = w;
                r.setColor(new Color(255, 255, 255, 38)); r.fill(new Path2D.Double() {{ moveTo(0, 0); lineTo(R * Math.cos(a1), R * Math.sin(a1)); lineTo(R * Math.cos(a2), R * Math.sin(a2)); closePath(); }});
            }
            r.dispose();
            glow(g, rx, ry, h * 0.35, new Color(255, 250, 200, 220));
            g.setColor(new Color(0xFFF7C2)); g.fill(new Ellipse2D.Double(rx - h * 0.06, ry - h * 0.06, h * 0.12, h * 0.12));
            double px = w * 0.60, py = h * 0.26;
            Path2D m = new Path2D.Double(); m.moveTo(w * 0.05, h); m.lineTo(w * 0.30, h * 0.58); m.lineTo(px - w * 0.04, py + h * 0.10); m.lineTo(px, py);
            m.lineTo(px + w * 0.05, py + h * 0.12); m.lineTo(w * 0.86, h * 0.60); m.lineTo(w * 1.05, h); m.closePath();
            g.setPaint(new GradientPaint(0, (float) py, new Color(0x6366F1), 0, h, new Color(0x312E81))); g.fill(m);
            g.setColor(Color.WHITE); g.fill(new Path2D.Double() {{ moveTo(px, py); lineTo(px - w * 0.04, py + h * 0.10); lineTo(px - w * 0.015, py + h * 0.08); lineTo(px, py + h * 0.13); lineTo(px + w * 0.02, py + h * 0.08); lineTo(px + w * 0.05, py + h * 0.12); closePath(); }});
            double fh = h * 0.2;
            g.setColor(new Color(0x1E1B4B)); g.setStroke(new BasicStroke(2.5f)); g.draw(new Line2D.Double(px, py, px, py - fh));
            Path2D flag = new Path2D.Double(); flag.moveTo(px, py - fh);
            for (int k = 0; k <= 16; k++) flag.lineTo(px + k * h * 0.011, py - fh + Math.sin(t * 4 + k * 0.5) * 3);
            for (int k = 16; k >= 0; k--) flag.lineTo(px + k * h * 0.011, py - fh + h * 0.075 + Math.sin(t * 4 + k * 0.5) * 3);
            flag.closePath(); g.setColor(new Color(0xEC4899)); g.fill(flag);
            ridge(g, w, h, h * 0.86, h * 0.10, 0.015, 2.0, new Color(0x2E8B57)); ridge(g, w, h, h * 0.94, h * 0.06, 0.02, 4.0, new Color(0x1F5233));
            Color[] conf = {new Color(0xF59E0B), new Color(0xEC4899), Color.WHITE, new Color(0xA7F3D0)};
            for (int k = 0; k < 24; k++) {
                double x = w * 0.45 + ((k * 53) % Math.max(1, (int) (w * 0.5))), y = ((t * 55 + k * 37) % (h + 20)) - 10;
                g.setColor(conf[k % 4]); g.fill(new RoundRectangle2D.Double(x, y, 7, 7, 3, 3));
            }
        }
    }

    static class Slides extends JComponent {
        static final String[] T = {"Замаа ол", "Ажлаа хайж ол", "Хамт явж\nтуршиж үз", "Шийд,\nажилдаа ор!"},
                              S = {"Шинэ ажил,\nшинэ эхлэл", "Байгууллагуудыг\nнэг дороос үз", "Дагалдан явж\nбодитоор мэдэр", "Тохирвол\nбаг болно"};
        BufferedImage[] imgs = new BufferedImage[4];
        int cur = 0, next = 1; boolean moving; long lastSwitch = System.currentTimeMillis(), moveStart;
        final long t0 = System.nanoTime(); javax.swing.Timer timer;

        Slides() {
            for (int i = 0; i < 4; i++)
                for (String ext : new String[]{".jpg", ".png"}) {
                    File f = new File("images/slide" + (i + 1) + ext);
                    if (f.exists()) try { imgs[i] = ImageIO.read(f); } catch (Exception e) { }
                }
            setPreferredSize(new Dimension(400, 230)); setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
            setAlignmentX(Component.LEFT_ALIGNMENT); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { if (!moving) { moving = true; moveStart = System.currentTimeMillis(); } } });
        }
        @Override public void addNotify() {
            super.addNotify();
            timer = new javax.swing.Timer(33, e -> {
                long now = System.currentTimeMillis();
                if (!moving && now - lastSwitch > 4000) { moving = true; moveStart = now; }
                if (moving && now - moveStart >= 700) { cur = next; next = (cur + 1) % 4; moving = false; lastSwitch = now; }
                repaint();
            });
            timer.start();
        }
        @Override public void removeNotify() { if (timer != null) timer.stop(); super.removeNotify(); }
        static double ease(double p) { return p < 0.5 ? 2 * p * p : 1 - 2 * (1 - p) * (1 - p); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setClip(new RoundRectangle2D.Double(0, 0, w - 3, h - 3, 24, 24));
            double e = moving ? ease(Math.min(1, (System.currentTimeMillis() - moveStart) / 700.0)) : 0;
            slide(g, cur, (int) (-e * w), w, h);
            if (moving) slide(g, next, (int) (w - e * w), w, h);
            int act = e > 0.5 ? next : cur;
            for (int i = 0; i < 4; i++) {
                g.setColor(i == act ? Color.WHITE : new Color(255, 255, 255, 120));
                g.fillOval(w / 2 - 28 + i * 16, h - 20, 8, 8);
            }
            g.dispose();
        }
        void slide(Graphics2D g0, int i, int ox, int w, int h) {
            Graphics2D g = (Graphics2D) g0.create(); g.translate(ox, 0);
            double t = (System.nanoTime() - t0) / 1e9;
            if (imgs[i] != null) {
                BufferedImage im = imgs[i]; double sc = Math.max((double) w / im.getWidth(), (double) h / im.getHeight());
                g.drawImage(im, (int) ((w - im.getWidth() * sc) / 2), (int) ((h - im.getHeight() * sc) / 2), (int) (im.getWidth() * sc), (int) (im.getHeight() * sc), null);
            } else {
                g.setClip(new Rectangle(0, 0, w, h)); Scene.paint(g, i, w, h, t);
            }
            g.setPaint(new GradientPaint(0, 0, new Color(10, 8, 40, 150), w * 0.62f, 0, new Color(10, 8, 40, 0))); g.fillRect(0, 0, w, h);
            g.setColor(Color.WHITE); g.setFont(F.deriveFont(Font.BOLD, 22f));
            int y = 58; for (String l : T[i].split("\n")) { g.drawString(l, 22, y); y += 27; }
            g.setFont(F.deriveFont(13f)); g.setColor(new Color(255, 255, 255, 225)); y += 6;
            for (String l : S[i].split("\n")) { g.drawString(l, 22, y); y += 18; }
            g.dispose();
        }
    }

    static class SceneBg extends JPanel {
        javax.swing.Timer timer; final long t0 = System.nanoTime();
        SceneBg() { setOpaque(true); }
        @Override public void addNotify() { super.addNotify(); timer = new javax.swing.Timer(40, e -> repaint()); timer.start(); }
        @Override public void removeNotify() { if (timer != null) timer.stop(); super.removeNotify(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int sh = Math.min(getHeight(), 440);
            g2.setColor(new Color(0x312E81)); g2.fillRect(0, 0, getWidth(), getHeight());
            Graphics2D sg = (Graphics2D) g2.create(); sg.translate(0, getHeight() - sh); sg.setClip(0, 0, getWidth(), sh);
            Scene.paint(sg, 0, getWidth(), sh, (System.nanoTime() - t0) / 1e9); sg.dispose();
            g2.setPaint(new GradientPaint(0, 0, new Color(20, 10, 70, 60), 0, getHeight(), new Color(20, 10, 70, 20))); g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    static class Bar extends JComponent {
        String name; int pct; double shown = 1; javax.swing.Timer timer;
        Bar(String n, int p) { name = n; pct = p; setPreferredSize(new Dimension(300, 38)); setMaximumSize(new Dimension(Integer.MAX_VALUE, 38)); setAlignmentX(Component.LEFT_ALIGNMENT); }
        @Override public void addNotify() {
            super.addNotify(); shown = 0;
            timer = new javax.swing.Timer(25, e -> { shown += 0.035; if (shown >= 1) { shown = 1; timer.stop(); } repaint(); });
            timer.start();
        }
        @Override public void removeNotify() { if (timer != null) timer.stop(); super.removeNotify(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(F.deriveFont(Font.PLAIN, 13f)); g2.setColor(TEXT); g2.drawString(name, 0, 14);
            String ps = pct + "%"; g2.setColor(MUTED); g2.drawString(ps, getWidth() - g2.getFontMetrics().stringWidth(ps) - 4, 14);
            g2.setColor(LINE); g2.fill(new RoundRectangle2D.Double(0, 22, getWidth() - 4, 8, 8, 8));
            double ez = 1 - Math.pow(1 - shown, 3);
            g2.setPaint(new GradientPaint(0, 0, pct == 100 ? GREEN : ACCENT, getWidth(), 0, pct == 100 ? new Color(0x34D399) : PINK));
            g2.fill(new RoundRectangle2D.Double(0, 22, (getWidth() - 4) * pct / 100.0 * ez, 8, 8, 8));
            g2.dispose();
        }
    }

    static class Switch extends JComponent {
        boolean on;
        Switch(boolean on) {
            this.on = on; setPreferredSize(new Dimension(46, 26)); setMaximumSize(new Dimension(46, 26));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { Switch.this.on = !Switch.this.on; repaint(); } });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(on ? GREEN : LINE); g2.fillRoundRect(0, 2, 44, 22, 22, 22);
            g2.setColor(Color.WHITE); g2.fillOval(on ? 23 : 3, 4, 18, 18);
            g2.dispose();
        }
    }

    static class RoundButton extends JButton {
        Color bg, fg = Color.WHITE;
        RoundButton(String t, Color bg, Runnable r) {
            super(t); this.bg = bg;
            setFont(F.deriveFont(Font.BOLD, 14f));
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            if (r != null) addActionListener(e -> r.run());
        }
        void style(Color bg, Color fg) { this.bg = bg; this.fg = fg; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = getModel().isPressed() ? shade(bg, 0.8f) : getModel().isRollover() ? shade(bg, 0.92f) : bg;
            g2.setPaint(new GradientPaint(0, 0, c, 0, getHeight(), bg.equals(Color.WHITE) ? c : shade(c, 0.82f)));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.dispose();
            setForeground(fg);
            super.paintComponent(g);
        }
    }

    static class Card extends JPanel {
        Card() { setOpaque(false); setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16)); setAlignmentX(Component.LEFT_ALIGNMENT); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(60, 40, 160, 22)); g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 20, 20);
            g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 20, 20);
            g2.dispose();
        }
        @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }
    }

    static class Avatar extends JComponent {
        BufferedImage img; String init; int s; Color color;
        Avatar(String photo, String name, int size, Color color) {
            s = size; this.color = color;
            init = name == null || name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
            try { if (photo != null) img = ImageIO.read(new File(photo)); } catch (Exception e) { img = null; }
            setPreferredSize(new Dimension(size, size)); setMaximumSize(new Dimension(size, size));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setClip(new Ellipse2D.Double(0, 0, s, s));
            if (img != null) {
                int side = Math.min(img.getWidth(), img.getHeight());
                g2.drawImage(img, 0, 0, s, s, (img.getWidth() - side) / 2, (img.getHeight() - side) / 2, (img.getWidth() + side) / 2, (img.getHeight() + side) / 2, null);
            } else {
                g2.setPaint(new GradientPaint(0, 0, color, s, s, shade(color, 0.75f))); g2.fillRect(0, 0, s, s);
                g2.setColor(Color.WHITE); g2.setFont(F.deriveFont(Font.BOLD, s * 0.45f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(init, (s - fm.stringWidth(init)) / 2, (s + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }

    static JFrame frame;
    static CardLayout cards = new CardLayout();
    static JPanel root = new JPanel(cards);
    static Map<String, JPanel> screens = new HashMap<>();
    static final Color[] PALETTE = {PRIMARY, ACCENT, GREEN, PINK, GRAD2};

    static JLabel label(String t, float size, int style, Color c) {
        JLabel l = new JLabel(t); l.setFont(F.deriveFont(style, size)); l.setForeground(c);
        l.setAlignmentX(Component.LEFT_ALIGNMENT); return l;
    }
    static JPanel vbox() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT); return p;
    }
    static Component gap(int h) { return Box.createVerticalStrut(h); }
    static void styleField(JTextField f) {
        f.setFont(F.deriveFont(15f)); f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(LINE, 1, true), new EmptyBorder(6, 10, 6, 10)));
    }
    static JTextField field(JPanel p, String lab, String val) {
        p.add(label(lab, 12f, Font.PLAIN, MUTED));
        JTextField f = new JTextField(val == null ? "" : val); styleField(f);
        p.add(f); p.add(gap(10)); return f;
    }
    static JPasswordField passField(JPanel p, String lab) {
        p.add(label(lab, 12f, Font.PLAIN, MUTED));
        JPasswordField f = new JPasswordField(); styleField(f);
        p.add(f); p.add(gap(10)); return f;
    }
    static RoundButton button(String t, Color bg, Runnable r) { return new RoundButton(t, bg, r); }
    static RoundButton small(String t, Color bg, Runnable r) {
        RoundButton b = button(t, bg, r); b.setFont(F.deriveFont(Font.BOLD, 12f)); b.setBorder(new EmptyBorder(6, 12, 6, 12));
        b.setMaximumSize(new Dimension(130, 32)); return b;
    }
    static void msg(String m) { JOptionPane.showMessageDialog(frame, m, APP, JOptionPane.INFORMATION_MESSAGE); }
    static JScrollPane scroll(JComponent c) {
        JScrollPane s = new JScrollPane(c); s.setBorder(null); s.getVerticalScrollBar().setUnitIncrement(16);
        s.getViewport().setBackground(BG); s.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); return s;
    }
    static JPanel pad(JComponent c, int top, int side) {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(BG);
        p.setBorder(new EmptyBorder(top, side, top, side)); p.add(c); return p;
    }
    static JPanel header(String title) {
        Gradient h = new Gradient(); h.setLayout(new BoxLayout(h, BoxLayout.X_AXIS));
        h.setBorder(new EmptyBorder(18, 24, 18, 24));
        h.add(new Logo(36)); h.add(Box.createHorizontalStrut(12));
        JLabel l = new JLabel(title); l.setFont(F.deriveFont(Font.BOLD, 22f)); l.setForeground(Color.WHITE); h.add(l);
        if (DEV_MODE) {
            h.add(Box.createHorizontalGlue());
            JLabel b = new JLabel(" 🚧 BETA "); b.setOpaque(true); b.setBackground(ACCENT); b.setForeground(Color.WHITE); b.setFont(F.deriveFont(Font.BOLD, 11f)); h.add(b);
        }
        return h;
    }

    public static void main(String[] a) {
        load();
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame(APP);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(480, 780);
            frame.setMinimumSize(new Dimension(420, 600));
            frame.setLocationRelativeTo(null);
            frame.add(root);
            show("login");
            frame.setVisible(true);
        });
    }

    static void show(String name) {
        JPanel p;
        switch (name) {
            case "signup": p = signupPanel(); break;
            case "terms":  p = termsPanel(); break;
            case "main":   p = mainPanel(); break;
            case "edit":   p = editPanel(); break;
            case "admin":  p = adminPanel(); break;
            default:       p = loginPanel();
        }
        JPanel old = screens.put(name, p);
        if (old != null) root.remove(old);
        root.add(p, name);
        cards.show(root, name);
        root.revalidate(); root.repaint();
    }

    static void demoLogin(String ph) {
        User u = byPhone(ph);
        if (u == null) { msg("Жишээ хэрэглэгч олдсонгүй (админ устгасан байж магадгүй)."); return; }
        if (u.banned) { msg("Энэ хэрэглэгч хар жагсаалтад байна."); return; }
        isAdmin = false; me = u; startTab = "home"; show(u.termsOk ? "main" : "terms");
    }
    static JPanel loginPanel() {
        SceneBg bg = new SceneBg(); bg.setLayout(new GridBagLayout());
        JPanel box = vbox(); box.setBorder(new EmptyBorder(0, 40, 0, 40));
        Logo logo = new Logo(96); logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel name = new JLabel(APP); name.setFont(F.deriveFont(Font.BOLD, 32f)); name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = new JLabel(DEV_MODE ? "Ажлаа 1 өдөр туршаад үз · BETA" : "Ажлаа 1 өдөр туршаад үз"); sub.setFont(F.deriveFont(14f)); sub.setForeground(new Color(255, 255, 255, 230));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(Box.createHorizontalStrut(360));
        box.add(logo); box.add(gap(10)); box.add(name); box.add(sub); box.add(gap(22));

        Card card = new Card(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        JTextField phone = field(card, "Утасны дугаар", "");
        JPasswordField pass = passField(card, "Нууц үг");
        Runnable doLogin = () -> {
            String ph = phone.getText().trim(), pw = new String(pass.getPassword());
            for (String[] ad : ADMINS)
                if (ad[0].equals(ph) && ad[1].equals(pw)) { isAdmin = true; adminName = ad[2]; me = null; show("admin"); return; }
            for (User u : users)
                if (u.phone.equals(ph) && hash(pw, u.salt).equals(u.hash)) {
                    if (u.banned) { msg("Таны бүртгэл дүрэм зөрчсөн тул хар жагсаалтад орсон байна.\nАдминтай холбогдоно уу."); return; }
                    isAdmin = false; me = u; startTab = "home"; show(u.termsOk ? "main" : "terms"); return;
                }
            msg("Утас эсвэл нууц үг буруу");
        };
        phone.addActionListener(e -> pass.requestFocusInWindow());
        pass.addActionListener(e -> doLogin.run());
        card.add(button("Нэвтрэх", PRIMARY, doLogin));
        card.add(gap(8));
        card.add(button("Бүртгүүлэх", ACCENT, () -> show("signup")));
        if (DEV_MODE) {
            card.add(gap(14)); card.add(label("🚧 Жишээ хэрэглэгчээр орж үзэх:", 12f, Font.PLAIN, MUTED)); card.add(gap(6));
            JPanel row = new JPanel(new GridLayout(1, 2, 8, 0)); row.setOpaque(false); row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            row.add(small("Ажил хайгч", GREEN, () -> demoLogin("99220001")));
            row.add(small("Байгууллага", PINK, () -> demoLogin("99110001")));
            card.add(row);
        }
        box.add(card);
        bg.add(box, new GridBagConstraints());
        return bg;
    }

    static String pickedPhoto = "";
    static JPanel signupPanel() {
        pickedPhoto = "";
        JPanel p = vbox();
        JRadioButton seeker = new JRadioButton("Ажил хайгч (туршиж үзэх)", true), org = new JRadioButton("Байгууллага");
        for (JRadioButton r : new JRadioButton[]{seeker, org}) { r.setOpaque(false); r.setFont(F.deriveFont(Font.BOLD, 14f)); r.setAlignmentX(Component.LEFT_ALIGNMENT); }
        ButtonGroup g = new ButtonGroup(); g.add(seeker); g.add(org);
        p.add(label("Та хэн бэ?", 12f, Font.PLAIN, MUTED)); p.add(seeker); p.add(org); p.add(gap(12));

        JPanel sBox = vbox();
        JTextField name = field(sBox, "Нэр", ""), reg = field(sBox, "Регистрийн дугаар (жишээ: УБ12345678)", "");
        JLabel photoLbl = label("e-Mongolia зураг: сонгоогүй (заавал биш)", 13f, Font.PLAIN, TEXT);
        sBox.add(photoLbl); sBox.add(gap(6));
        sBox.add(button("e-Mongolia зураг сонгох", PRIMARY, () -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try { pickedPhoto = copyPhoto(fc.getSelectedFile()); photoLbl.setText("e-Mongolia зураг: сонгосон ✓"); }
                catch (IOException e) { msg("Зураг хуулж чадсангүй"); }
            }
        }));
        sBox.add(gap(12));

        JPanel oBox = vbox();
        JTextField orgName = field(oBox, "Байгууллагын нэр", ""), contact = field(oBox, "Холбоо барих хүн", ""), loc = field(oBox, "Байршил (хаяг)", "");
        oBox.setVisible(false);
        seeker.addActionListener(e -> { sBox.setVisible(true); oBox.setVisible(false); });
        org.addActionListener(e -> { sBox.setVisible(false); oBox.setVisible(true); });
        p.add(sBox); p.add(oBox);

        JTextField phone = field(p, "Утасны дугаар (8 орон)", "");
        JPasswordField pass = passField(p, "Нууц үг (6+ тэмдэгт)");
        JPasswordField pass2 = passField(p, "Нууц үг давтах");
        p.add(button("Бүртгүүлэх", PRIMARY, () -> {
            String ph = phone.getText().trim(), pw = new String(pass.getPassword());
            if (!validPhone(ph)) { msg("Утасны дугаар 8 оронтой тоо байх ёстой"); return; }
            if (pw.length() < 6) { msg("Нууц үг 6-аас дээш тэмдэгттэй байх ёстой"); return; }
            if (!pw.equals(new String(pass2.getPassword()))) { msg("Нууц үг таарахгүй байна"); return; }
            if (isAdminPhone(ph) || byPhone(ph) != null) { msg("Энэ утасны дугаар бүртгэлтэй байна"); return; }
            User u = new User(); u.phone = ph;
            if (seeker.isSelected()) {
                String rg = reg.getText().trim().toUpperCase();
                if (name.getText().trim().isEmpty()) { msg("Нэрээ бөглөнө үү"); return; }
                if (!validReg(rg)) { msg("Регистр буруу байна. Жишээ: УБ12345678 (2 үсэг + 8 тоо)"); return; }
                u.role = "seeker"; u.name = name.getText().trim(); u.regno = rg;
                u.photo = pickedPhoto.isEmpty() ? null : pickedPhoto;
            } else {
                if (orgName.getText().trim().isEmpty() || contact.getText().trim().isEmpty() || loc.getText().trim().isEmpty()) { msg("Бүх талбарыг бөглөнө үү"); return; }
                u.role = "org"; u.name = orgName.getText().trim(); u.contact = contact.getText().trim(); u.location = loc.getText().trim();
            }
            u.salt = UUID.randomUUID().toString(); u.hash = hash(pw, u.salt);
            users.add(u); save(); me = u; isAdmin = false; startTab = "home";
            show("terms");
        }));
        p.add(gap(8));
        p.add(button("Буцах", MUTED, () -> show("login")));

        JPanel page = new JPanel(new BorderLayout());
        page.add(header("Бүртгүүлэх"), BorderLayout.NORTH);
        page.add(scroll(pad(p, 20, 28)), BorderLayout.CENTER);
        return page;
    }

    static JPanel termsPanel() {
        JPanel page = new JPanel(new BorderLayout());
        page.add(header("Аппын журам"), BorderLayout.NORTH);
        JTextArea t = new JTextArea(TERMS); t.setLineWrap(true); t.setWrapStyleWord(true); t.setEditable(false);
        t.setFont(F.deriveFont(14f)); t.setForeground(TEXT); t.setBorder(new EmptyBorder(14, 14, 14, 14));
        Card c = new Card(); c.setLayout(new BorderLayout()); c.add(t);
        page.add(scroll(pad(c, 16, 20)), BorderLayout.CENTER);

        JPanel s = new JPanel(); s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS)); s.setBackground(BG);
        s.setBorder(new EmptyBorder(8, 24, 16, 24));
        JCheckBox cb = new JCheckBox("Журмыг уншиж, зөвшөөрч байна"); cb.setOpaque(false); cb.setFont(F.deriveFont(Font.BOLD, 14f));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.add(cb); s.add(gap(8));
        s.add(button("Үргэлжлүүлэх", PRIMARY, () -> {
            if (!cb.isSelected()) { msg("Журмыг зөвшөөрнө үү"); return; }
            me.termsOk = true; save(); startTab = "home"; show("main");
        }));
        page.add(s, BorderLayout.SOUTH);
        return page;
    }

    static JPanel mainPanel() {
        JPanel page = new JPanel(new BorderLayout());
        CardLayout cl = new CardLayout();
        JPanel content = new JPanel(cl);
        content.add(homeTab(), "home"); content.add(profileTab(), "profile"); content.add(settingsTab(), "settings");
        page.add(content, BorderLayout.CENTER);

        String[] keys = {"home", "profile", "settings"}, names = {"🏠 Нүүр", "👤 Профайл", "⚙ Тохиргоо", "⎋ Гарах"};
        RoundButton[] bs = new RoundButton[4];
        JPanel nav = new JPanel(new GridLayout(1, 4, 6, 0)); nav.setBackground(Color.WHITE);
        nav.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, LINE), new EmptyBorder(8, 8, 8, 8)));
        for (int i = 0; i < 4; i++) {
            final int k = i;
            bs[i] = button(names[i], Color.WHITE, () -> {
                if (k == 3) {
                    if (JOptionPane.showConfirmDialog(frame, "Та гарахдаа итгэлтэй байна уу?", "Гарах", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        me = null; show("login");
                    }
                    return;
                }
                cl.show(content, keys[k]);
                for (int j = 0; j < 3; j++) bs[j].style(j == k ? PRIMARY : Color.WHITE, j == k ? Color.WHITE : MUTED);
            });
            bs[i].setFont(F.deriveFont(Font.BOLD, 12f)); bs[i].setBorder(new EmptyBorder(8, 2, 8, 2));
            bs[i].style(Color.WHITE, i == 3 ? RED : MUTED);
            nav.add(bs[i]);
        }
        int start = startTab.equals("profile") ? 1 : 0;
        cl.show(content, keys[start]);
        bs[start].style(PRIMARY, Color.WHITE);
        page.add(nav, BorderLayout.SOUTH);
        return page;
    }

    static void fillList(JPanel list, boolean seeker, String q) {
        list.removeAll();
        int n = 0;
        for (User u : users) {
            if (u.role.equals(me.role) || u.banned) continue;
            String hay = (u.name + " " + (seeker ? u.location : u.phone)).toLowerCase();
            if (!q.isEmpty() && !hay.contains(q)) continue;
            Card c = new Card(); c.setLayout(new BorderLayout(12, 0));
            c.add(new Avatar(seeker ? null : u.photo, u.name, 46, PALETTE[n % PALETTE.length]), BorderLayout.WEST);
            JPanel tx = vbox();
            tx.add(label(u.name, 15f, Font.BOLD, TEXT));
            tx.add(label(seeker ? "📍 " + u.location : "📞 " + u.phone, 12f, Font.PLAIN, MUTED));
            c.add(tx, BorderLayout.CENTER);
            c.add(label("›", 24f, Font.BOLD, MUTED), BorderLayout.EAST);
            c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            c.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { details(u, seeker); } });
            list.add(c); list.add(gap(8)); n++;
        }
        if (n == 0) list.add(label(q.isEmpty() ? "Одоогоор бүртгэл алга." : "Хайлтад тохирох үр дүн олдсонгүй.", 13f, Font.PLAIN, MUTED));
        list.revalidate(); list.repaint();
    }

    static JPanel homeTab() {
        boolean seeker = "seeker".equals(me.role);
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(BG);

        Gradient hero = new Gradient().animated(); hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(new EmptyBorder(28, 24, 64, 24)); hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        Logo logo = new Logo(64); logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.add(logo); hero.add(gap(10));
        hero.add(label(APP, 36f, Font.BOLD, Color.WHITE));
        hero.add(label("Сайн байна уу, " + me.name + "!", 14f, Font.PLAIN, new Color(255, 255, 255, 225)));
        hero.add(gap(10));
        JTextArea intro = new JTextArea(APP + " бол ажил хайгчид 1 өдрийн туршилтаар жинхэнэ ажлыг хийж үзэх, "
                + "байгууллагууд тохирох хүнээ олох боломжийг олгодог холбогч апп юм. Дагалдан явж, ажлаа туршаад, дараа нь шийд.");
        intro.setEditable(false); intro.setOpaque(false); intro.setLineWrap(true); intro.setWrapStyleWord(true);
        intro.setFont(F.deriveFont(14f)); intro.setForeground(Color.WHITE); intro.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.add(intro);
        p.add(hero);

        JPanel body = vbox(); body.setBorder(new EmptyBorder(18, 20, 18, 20));
        body.setBackground(BG); body.setOpaque(true);
        if (DEV_MODE) {
            Card dev = new Card(); dev.setLayout(new BorderLayout());
            dev.add(label("<html><body style='width:330px'>🚧 <b>Хөгжүүлэлтийн шатанд.</b> Нэвтрэх, бүртгүүлэх, профайл засах, журам, админ хэсэг ажиллана. Тохиргоо нь жишээ харагдац юм.</body></html>", 12f, Font.PLAIN, new Color(0xB45309)));
            body.add(dev); body.add(gap(14));
        }
        body.add(new Slides()); body.add(gap(18));
        body.add(label("Хэрхэн ажилладаг вэ?", 17f, Font.BOLD, TEXT)); body.add(gap(8));
        String[][] steps = {{"1", "Бүртгүүлнэ", "Мэдээллээ оруулж, журмыг зөвшөөрнө"},
                            {"2", "Холбогдоно", seeker ? "Байгууллагыг сонгож холбогдоно" : "Ажил хайгчтай холбогдоно"},
                            {"3", "1 өдөр туршина", "Дагалдан явж ажлыг бодитоор хийж үзнэ"}};
        for (String[] s : steps) {
            Card c = new Card(); c.setLayout(new BorderLayout(12, 0));
            JLabel num = new JLabel(s[0], SwingConstants.CENTER); num.setOpaque(true); num.setBackground(ACCENT);
            num.setForeground(Color.WHITE); num.setFont(F.deriveFont(Font.BOLD, 16f)); num.setPreferredSize(new Dimension(34, 34));
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); left.setOpaque(false); left.add(num);
            JPanel tx = vbox(); tx.add(label(s[1], 14f, Font.BOLD, TEXT)); tx.add(label(s[2], 12f, Font.PLAIN, MUTED));
            c.add(left, BorderLayout.WEST); c.add(tx, BorderLayout.CENTER);
            body.add(c); body.add(gap(8));
        }

        body.add(gap(10));
        body.add(label(seeker ? "Ажлын санал (байгууллагууд)" : "Туршилтад хамрагдах хүмүүс", 17f, Font.BOLD, TEXT)); body.add(gap(8));
        body.add(label(seeker ? "Хайх (нэр, байршил)" : "Хайх (нэр, утас)", 12f, Font.PLAIN, MUTED));
        JTextField search = new JTextField(); styleField(search);
        body.add(search); body.add(gap(10));
        JPanel list = vbox();
        body.add(list);
        fillList(list, seeker, "");
        search.getDocument().addDocumentListener(new DocumentListener() {
            void upd() { fillList(list, seeker, search.getText().trim().toLowerCase()); }
            @Override public void insertUpdate(DocumentEvent e) { upd(); }
            @Override public void removeUpdate(DocumentEvent e) { upd(); }
            @Override public void changedUpdate(DocumentEvent e) { upd(); }
        });
        p.add(body);

        JPanel w = new JPanel(new BorderLayout()); w.add(scroll(p)); return w;
    }

    static void details(User u, boolean org) {
        String d = org
            ? "Байгууллага: " + u.name + "\nХолбоо барих: " + u.contact + "\nБайршил: " + u.location + "\nУтас: " + u.phone
            : "Нэр: " + u.name + "\nУтас: " + u.phone;
        JOptionPane.showMessageDialog(frame, d, "Дэлгэрэнгүй", JOptionPane.INFORMATION_MESSAGE);
    }

    static JPanel profileTab() {
        boolean seeker = "seeker".equals(me.role);
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(BG);

        Gradient h = new Gradient(); h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
        h.setBorder(new EmptyBorder(26, 24, 26, 24)); h.setAlignmentX(Component.LEFT_ALIGNMENT);
        h.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        Avatar av = new Avatar(me.photo, me.name, 110, ACCENT); av.setAlignmentX(Component.CENTER_ALIGNMENT);
        h.add(av); h.add(gap(10));
        JLabel nm = label(me.name, 22f, Font.BOLD, Color.WHITE); nm.setAlignmentX(Component.CENTER_ALIGNMENT); h.add(nm);
        JLabel role = label(seeker ? "Ажил хайгч" : "Байгууллага", 13f, Font.PLAIN, new Color(255, 255, 255, 210));
        role.setAlignmentX(Component.CENTER_ALIGNMENT); h.add(role);
        p.add(h);

        JPanel body = vbox(); body.setOpaque(true); body.setBackground(BG); body.setBorder(new EmptyBorder(18, 20, 18, 20));
        Card c = new Card(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        if (seeker) {
            row(c, "Нэр", me.name); row(c, "Утас", me.phone);
            String r = me.regno == null ? "" : me.regno;
            row(c, "Регистр", r.length() == 10 ? r.substring(0, 2) + "******" + r.substring(8) : "—");
        } else {
            row(c, "Байгууллага", me.name); row(c, "Холбоо барих", me.contact);
            row(c, "Байршил", me.location); row(c, "Утас", me.phone);
        }
        body.add(c); body.add(gap(14));
        body.add(button("✎ Профайл засах", PRIMARY, () -> show("edit")));
        p.add(body);
        JPanel w = new JPanel(new BorderLayout()); w.add(scroll(p)); return w;
    }
    static void row(JPanel c, String k, String v) {
        c.add(label(k, 12f, Font.PLAIN, MUTED)); c.add(label(v, 15f, Font.BOLD, TEXT)); c.add(gap(10));
    }

    static String newPhoto;
    static JPanel editPanel() {
        newPhoto = null;
        boolean seeker = "seeker".equals(me.role);
        JPanel p = vbox();
        JTextField name = field(p, seeker ? "Нэр" : "Байгууллагын нэр", me.name);
        JTextField reg = null, contact = null, loc = null;
        if (seeker) {
            reg = field(p, "Регистрийн дугаар", me.regno);
            JLabel pl = label("e-Mongolia зураг: одоогийнхыг хэвээр", 13f, Font.PLAIN, TEXT);
            p.add(pl); p.add(gap(6));
            p.add(button("Зураг солих", ACCENT, () -> {
                JFileChooser fc = new JFileChooser();
                if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    try { newPhoto = copyPhoto(fc.getSelectedFile()); pl.setText("Шинэ зураг сонгосон ✓"); }
                    catch (IOException e) { msg("Зураг хуулж чадсангүй"); }
                }
            }));
            p.add(gap(12));
        } else {
            contact = field(p, "Холбоо барих хүн", me.contact);
            loc = field(p, "Байршил (хаяг)", me.location);
        }
        JTextField phone = field(p, "Утасны дугаар", me.phone);
        JPasswordField pass = passField(p, "Шинэ нууц үг (солихгүй бол хоосон үлдээ)");

        final JTextField fReg = reg, fContact = contact, fLoc = loc;
        p.add(button("Хадгалах", PRIMARY, () -> {
            String ph = phone.getText().trim(), pw = new String(pass.getPassword());
            if (name.getText().trim().isEmpty()) { msg("Нэрээ бөглөнө үү"); return; }
            if (!validPhone(ph)) { msg("Утасны дугаар 8 оронтой тоо байх ёстой"); return; }
            if (isAdminPhone(ph)) { msg("Энэ утасны дугаар бүртгэлтэй байна"); return; }
            for (User u : users) if (u != me && u.phone.equals(ph)) { msg("Энэ утасны дугаар бүртгэлтэй байна"); return; }
            if (!pw.isEmpty() && pw.length() < 6) { msg("Нууц үг 6-аас дээш тэмдэгттэй байх ёстой"); return; }
            if (seeker) {
                String rg = fReg.getText().trim().toUpperCase();
                if (!validReg(rg)) { msg("Регистр буруу байна. Жишээ: УБ12345678 (2 үсэг + 8 тоо)"); return; }
                me.regno = rg;
                if (newPhoto != null) me.photo = newPhoto;
            } else {
                if (fContact.getText().trim().isEmpty() || fLoc.getText().trim().isEmpty()) { msg("Бүх талбарыг бөглөнө үү"); return; }
                me.contact = fContact.getText().trim(); me.location = fLoc.getText().trim();
            }
            me.name = name.getText().trim(); me.phone = ph;
            if (!pw.isEmpty()) { me.salt = UUID.randomUUID().toString(); me.hash = hash(pw, me.salt); }
            save(); msg("Мэдээлэл амжилттай хадгалагдлаа ✓");
            startTab = "profile"; show("main");
        }));
        p.add(gap(8));
        p.add(button("Болих", MUTED, () -> { startTab = "profile"; show("main"); }));

        JPanel page = new JPanel(new BorderLayout());
        page.add(header("Профайл засах"), BorderLayout.NORTH);
        page.add(scroll(pad(p, 20, 28)), BorderLayout.CENTER);
        return page;
    }

    static void switchRow(JPanel c, String text, boolean on) {
        JPanel r = new JPanel(new BorderLayout()); r.setOpaque(false); r.setAlignmentX(Component.LEFT_ALIGNMENT);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        r.add(label(text, 14f, Font.PLAIN, TEXT), BorderLayout.CENTER); r.add(new Switch(on), BorderLayout.EAST);
        c.add(r); c.add(gap(6));
    }
    static JPanel settingsTab() {
        JPanel page = new JPanel(new BorderLayout());
        page.add(header("Тохиргоо"), BorderLayout.NORTH);
        JPanel p = vbox();
        if (DEV_MODE) {
            p.add(label("🚧 Хөгжүүлэлтийн явц", 17f, Font.BOLD, TEXT)); p.add(gap(8));
            Card r = new Card(); r.setLayout(new BoxLayout(r, BoxLayout.Y_AXIS));
            String[] n = {"Нэвтрэх", "Бүртгүүлэх", "Аппын журам", "Админ самбар", "Профайл", "Нүүр хуудас", "Тохиргоо"};
            int[] v = {100, 100, 100, 100, 100, 80, 40};
            for (int i = 0; i < n.length; i++) r.add(new Bar(n[i], v[i]));
            p.add(r); p.add(gap(18));
        }
        p.add(label("Тохиргоо" + (DEV_MODE ? " (жишээ)" : ""), 17f, Font.BOLD, TEXT)); p.add(gap(8));
        Card s = new Card(); s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));
        switchRow(s, "Мэдэгдэл", true); switchRow(s, "Харанхуй горим", false); switchRow(s, "Дуу чимээ", true);
        p.add(s); p.add(gap(18));
        p.add(label("Аппын журам", 17f, Font.BOLD, TEXT)); p.add(gap(8));
        JTextArea t = new JTextArea(TERMS + "\n\n" + APP + " v1.0 beta");
        t.setLineWrap(true); t.setWrapStyleWord(true); t.setEditable(false); t.setOpaque(false);
        t.setFont(F.deriveFont(14f)); t.setForeground(TEXT); t.setAlignmentX(Component.LEFT_ALIGNMENT);
        Card c = new Card(); c.setLayout(new BorderLayout()); c.add(t);
        p.add(c);
        page.add(scroll(pad(p, 16, 20)), BorderLayout.CENTER);
        return page;
    }

    static JPanel adminPanel() {
        JPanel p = vbox();
        Card ac = new Card(); ac.setLayout(new BorderLayout(12, 0));
        ac.add(new Avatar(null, adminName, 52, PRIMARY), BorderLayout.WEST);
        JPanel at = vbox();
        at.add(label("Сайн байна уу, " + adminName + "!", 17f, Font.BOLD, TEXT));
        at.add(label("👑 Админ эрхээр нэвтэрсэн", 12f, Font.PLAIN, MUTED));
        StringBuilder an = new StringBuilder(); for (String[] a : ADMINS) an.append(an.length() == 0 ? "" : ", ").append(a[2]);
        at.add(label("Админууд: " + an, 12f, Font.PLAIN, MUTED));
        ac.add(at, BorderLayout.CENTER);
        p.add(ac); p.add(gap(12));
        int seekers = 0, orgs = 0, banned = 0;
        for (User u : users) { if (u.role.equals("seeker")) seekers++; else orgs++; if (u.banned) banned++; }
        Card stats = new Card(); stats.setLayout(new GridLayout(1, 3, 8, 0));
        String[][] st = {{"" + seekers, "Ажил хайгч"}, {"" + orgs, "Байгууллага"}, {"" + banned, "Хар жагсаалт"}};
        for (int i = 0; i < 3; i++) {
            JPanel c = vbox(); c.add(label(st[i][0], 26f, Font.BOLD, i == 2 ? RED : PRIMARY)); c.add(label(st[i][1], 12f, Font.PLAIN, MUTED)); stats.add(c);
        }
        p.add(stats); p.add(gap(16));
        p.add(label("Бүх хэрэглэгчид", 17f, Font.BOLD, TEXT)); p.add(gap(8));
        int n = 0;
        for (User u : new ArrayList<>(users)) {
            Card c = new Card(); c.setLayout(new BorderLayout(12, 0));
            c.add(new Avatar(u.photo, u.name, 44, PALETTE[n++ % PALETTE.length]), BorderLayout.WEST);
            JPanel tx = vbox();
            tx.add(label(u.name, 14f, Font.BOLD, TEXT));
            tx.add(label((u.role.equals("org") ? "Байгууллага" : "Ажил хайгч") + " · " + u.phone, 12f, Font.PLAIN, MUTED));
            if (u.banned) tx.add(label("⛔ Хар жагсаалтад байна", 12f, Font.BOLD, RED));
            c.add(tx, BorderLayout.CENTER);
            JPanel act = vbox();
            act.add(small(u.banned ? "Гаргах" : "Хориглох", u.banned ? GREEN : ACCENT, () -> {
                u.banned = !u.banned; save(); show("admin");
            }));
            act.add(gap(4));
            act.add(small("Устгах", RED, () -> {
                if (JOptionPane.showConfirmDialog(frame, "\"" + u.name + "\"-г бүр мөсөн устгах уу?", "Устгах", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    users.remove(u); save(); show("admin");
                }
            }));
            c.add(act, BorderLayout.EAST);
            p.add(c); p.add(gap(8));
        }
        if (users.isEmpty()) p.add(label("Бүртгэлтэй хэрэглэгч алга.", 13f, Font.PLAIN, MUTED));
        p.add(gap(8));
        p.add(button("Гарах", MUTED, () -> { isAdmin = false; show("login"); }));

        JPanel page = new JPanel(new BorderLayout());
        page.add(header("Админ самбар"), BorderLayout.NORTH);
        page.add(scroll(pad(p, 16, 20)), BorderLayout.CENTER);
        return page;
    }
}