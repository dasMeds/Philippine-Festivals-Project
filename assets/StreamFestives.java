import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.Timer;

public class StreamFestives extends JFrame {

    // --- Palette ---
    private static final Color COL_BG = new Color(12, 12, 12);
    private static final Color COL_ACCENT = new Color(229, 9, 20); // Netflix Red
    private static final Color COL_TEXT_PRI = new Color(255, 255, 255);
    private static final Color COL_TEXT_SEC = new Color(180, 180, 180);

    // --- Fonts ---
    private static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 28);
    private static final Font FONT_SUB = new Font("SansSerif", Font.PLAIN, 16);
    private static final Font FONT_CARD = new Font("SansSerif", Font.BOLD, 15);
    
    // --- Data ---
    private Map<String, List<String>> islandMap = new LinkedHashMap<>();
    private Map<String, List<Festival>> regionMap = new LinkedHashMap<>();

    // --- UI Components ---
    private CardLayout mainLayout;
    private JPanel mainPanel;
    
    private JPanel homePanel;    
    private JPanel regionPanel;  
    private JPanel browserPanel; 
    
    private JPanel regionGrid;   
    private JPanel festivalGrid; 
    
    // Class-level variables
    private JLabel browserTitle; 
    private JLabel regionTitle; 

    private JPanel glassPane;

    public StreamFestives() {
        setTitle("Philippine Festivals Database");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COL_BG);

        buildDatabase(); 
        initUI();
        initGlassPane();
    }

    private void initUI() {
        mainLayout = new CardLayout();
        mainPanel = new JPanel(mainLayout);
        mainPanel.setBackground(COL_BG);

        createHomePanel();
        createRegionPanel();
        createBrowserPanel();

        mainPanel.add(homePanel, "HOME");
        mainPanel.add(regionPanel, "REGION");
        mainPanel.add(browserPanel, "BROWSER");

        add(mainPanel);
        mainLayout.show(mainPanel, "HOME");
    }

    // --- GLASS PANE & MODAL LOGIC ---
    private void initGlassPane() {
        glassPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0, 0, 0, 220));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        glassPane.setOpaque(false);
        glassPane.setLayout(new GridBagLayout());
        
        glassPane.addMouseListener(new MouseAdapter() {}); 
        glassPane.addKeyListener(new KeyAdapter() {});
        
        setGlassPane(glassPane);
    }

    private void showFestivalModal(Festival f) {
        glassPane.removeAll();

        JPanel modalContainer = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(25, 25, 25, 250)); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 0, 0);
            }
        };
        
        Dimension screenSize = getSize();
        int modalW = (int)(screenSize.width * 0.95);
        int modalH = (int)(screenSize.height * 0.90);
        modalContainer.setPreferredSize(new Dimension(modalW, modalH));
        modalContainer.setOpaque(false);
        modalContainer.setBorder(new EmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        
        // --- LEFT COLUMN: Image & Actions ---
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        
        JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createLineBorder(new Color(60,60,60), 1));
        
        new Thread(() -> {
            try {
                URL url = new URL(f.getThumbUrl());
                BufferedImage img = ImageIO.read(url);
                if(img != null) {
                    Image scaled = img.getScaledInstance(500, 350, Image.SCALE_SMOOTH);
                    SwingUtilities.invokeLater(() -> imageLabel.setIcon(new ImageIcon(scaled)));
                }
            } catch(Exception e) {}
        }).start();

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 20)); 
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(30, 0, 0, 0)); 

        JButton watchBtn = new JButton("WATCH VIDEO ►");
        watchBtn.setFont(new Font("SansSerif", Font.BOLD, 22)); 
        watchBtn.setBackground(COL_ACCENT);
        watchBtn.setForeground(Color.WHITE);
        watchBtn.setFocusPainted(false);
        watchBtn.setBorderPainted(false);
        watchBtn.setPreferredSize(new Dimension(0, 80)); 
        watchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        watchBtn.addActionListener(e -> {
            glassPane.setVisible(false);
            openLink(f.videoId);
        });

        JButton closeBtn = createSimpleButton("CLOSE OVERVIEW");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 16)); 
        closeBtn.setForeground(COL_TEXT_SEC);
        closeBtn.setBorder(BorderFactory.createLineBorder(COL_TEXT_SEC, 2));
        closeBtn.setPreferredSize(new Dimension(0, 60)); 
        closeBtn.addActionListener(e -> glassPane.setVisible(false));

        btnPanel.add(watchBtn);
        btnPanel.add(closeBtn);

        leftPanel.add(imageLabel, BorderLayout.CENTER);
        leftPanel.add(btnPanel, BorderLayout.SOUTH);

        // --- RIGHT COLUMN: Content ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(0, 50, 0, 0));

        JPanel textHeader = new JPanel(new BorderLayout());
        textHeader.setOpaque(false);
        
        JLabel title = new JLabel(f.name);
        title.setFont(new Font("SansSerif", Font.BOLD, 52));
        title.setForeground(COL_TEXT_PRI);
        
        JLabel loc = new JLabel(f.location.toUpperCase());
        loc.setFont(new Font("SansSerif", Font.BOLD, 24));
        loc.setForeground(COL_ACCENT);
        loc.setBorder(new EmptyBorder(10, 0, 30, 0));
        
        textHeader.add(title, BorderLayout.NORTH);
        textHeader.add(loc, BorderLayout.CENTER);

        JTextArea descArea = new JTextArea(f.description);
        descArea.setFont(new Font("SansSerif", Font.PLAIN, 18)); 
        descArea.setForeground(new Color(220, 220, 220));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setOpaque(false);
        descArea.setEditable(false);
        descArea.setBorder(new EmptyBorder(10, 0, 10, 20));

        JScrollPane scroll = new JScrollPane(descArea);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        styleScrollBar(scroll.getVerticalScrollBar());

        rightPanel.add(textHeader, BorderLayout.NORTH);
        rightPanel.add(scroll, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.40; gbc.weighty = 1.0;
        modalContainer.add(leftPanel, gbc);

        gbc.gridx = 1; 
        gbc.weightx = 0.60;
        modalContainer.add(rightPanel, gbc);

        glassPane.add(modalContainer);
        glassPane.revalidate();
        glassPane.repaint();
        glassPane.setVisible(true);
    }

    // --- HOME PANEL ---
    private void createHomePanel() {
        homePanel = new JPanel(new GridBagLayout());
        homePanel.setBackground(COL_BG);

        JPanel content = new JPanel(new GridLayout(1, 3, 30, 0));
        content.setOpaque(false);
        content.add(createHomeButton("LUZON", "The Northern Isles"));
        content.add(createHomeButton("VISAYAS", "The Central Islands"));
        content.add(createHomeButton("MINDANAO", "The Land of Promise"));

        JLabel title = new JLabel("EXPLORE PHILIPPINES", SwingConstants.CENTER);
        title.setFont(FONT_HEADER);
        title.setForeground(COL_ACCENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0,0,50,0);
        homePanel.add(title, gbc);

        gbc.gridy = 1;
        homePanel.add(content, gbc);
    }

    private JButton createHomeButton(String title, String sub) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(40, 40, 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(COL_ACCENT);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                } else {
                    g2.setColor(new Color(30, 30, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                }
                FontMetrics fmHeader = g2.getFontMetrics(FONT_HEADER);
                FontMetrics fmSub = g2.getFontMetrics(FONT_SUB);
                int hTitle = fmHeader.getAscent();
                int gap = 15;
                int totalH = hTitle + gap + fmSub.getAscent();
                int startY = (getHeight() - totalH) / 2 + hTitle;

                g2.setColor(COL_TEXT_PRI);
                g2.setFont(FONT_HEADER);
                g2.drawString(title, (getWidth() - fmHeader.stringWidth(title)) / 2, startY);

                g2.setColor(COL_TEXT_SEC);
                g2.setFont(FONT_SUB);
                g2.drawString(sub, (getWidth() - fmSub.stringWidth(sub)) / 2, startY + gap + 5);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(280, 350));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> loadRegionsForIsland(title));
        return btn;
    }

    // --- REGION PANEL ---
    private void createRegionPanel() {
        regionPanel = new JPanel(new BorderLayout());
        regionPanel.setBackground(COL_BG);
        
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COL_BG);
        header.setBorder(new EmptyBorder(30, 40, 20, 40));
        
        JButton backBtn = createSimpleButton("← BACK TO ISLANDS");
        backBtn.addActionListener(e -> mainLayout.show(mainPanel, "HOME"));
        
        regionTitle = new JLabel("SELECT REGION", SwingConstants.CENTER);
        regionTitle.setFont(FONT_HEADER);
        regionTitle.setForeground(COL_TEXT_PRI);

        header.add(backBtn, BorderLayout.WEST);
        header.add(regionTitle, BorderLayout.CENTER);
        
        regionGrid = new JPanel(new GridLayout(0, 2, 20, 20)); 
        regionGrid.setBackground(COL_BG);
        regionGrid.setBorder(new EmptyBorder(20, 80, 40, 80)); 
        
        JScrollPane scroll = new JScrollPane(regionGrid);
        scroll.setBorder(null);
        styleScrollBar(scroll.getVerticalScrollBar());

        regionPanel.add(header, BorderLayout.NORTH);
        regionPanel.add(scroll, BorderLayout.CENTER);
    }

    private void loadRegionsForIsland(String islandName) {
        regionTitle.setText(islandName + " REGIONS");
        regionGrid.removeAll(); 
        List<String> regions = islandMap.get(islandName);
        if (regions != null) {
            for (String r : regions) {
                JButton btn = createModernButton(r);
                btn.setPreferredSize(new Dimension(0, 100)); 
                btn.addActionListener(e -> loadFestivalsForRegion(r));
                regionGrid.add(btn);
            }
        }
        regionGrid.revalidate();
        regionGrid.repaint();
        mainLayout.show(mainPanel, "REGION");
    }

    // --- BROWSER PANEL ---
    private void createBrowserPanel() {
        browserPanel = new JPanel(new BorderLayout());
        browserPanel.setBackground(COL_BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COL_BG);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        JButton backBtn = createSimpleButton("← BACK TO REGIONS");
        backBtn.addActionListener(e -> mainLayout.show(mainPanel, "REGION"));

        browserTitle = new JLabel("FESTIVALS", SwingConstants.LEFT);
        browserTitle.setFont(FONT_HEADER);
        browserTitle.setForeground(COL_ACCENT);

        header.add(backBtn, BorderLayout.WEST);
        header.add(browserTitle, BorderLayout.EAST);

        festivalGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 25, 25)); 
        festivalGrid.setBackground(COL_BG);
        festivalGrid.setBorder(new EmptyBorder(20, 30, 40, 30));
        
        JScrollPane scroll = new JScrollPane(festivalGrid);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollBar(scroll.getVerticalScrollBar());

        browserPanel.add(header, BorderLayout.NORTH);
        browserPanel.add(scroll, BorderLayout.CENTER);
    }

    private void loadFestivalsForRegion(String regionName) {
        browserTitle.setText(regionName);
        festivalGrid.removeAll();

        List<Festival> fests = regionMap.get(regionName);
        if (fests != null) {
            for (Festival f : fests) {
                festivalGrid.add(new FestivalCard(f));
            }
        }
        
        festivalGrid.revalidate();
        festivalGrid.repaint();
        
        mainLayout.show(mainPanel, "BROWSER");
        SwingUtilities.invokeLater(() -> {
            JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, festivalGrid);
            if(scroll != null) scroll.getVerticalScrollBar().setValue(0);
        });
    }

    private void openLink(String videoId) {
        try { Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=" + videoId)); } 
        catch(Exception ex) {} 
    }

    // --- CUSTOM COMPONENTS ---

    private JButton createModernButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(COL_ACCENT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    setForeground(Color.WHITE);
                } else {
                    g2.setColor(new Color(30, 30, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    setForeground(COL_TEXT_SEC);
                }
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(FONT_SUB);
        btn.setForeground(COL_TEXT_SEC);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createSimpleButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(COL_TEXT_SEC);
        btn.setBackground(COL_BG);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { btn.setForeground(COL_TEXT_SEC); }
        });
        return btn;
    }

    // --- FESTIVAL CARD (Fixed Size + Internal Zoom) ---
    class FestivalCard extends JPanel {
        private Festival festival;
        private Image thumb;
        private float zoom = 1.0f;
        private Timer timer;
        private boolean hover = false;
        
        // FIXED SIZE - Won't stretch or shrink
        private static final int W = 250;
        private static final int H = 210;

        public FestivalCard(Festival f) {
            this.festival = f;
            setPreferredSize(new Dimension(W, H)); 
            setBackground(COL_BG);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            new Thread(() -> {
                try {
                    URL url = new URL(f.getThumbUrl());
                    BufferedImage img = ImageIO.read(url);
                    if(img != null) {
                        thumb = img; 
                        SwingUtilities.invokeLater(this::repaint);
                    }
                } catch(Exception e) {}
            }).start();

            // Smooth Hover Animation
            timer = new Timer(15, e -> {
                float target = hover ? 1.1f : 1.0f; // Zoom in 10%
                if(Math.abs(zoom - target) > 0.001f) {
                    zoom += (target - zoom) * 0.15f; 
                    repaint();
                } else timer.stop();
            });

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true; timer.start(); }
                public void mouseExited(MouseEvent e) { hover = false; timer.start(); }
                public void mouseClicked(MouseEvent e) { showFestivalModal(f); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth();
            int h = getHeight();
            
            // --- DRAWING LOGIC (Internal Zoom only) ---
            // Calculate scale based on hover zoom factor
            int sw = (int)(w * zoom);
            int sh = (int)(h * zoom);
            int sx = (w - sw) / 2;
            int sy = (h - sh) / 2;

            // Clip prevents drawing outside the rounded card
            RoundRectangle2D rect = new RoundRectangle2D.Float(0, 0, w, h, 15, 15);
            g2.setClip(rect);

            g2.setColor(new Color(40,40,40));
            g2.fillRect(0, 0, w, h);

            if(thumb != null) {
                g2.drawImage(thumb, sx, sy, sw, sh, null);
            }

            GradientPaint gp = new GradientPaint(0, h * 0.4f, new Color(0,0,0,0), 0, h, new Color(0,0,0,240));
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            g2.setColor(Color.WHITE);
            g2.setFont(FONT_CARD);
            g2.drawString(festival.name, 15, h - 40);
            
            g2.setColor(COL_TEXT_SEC);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString(festival.location, 15, h - 20);

            if(hover) {
                // Dim on hover
                g2.setColor(new Color(0,0,0,40));
                g2.fillRect(0,0,w,h);
                
                int is = 44;
                g2.setColor(COL_ACCENT);
                g2.fillOval((w-is)/2, (h-is)/2, is, is);
                g2.setColor(Color.WHITE);
                Polygon p = new Polygon();
                int cx = w/2, cy = h/2;
                p.addPoint(cx-6, cy-9);
                p.addPoint(cx-6, cy+9);
                p.addPoint(cx+9, cy);
                g2.fillPolygon(p);
                
                // Border
                g2.setClip(null);
                g2.setColor(new Color(255,255,255,100));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, w-1, h-1, 15, 15);
            }
        }
    }

    // --- DATA ---
    static class Festival {
        String name, location, videoId, description;
        public Festival(String n, String l, String v, String d) { 
            name = n; location = l; videoId = v; description = d;
        }
        String getThumbUrl() { return "https://img.youtube.com/vi/" + videoId + "/mqdefault.jpg"; }
    }

    private void buildDatabase() {
        // --- LUZON ---
        List<String> luzonRegions = Arrays.asList(
            "Region I – Ilocos Region", "Region II – Cagayan Valley", "Region III – Central Luzon",
            "Region IV-A – CALABARZON", "Region IV-B – MIMAROPA", "Region V – Bicol Region", "NCR – National Capital Region"
        );
        islandMap.put("LUZON", luzonRegions);

        addFest("Region I – Ilocos Region", "Pamulinawen", "Laoag", "jJjzU3WjUXE"); 
        addFest("Region I – Ilocos Region", "Empanada Festival", "Batac", "8nG8zIjzNMU");
        addFest("Region I – Ilocos Region", "Tobacco Festival", "Ilocos Norte", "xj6dJO9lFOk");
        addFest("Region I – Ilocos Region", "Vigan Longganisa", "Vigan", "lDcNPg_CIkM");
        addFest("Region I – Ilocos Region", "Basi Festival", "Ilocos Sur", "v5anutxgUhQ");
        addFest("Region I – Ilocos Region", "Bangus Festival", "Dagupan", "H5KmIyz2URg");

        addFest("Region II – Cagayan Valley", "Vakul-Kanayi", "Batanes", "R4PuTlFvU60");
        addFest("Region II – Cagayan Valley", "Bangkarera", "Cagayan", "GmWow6faV54");
        addFest("Region II – Cagayan Valley", "Bambanti Festival", "Isabela", "mv8SHD-jvKk"); 
        addFest("Region II – Cagayan Valley", "Ammungan Festival", "Nueva Vizcaya", "24Si0T49dJw");

        addFest("Region III – Central Luzon", "Singkaban Festival", "Bulacan", "JG2mXrgIPn4");
        addFest("Region III – Central Luzon", "Taong Putik", "Nueva Ecija", "FsnowhE9EiM");
        addFest("Region III – Central Luzon", "Kuraldal Festival", "Pampanga", "uJchn9VWIIE");
        addFest("Region III – Central Luzon", "Mango Festival", "Zambales", "8HDQkkMDC3k");
        addFest("Region III – Central Luzon", "Araw ng Kagitingan", "Bataan", "cza8OPV4jl4");

        addFest("Region IV-A – CALABARZON", "Pahiyas Festival", "Quezon", "VL7BgXcZHd4");
        addFest("Region IV-A – CALABARZON", "Higantes Festival", "Rizal", "yS6n_yguzm4");
        addFest("Region IV-A – CALABARZON", "Sublian Festival", "Batangas", "i5uwL284lCg");
        addFest("Region IV-A – CALABARZON", "Turumba Festival", "Laguna", "ORK0wSDX_0M");
        addFest("Region IV-A – CALABARZON", "Maytinis Festival", "Cavite", "ERPuWkb_Ung");

        addFest("Region IV-B – MIMAROPA", "Moriones Festival", "Marinduque", "q0QHQ_fD77Q");
        addFest("Region IV-B – MIMAROPA", "Baragatan", "Palawan", "bky8Tpx62QA");
        addFest("Region IV-B – MIMAROPA", "Biniray Festival", "Romblon", "M2BtwkNkIGo");
        addFest("Region IV-B – MIMAROPA", "Sandugo (Mindoro)", "Oriental Mindoro", "s-Hy-DM72Ws");

        addFest("Region V – Bicol Region", "Peñafrancia", "Camarines Sur", "KJzJDVdtvfc");
        addFest("Region V – Bicol Region", "Magayon Festival", "Albay", "hPXKBa0CMeg");
        addFest("Region V – Bicol Region", "Rodeo Masbateño", "Masbate", "DGYRw1gRowY");
        addFest("Region V – Bicol Region", "Abaca Festival", "Catanduanes", "O4xTJVEISxw");
        addFest("Region V – Bicol Region", "Pinyasan Festival", "Cam Norte", "KWh4YC2JfMY");

        addFest("NCR – National Capital Region", "Aliwan Fiesta", "Pasay", "G4ttiW03CoE");
        addFest("NCR – National Capital Region", "Black Nazarene", "Quiapo, Manila", "GDsZTirLLCM");
        addFest("NCR – National Capital Region", "Flores de Mayo", "Manila", "b4ff5sVreVY");

        // --- VISAYAS ---
        List<String> visRegions = Arrays.asList(
            "Region VI – Western Visayas", "Region VII – Central Visayas", "Region VIII – Eastern Visayas"
        );
        islandMap.put("VISAYAS", visRegions);

        addFest("Region VI – Western Visayas", "Ati-Atihan", "Aklan", "0GddP5BdwFE");
        addFest("Region VI – Western Visayas", "Dinagyang", "Iloilo", "ONSZdKl_r-o");
        addFest("Region VI – Western Visayas", "Manggahan", "Guimaras", "rALQ_uJyZEM");
        addFest("Region VI – Western Visayas", "Binirayan", "Antique", "vvXfACw2wV8");
        addFest("Region VI – Western Visayas", "Sinadya sa Halaran", "Capiz", "a67FnX2bV6E");

        addFest("Region VII – Central Visayas", "Sinulog Festival", "Cebu", "eas0BV09CTM");
        addFest("Region VII – Central Visayas", "Sandugo Festival", "Bohol", "GTnreV6MG_4");
        addFest("Region VII – Central Visayas", "Buglasan", "Negros Oriental", "47Vocz_63TM");
        addFest("Region VII – Central Visayas", "Dilaab Festival", "Siquijor", "4WB8JwxuPGw");

        addFest("Region VIII – Eastern Visayas", "Pintados", "Leyte", "MVB79qb2PYA");
        addFest("Region VIII – Eastern Visayas", "Sangyaw Festival", "Tacloban", "cGj-Hj0S6hU");
        addFest("Region VIII – Eastern Visayas", "Padul-ong", "Biliran", "pyXjKso169s");
        addFest("Region VIII – Eastern Visayas", "Karadyaan", "Eastern Samar", "jMaGSk07MRs");

        // --- MINDANAO ---
        List<String> minRegions = Arrays.asList(
            "Region IX – Zamboanga Peninsula", "Region X – Northern Mindanao", "Region XI – Davao Region",
            "Region XII – SOCCSKSARGEN", "Region XIII – Caraga", "BARMM"
        );
        islandMap.put("MINDANAO", minRegions);

        addFest("Region IX – Zamboanga Peninsula", "Hermosa Festival", "Zamboanga City", "bLK8cFY4y4A");
        addFest("Region IX – Zamboanga Peninsula", "Sinulog sa Dipolog", "Zambo del Norte", "10w0X2sgWEQ");
        addFest("Region IX – Zamboanga Peninsula", "Buklog Festival", "Subanen", "e-T2Suod5RE");

        addFest("Region X – Northern Mindanao", "Kaamulan Festival", "Bukidnon", "kfddbH8EGJg");
        addFest("Region X – Northern Mindanao", "Lanzones Festival", "Camiguin", "rR7V4koglsU");
        addFest("Region X – Northern Mindanao", "Diyandi Festival", "Iligan", "X-Fd24DvUL8");
        addFest("Region X – Northern Mindanao", "Kuyamis Festival", "Cagayan de Oro", "6kTmJnnA4fE");

        addFest("Region XI – Davao Region", "Kadayawan", "Davao City", "pJarYUosq80");
        addFest("Region XI – Davao Region", "Bulawan Festival", "Davao de Oro", "7vOpvcXSyxs");

        addFest("Region XII – SOCCSKSARGEN", "T’nalak Festival", "South Cotabato", "DIBU51_MfdU");
        addFest("Region XII – SOCCSKSARGEN", "Kalivungan", "Cotabato", "SFTaxi02QFU");
        addFest("Region XII – SOCCSKSARGEN", "MunaTo Festival", "Sarangani", "NfuOf3MXp8E");
        addFest("Region XII – SOCCSKSARGEN", "Pasaka Festival", "Sultan Kudarat", "W6t9s131ybM");

        addFest("Region XIII – Caraga", "Bonok-Bonok", "Surigao", "4Ad43Y2tuZo");
        addFest("Region XIII – Caraga", "Kahimunan", "Butuan", "UteURfijsMI");
        addFest("Region XIII – Caraga", "Balangay", "Agusan", "Q8geN-PIm2E");

        addFest("BARMM", "Shariff Kabunsuan", "Maguindanao", "FKfVb0KVNPg");
        addFest("BARMM", "Lami-Lamihan", "Basilan", "k7Y1xHg0nj4");
        addFest("BARMM", "Agal-Agal", "Tawi-Tawi", "VSgtgPlFLas");
        addFest("BARMM", "Araw ng Lanao", "Lanao del Sur", "kFpkwNQ0Fg");
    }

    private void addFest(String region, String name, String loc, String vid) {
        String desc = getRealHistory(name);
        regionMap.computeIfAbsent(region, k -> new ArrayList<>()).add(new Festival(name, loc, vid, desc));
    }

    private String getRealHistory(String name) {
        switch (name) {
            case "Pamulinawen": return 
                "Celebrated Annually: February 10 (Feast Day)\n" +
                "Established: 1598 (Feast of St. William)\n\n" +
                "The Pamulinawen Festival honors St. William the Hermit, the patron saint of Laoag City. The festival's name is derived from 'Pamulinawen', a popular Ilocano folk song about a stone-hearted girl who cannot be easily wooed. This song symbolizes the character of the Ilocano people: hard to get but fiercely loyal once won over.\n\n" +
                "The origins of the celebration date back to the Spanish colonization in 1598 when the Augustinian friars established the parish of St. William. Over the centuries, what began as a strictly religious feast day evolved into a cultural spectacle that highlights the unique heritage of the Ilocos region.\n\n" +
                "A key feature of the festival is the 'Dulay' or floral offering, which has its roots in pre-colonial rituals. Indigenous people would offer flowers and fruits to the spirits of nature, a practice that was later adapted to honor the patron saint.\n\n" +
                "During the festival, the streets of Laoag come alive with the 'Calesa' parade. The horse-drawn carriages, which were the primary mode of transport during the Spanish era, are decorated with colorful flowers and fabrics, showcasing the artistry of the local kutseros.\n\n" +
                "The Street Dancing competition is another highlight, where contingents from various barangays perform movements inspired by Ilocano traditions. They often incorporate props made from local materials like Abel Iloco fabric and bamboo.\n\n" +
                "Historically, the festival also served as a time for reconciliation. It was believed that feuds between families should be settled before the feast day of St. William, ensuring peace and unity within the community.\n\n" +
                "In recent years, the local government has expanded the celebration to include the 'Miss Laoag' beauty pageant and a massive food fair featuring Ilocano delicacies like Bagnet and Empanada. This modernization helps attract younger generations and tourists.\n\n" +
                "Today, the Pamulinawen Festival stands as a testament to the resilience and strong faith of the Ilocanos. It effectively bridges the gap between the city's colonial past and its vibrant present, ensuring that local traditions remain alive for future generations.";

            case "Empanada Festival": return 
                "Celebrated Annually: June 23 (Batac Charter Day)\n" +
                "Established: 2007 (First Celebration)\n\n" +
                "The Empanada Festival is a flavorful celebration held in Batac City, Ilocos Norte, known as the 'Home of the Great Leaders'. It honors the city’s most famous culinary export, the Batac Empanada, which is distinct for its bright orange color derived from achuete oil.\n\n" +
                "Established in 2007 as part of the Charter Day festivities, the festival highlights the importance of the empanada industry to the local economy. It showcases how a simple street food, made from rice flour, mongo, papaya, and longganisa, became a symbol of the city’s identity.\n\n" +
                "The main event is the Street Dancing parade, where dancers wear costumes inspired by the ingredients of the empanada. Their choreography often depicts the entire process of making the delicacy, from planting the crops to frying the final product.\n\n" +
                "Unlike other festivals that focus on religious figures, this celebration is purely cultural and economic. It serves as a thanksgiving for the livelihood provided by the local food industry, which supports hundreds of families in Batac.\n\n" +
                "Competitions for the 'Best Empanada Maker' are fiercely contested. Locals and tourists gather to watch experts fold and fry the empanadas with lightning speed, judging them on crispiness, filling ratio, and authentic taste.\n\n" +
                "The festival also emphasizes the agricultural roots of the ingredients. It promotes the farming of local crops like papaya and mongo beans, ensuring that the supply chain remains sustainable and benefits local farmers.\n\n" +
                "Music and dance play a crucial role in the festivities. The rhythm of the drums mimics the fast-paced action of the busy empanadaan stalls, creating an energetic atmosphere that fills the city streets.\n\n" +
                "Over the years, the Empanada Festival has put Batac on the gastronomic map of the Philippines. It invites everyone to taste not just the food, but the rich history and hard work that goes into every bite.";

            case "Tobacco Festival": return 
                "Celebrated Annually: March 25-28 (Candon City Fiesta)\n" +
                "Established: 2001 (Institutionalized as Tobacco Festival)\n\n" +
                "The Tobacco Festival is the pride of Candon City, Ilocos Sur, known as the 'Tobacco Capital of the Philippines'. It is celebrated every March to coincide with the city's fiesta and the commemoration of the historic 'Cry of Candon'.\n\n" +
                "The festival was institutionalized in 2001 to honor the Virginia tobacco industry, which has been the economic backbone of the city for decades. It serves as a thanksgiving for the bountiful harvest that supports thousands of tobacco farmers and their families.\n\n" +
                "Historically, the festival is linked to the 'Cry of Candon' (Ikkis ti Candon) on March 25, 1898. This was a revolutionary uprising led by Don Isabelo Abaya against the Spanish colonizers, marking Candon as a center of resistance and patriotism.\n\n" +
                "Street dancing is a major highlight, where performers don costumes made from dried tobacco leaves. The choreography often blends themes of agricultural labor with the revolutionary spirit of the Katipunan, creating a unique narrative performance.\n\n" +
                "One of the most anticipated events is the 'Miss Tobacco Philippines' pageant. It is not just a beauty contest but a platform to promote the industry and advocate for the welfare of the tobacco farming communities.\n\n" +
                "The festival also features a trade fair showcasing other local products, particularly the famous Candon Calamay (rice cake). This sweet delicacy complements the agricultural theme, highlighting the diverse produce of the region.\n\n" +
                "Competitions like tobacco leaf tying and stringing are held to celebrate the skills of the farmers. These events turn mundane agricultural tasks into exciting contests, fostering a sense of pride in the local livelihood.\n\n" +
                "Today, the Tobacco Festival stands as a dual celebration of economic resilience and historical valor. It reminds the people of Candon that their prosperity is rooted in both the soil they till and the freedom their ancestors fought for.";

            case "Sinulog Festival": return 
                "Celebrated Annually: 3rd Sunday of January\n" +
                "Established: 1980 (First organized parade)\n\n" +
                "The Sinulog Festival is Cebu's biggest and most colorful celebration in honor of the Señor Santo Niño. The name comes from the Cebuano word 'sulog', meaning 'like water current movement', which describes the forward-backward dance step used during the prayer dance.\n\n" +
                "The festival commemorates the Filipino people's acceptance of Christianity. Its origin dates back to 1521 when Portuguese explorer Ferdinand Magellan arrived in Cebu and gave the image of the Santo Niño as a baptismal gift to Hara Amihan (Queen Juana) and Rajah Humabon.\n\n" +
                "For centuries, the dance was a small, solemn ritual performed by candle vendors at the Basilica Minore del Santo Niño. It wasn't until 1980 that David Odilao Jr., then Regional Director of the Ministry of Sports and Youth Development, organized the first Sinulog parade to distinguish Cebu's festival from the Ati-Atihan.\n\n" +
                "The 'Pit Senyor!' chant heard throughout the festival is short for 'Sangpit sa Señor', which means 'to call upon the Lord'. Devotees shout this phrase while dancing as a form of supplication and thanksgiving for prayers answered.\n\n" +
                "A significant pre-festival event is the 'Fluvial Procession', where the image of the Santo Niño is carried on a galleon-inspired boat from Mandaue City to Cebu City. This reenacts the arrival of the Spaniards and the introduction of the Christian faith.\n\n" +
                "The Grand Parade is the climax of the festival, featuring massive floats and street dancers in vibrant costumes. The dancers move to the rhythm of drums, trumpets, and native gongs, creating a hypnotic beat that energizes the millions of spectators lining the streets.\n\n" +
                "Beyond the religious aspect, Sinulog has become a massive cultural and economic engine for Cebu. It includes trade fairs, choral competitions, and film festivals, showcasing the diverse talents of the Cebuanos.\n\n" +
                "Today, Sinulog is recognized globally, attracting tourists from all over the world. It remains a profound expression of faith, history, and the enduring spirit of the Cebuano people.";

            case "MassKara Festival": return 
                "Celebrated Annually: 4th Sunday of October\n" +
                "Established: 1980\n\n" +
                "The MassKara Festival is a modern festival born out of tragedy and crisis. It was established in 1980 during a period when the province of Negros Occidental was suffering from an economic crisis due to the plummeting price of sugar, its primary crop.\n\n" +
                "To make matters worse, on April 22, 1980, the inter-island vessel MV Don Juan collided with a tanker and sank, claiming roughly 700 lives, many of whom were prominent families from Bacolod City. The city was engulfed in a cloud of gloom and hopelessness.\n\n" +
                "In an effort to lift the spirits of the people, the local government and artists decided to organize a 'festival of smiles'. They created the concept of MassKara, a portmanteau of 'mass' (a multitude of people) and the Spanish word 'cara' (face).\n\n" +
                "The festival features street dancers wearing smiling masks, symbolizing the resilience of the Negrenses. The message was clear: no matter how tough life gets, the people of Bacolod will survive and they will do so with a smile.\n\n" +
                "Originally, the masks were influenced by the Rio Carnival and were hand-painted with native motifs. Over the decades, the designs have evolved to become incredibly ornate, featuring LED lights, feathers, and intricate beadwork.\n\n" +
                "The street dancing competition is a high-energy event where barangays compete for prestige. The choreography mixes Latin-inspired beats with traditional Filipino folk movements, creating a unique cultural fusion.\n\n" +
                "The MassKara Festival has successfully transformed Bacolod's image from a struggling sugar capital to the 'City of Smiles'. It is now one of the most famous festivals in the Philippines, drawing huge crowds and international media attention.\n\n" +
                "More than just a party, MassKara is a story of survival. It stands as a powerful reminder of how art and community spirit can help a people overcome their darkest hours.";

            case "Ati-Atihan": return 
                "Celebrated Annually: 3rd Sunday of January\n" +
                "Established: ~1200s (Folk origin) / 1700s (Christian adaptation)\n\n" +
                "Known as the 'Mother of All Philippine Festivals', the Ati-Atihan is held in Kalibo, Aklan. It honors the Santo Niño but its roots are deeply embedded in pre-colonial history, commemorating the peace pact between the indigenous Ati people and the Malay settlers.\n\n" +
                "According to the legend of the 'Barter of Panay', ten Bornean datus arrived in Panay in the 13th century escaping a tyrant. They bought the lowlands from the Ati King Marikudo in exchange for a golden salakot and jewelry. To celebrate, the Malays painted their faces black to look like the Ati.\n\n" +
                "When the Spaniards arrived, they incorporated the feast of the Santo Niño into the existing celebration. This allowed the indigenous tradition to survive under the guise of a Catholic feast, creating a unique syncretic festival.\n\n" +
                "Unlike other festivals where spectators watch from the sidelines, Ati-Atihan is famous for its 'sadsad' or street dancing where everyone is invited to join. There are no barriers between the performers and the crowd.\n\n" +
                "Participants cover their bodies with soot and wear colorful, indigenous-inspired costumes made of indigenous materials like abaca, shells, and feathers. They dance to the rhythmic and repetitive beat of drums and lyres.\n\n" +
                "The cry 'Hala Bira! Puera Pasma!' is central to the event. It translates to 'Pour it on! Keep away sickness!', reflecting the belief that the frantic dancing is a form of prayer and protection against illness.\n\n" +
                "The festival culminates on Sunday with a religious procession of the Santo Niño images. It creates a stark contrast to the wild street partying, grounding the event back to its religious significance.\n\n" +
                "Ati-Atihan has inspired many other festivals in the Philippines, including the Sinulog and Dinagyang. However, it remains unique for its raw, communal energy and its direct link to the country's pre-colonial past.";

            case "Pahiyas Festival": return 
                "Celebrated Annually: May 15\n" +
                "Established: 16th Century (Spanish Era)\n\n" +
                "The Pahiyas Festival is one of the Philippines' most colorful harvest festivals, held in Lucban, Quezon. It is celebrated in honor of San Isidro Labrador, the patron saint of farmers, as a thanksgiving for a bountiful harvest.\n\n" +
                "The festival's name comes from the word 'payas', which means to decorate. Residents adorn the facades of their homes with fresh fruits, vegetables, and the famous 'kiping'—leaf-shaped rice wafers dyed in brilliant colors.\n\n" +
                "Its origins date back to the 16th century when farmers would bring their harvest to the church for blessing. Over time, the church could no longer accommodate the volume of produce, so the friars advised farmers to display their harvest in front of their homes for the priest to bless as he passed by.\n\n" +
                "The Kiping is the visual centerpiece of the festival. Made from ground rice and water, it is steamed in molds made from real leaves (like cacao or coffee) to capture the texture, then dried and dyed. It is edible and can be grilled or fried.\n\n" +
                "Every year, the festival route changes to give different households the chance to participate. This encourages creativity and community involvement, as neighbors compete to have the most elaborate house decoration.\n\n" +
                "The 'Kalabaw' or carabao parade is another highlight, showcasing the farmers' trusted companions. The animals are often dressed up or pulling carts laden with produce, symbolizing the hard work of the agricultural sector.\n\n" +
                "Culinary tourism is a huge part of Pahiyas. Visitors flock to Lucban to taste the local longganisa (garlic sausage) and pansit habhab, a noodle dish eaten straight from a banana leaf without utensils.\n\n" +
                "Pahiyas is a vibrant fusion of art, faith, and agriculture. It serves as a beautiful reminder of the deep connection between the Filipino people, their land, and their gratitude for nature's blessings.";

            case "Dinagyang": return 
                "Celebrated Annually: 4th Sunday of January\n" +
                "Established: 1968\n\n" +
                "Dinagyang is Iloilo City's premier religious and cultural festival, held in honor of the Santo Niño and to commemorate the arrival of Malay settlers on Panay Island. It is famous for its fast-paced, highly choreographed street dancing involving various 'tribes'.\n\n" +
                "The festival began in 1968 when a replica of the Santo Niño de Cebu was brought to Iloilo by Fr. Sulpicio Enderez. The local parish of San Jose started a small celebration which eventually grew into the massive spectacle seen today.\n\n" +
                "The name 'Dinagyang' was coined in 1977 by broadcaster Pacifico Sudario. It is derived from the Hiligaynon word 'dagyang', meaning merrymaking or revelry, perfectly capturing the spirit of the event.\n\n" +
                "Unlike the free-flowing Ati-Atihan, Dinagyang is known for its precision. The participating tribes, often composed of students, undergo months of rigorous training to perfect their energetic routines and drum beats.\n\n" +
                "The performers paint their skin dark brown or black to represent the Aetas (Ati), the indigenous people of Panay. Their costumes are elaborate, using indigenous materials like feathers, beads, and woven grass to create stunning visual effects.\n\n" +
                "The 'Dagoy' mascot, a friendly Aeta warrior, has become the symbol of the festival. Standing at 6 feet 9 inches, Dagoy represents the hospitality and joy of the Ilonggo people.\n\n" +
                "The festival is divided into three major events: the Ati Tribe Competition, the Kasadyahan Cultural Parade, and the Miss Iloilo Dinagyang beauty pageant. Each event draws massive crowds and showcases different aspects of Ilonggo culture.\n\n" +
                "Dinagyang has been inducted into the Association of Tourism Officers of the Philippines (ATOP) Hall of Fame. It is widely regarded as the 'Queen of All Festivals' in the Philippines due to its world-class performances and organization.";

            case "Kadayawan": return 
                "Celebrated Annually: 3rd Week of August\n" +
                "Established: 1986 (as Apo Duwaling)\n\n" +
                "Kadayawan is Davao City's thanksgiving festival for nature's gifts, the wealth of culture, and the bounties of harvest. Unlike many other major Philippine festivals, it is non-religious and focuses purely on cultural heritage and thanksgiving.\n\n" +
                "The festival began in 1986 as the 'Apo Duwaling' festival, named after three icons of Davao: Mount Apo, the Durian, and the Waling-waling orchid. It was created to unite the Davaoeños after the turbulent martial law era.\n\n" +
                "In 1988, it was renamed 'Kadayawan sa Dabaw' by then-Mayor Rodrigo Duterte. The name comes from the Mandaya word 'madayaw', meaning valuable, good, or beautiful, reflecting the city's abundance.\n\n" +
                "The festival honors the 11 indigenous tribes of Davao City: the Bagobo-Klata, Bagobo-Tagabawa, Matigsalug, Maranao, Maguindanaon, Bahobo-Manobo, Ovu-Manobo, Sama, Tausug, Iranun, and Kagan. It serves as a platform for them to showcase their distinct traditions.\n\n" +
                "A major highlight is the 'Indak-Indak sa Kadalanan' (Street Dancing), where contingents from all over Mindanao perform in colorful tribal regalia. The dances tell stories of harvest, war, and rituals, accompanied by the beating of kulintang and agong instruments.\n\n" +
                "The 'Pamulak sa Kadayawan' is the floral float parade, rivaling the beauty of Baguio's Panagbenga. The floats showcase the incredible flora of the region, often featuring real fruits and vegetables that spectators can sometimes catch.\n\n" +
                "Fruit is central to the celebration. During August, Davao is overflowing with durian, mangosteen, rambutan, and lanzones. Prices drop significantly, and fruit stands line the streets, making it a paradise for food lovers.\n\n" +
                "Kadayawan is a celebration of life and diversity. It stands as a powerful example of how different tribes, religions, and settlers can live together in harmony and shared prosperity.";

            case "Moriones Festival": return 
                "Celebrated Annually: Holy Week (March/April)\n" +
                "Established: 1870s (Attributed to Fr. Dionisio Santiago)\n\n" +
                "The Moriones Festival is a unique folk-religious event held in Marinduque during Holy Week. It centers on the reenactment of the story of Saint Longinus, the Roman centurion who pierced the side of the crucified Christ.\n\n" +
                "The term 'Morion' refers to the helmet worn by the Roman soldiers. Locals, known as 'Moriones', dress in colorful Roman costumes and wear hand-carved wooden masks depicting fierce expressions to mimic the soldiers.\n\n" +
                "According to the legend, Longinus was blind in one eye. When Christ's blood splattered on his face, his vision was miraculously restored. This led to his conversion to Christianity and subsequent martyrdom.\n\n" +
                "For seven days, the Moriones roam the streets, playing pranks on children and engaging the public. This penitential act is often done as a 'panata' (vow) in exchange for a miracle or as an act of atonement.\n\n" +
                "The highlight of the festival is the 'Pugutan' (Beheading) held on Easter Sunday. It is a theatrical presentation where Longinus is captured and beheaded for his faith, symbolizing the triumph of Christianity.\n\n" +
                "The masks used are works of art in themselves, carved from dapdap or santol wood. They are passed down through generations or crafted by local artisans, making Marinduque a hub for traditional mask-making.\n\n" +
                "Unlike the festive atmosphere of other fiestas, Moriones maintains a solemn yet theatrical tone appropriate for Lent. It involves the entire community, with towns becoming open-air stages for the passion play.\n\n" +
                "The Moriones Festival is one of the oldest religious festivals in the Philippines. It provides a visceral, tangible connection to the country's Catholic heritage and the dramatic flair of its folk traditions.";

            default: return 
                "Celebrated Annually: Varies by Region\n" +
                "Established: Pre-colonial to Modern Era\n\n" +
                "This festival is a significant cultural event in " + name + ", celebrated to honor local traditions, patron saints, or historical milestones. Like many Philippine festivals, it serves as a gathering for the community to showcase their unique heritage through dance, music, and food.\n\n" +
                "The origins of such festivals often trace back to animistic rituals of thanksgiving for a good harvest. Indigenous communities would offer dance and song to the spirits of nature, asking for protection and abundance.\n\n" +
                "With the arrival of the Spanish, these rituals were Christianized. The dates were often moved to coincide with the feast day of a Catholic patron saint, allowing the church to integrate into the daily lives of the natives.\n\n" +
                "A common feature is the street parade, where participants wear elaborate costumes reflecting the local flora, fauna, or historical figures. These costumes are often handmade by local artisans, showcasing the craftsmanship of the region.\n\n" +
                "Music plays a vital role, with traditional instruments like gongs and kulintang often blending with modern drum and bugle corps. This creates a soundscape that is distinctively Filipino—a mix of the old and the new.\n\n" +
                "Food is another pillar of the celebration. Homes are thrown open to guests, and local delicacies are prepared in abundance. This tradition of 'open house' hospitality is a hallmark of Filipino fiesta culture.\n\n" +
                "In recent decades, local governments have professionalized these festivals to boost tourism. Competitions for street dancing and float design have raised the artistic standards, attracting visitors from across the globe.\n\n" +
                "Ultimately, festivals like this act as a social glue. They strengthen community bonds, instill pride in local identity, and ensure that the stories of the ancestors are passed down to the younger generation.";
        }
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setBackground(new Color(20,20,20));
        sb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = new Color(60, 60, 60);
                this.trackColor = new Color(20, 20, 20);
            }
            @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        });
    }

    private JButton createZeroButton() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        return b;
    }
    
    // --- WRAP LAYOUT HELPER ---
    class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override public Dimension minimumLayoutSize(Container target) { return layoutSize(target, false); }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
                int rowWidth = 0, rowHeight = 0, y = 0;
                for (Component m : target.getComponents()) {
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) { y += rowHeight + vgap; rowWidth = 0; rowHeight = 0; }
                        rowWidth += d.width + hgap;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                return new Dimension(targetWidth, y + rowHeight + vgap + insets.top + insets.bottom);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StreamFestives().setVisible(true));
    }
}