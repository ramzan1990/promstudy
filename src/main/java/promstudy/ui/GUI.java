package promstudy.ui;

import promstudy.main.PromStudy;
import promstudy.managers.GUIManager;
import promstudy.visualization.DataComponent;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class GUI extends PSFrame {

    private static final int panelMargin = 6;
    public JTextArea consoleArea;
    private JPanel dataSelectPanel, consolePanel, dataTransformPanel, dataPanel;
    private JToolBar toolsPanel;
    private JDesktopPane workSpacePanel;
    private GUIManager manager;


    public GUI(final GUIManager manager) {
        this.manager = manager;
        // <editor-fold defaultstate="collapsed" desc="menu">
        JMenuBar menu = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenu helpM = new JMenu("Help");
        JMenu options = new JMenu("Options");
        JMenu optionsGraphics = new JMenu("Graphics");
        JMenu data = new JMenu("Data");
        JMenu window = new JMenu("Window");
        JMenu analysis = new JMenu("Analysis");
        JMenuItem chooseBins = new JMenuItem("Choose Bins");
        JMenuItem analyse = new JMenuItem("Analyse");
        JMenuItem chooseStep = new JMenuItem("Choose Step");
        JMenuItem exit = new JMenuItem("Exit");
        JMenuItem loadData = new JMenuItem("Load Test Data");
        JMenuItem loadLData = new JMenuItem("Load Sequences");
        JMenuItem dataSummary= new JMenuItem("Data Summary");
        JMenuItem predict = new JMenuItem("Predict");
        JMenuItem accuracyHistogram = new JMenuItem("Accuracy Histogram");
        JMenuItem  Help = new JMenuItem("Help");
        JMenuItem About = new JMenuItem("About");
        JMenuItem ClearConsole = new JMenuItem("Clear console");
        final JCheckBoxMenuItem HideDataPanel = new JCheckBoxMenuItem("Data Panel");
        final JCheckBoxMenuItem HideTools = new JCheckBoxMenuItem("Tools Panel");
        final JCheckBoxMenuItem HideConsole = new JCheckBoxMenuItem("Console");
        final JCheckBoxMenuItem whiteTheme = new JCheckBoxMenuItem("White Theme");
        final JCheckBoxMenuItem ROCCurve = new JCheckBoxMenuItem("ROCCurve");
        JMenuItem Tile = new JMenuItem("Tile");
        JMenuItem Cascade = new JMenuItem("Cascade");

        options.add(optionsGraphics);
        optionsGraphics.add(whiteTheme);
        optionsGraphics.add(chooseBins);
        chooseBins.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manager.chooseBins();
            }
        });
        whiteTheme.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manager.setTheme(whiteTheme.isSelected());
            }
        });
        options.add(chooseStep);
        chooseStep.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manager.chooseStep();
            }
        });
        options.add(ROCCurve);
        ROCCurve.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manager.ROCCurve(ROCCurve.isSelected());
            }
        });
        ROCCurve.setState(true);

        file.add(exit);
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                System.exit(0);
            }
        });
        analysis.add(predict);
        analysis.add(accuracyHistogram);
        analysis.addSeparator();
        analysis.add(analyse);

        analyse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manager.analyse();
            }
        });
        data.add(loadData);
        data.add(loadLData);
        data.add(dataSummary);


        loadLData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manager.io.loadLData();

            }
        });
        loadData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manager.io.loadData();

            }
        });
        dataSummary.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manager.dataSummary();
            }
        });

        predict.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manager.predict();
            }
        });


        accuracyHistogram.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manager.accuracyHistogram();
            }
        });

        HideConsole.setSelected(true);
        HideConsole.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (HideConsole.isSelected()) {
                    consolePanel.setVisible(true);
                } else {
                    consolePanel.setVisible(false);
                }
                repaint();
            }
        });
        HideTools.setSelected(true);
        HideTools.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (HideTools.isSelected()) {
                    toolsPanel.setVisible(true);
                } else {
                    toolsPanel.setVisible(false);
                }
                repaint();
            }
        });
        HideDataPanel.setSelected(true);
        HideDataPanel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (HideDataPanel.isSelected()) {
                    dataPanel.setVisible(true);
                } else {
                    dataPanel.setVisible(false);
                }
                repaint();
            }
        });
        window.add(HideDataPanel);
        window.add(HideTools);
        window.add(HideConsole);
        window.addSeparator();
        window.add(Tile);
        window.add(Cascade);
        window.addSeparator();
        window.add(ClearConsole);
        ClearConsole.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                consoleArea.setText("");
            }
        });
        Tile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                tile(workSpacePanel);
            }
        });
        Cascade.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cascade(workSpacePanel);
            }
        });

        helpM.add(Help);
        helpM.addSeparator();
        helpM.add(About);
        Help.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manager.help();

            }
        });
        About.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                final JEditorPane editorPane = new JEditorPane();

                // Enable use of custom set fonts
                editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
                editorPane.setFont(new Font("Verdana", Font.PLAIN, 14));

                editorPane.setPreferredSize(new Dimension(470, 100));
                editorPane.setEditable(false);
                editorPane.setContentType("text/html");
                editorPane.setText(
                        "<html>"
                                + "<body bgcolor = \"#535353\" link=\"#009aff\" vlink=\"#009aff\" alink=\"#009aff\">"
                                + "Visan - Visual data analysis package,  (c) Softberry Inc. 2017"
                                + "<br>Authors: Ramzan Umarov & Victor Solovyev<br>"
                                + "<a href='http://www.softberry.com'><font color=\"009aff\">www.softberry.com</font>"
                                + "<a href=\"mailto:softberry@softberry.com?Subject=VISAN\" target=\"_top\"><br>"
                                + "<font color=\"009aff\">Send Mail</font></a>"
                                + "</body>"
                                + "</html>");

                editorPane.setBorder(BorderFactory.createEmptyBorder());
                editorPane.setBackground(new Color(40, 41, 43));
                // TIP: Add Hyperlink listener to process hyperlinks
                editorPane.addHyperlinkListener(new HyperlinkListener() {
                    public void hyperlinkUpdate(final HyperlinkEvent e) {
                        if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    // TIP: Show hand cursor
                                    SwingUtilities.getWindowAncestor(editorPane).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                    // TIP: Show URL as the tooltip
                                    editorPane.setToolTipText(e.getURL().toExternalForm());
                                }
                            });
                        } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    // Show default cursor
                                    SwingUtilities.getWindowAncestor(editorPane).setCursor(Cursor.getDefaultCursor());

                                    // Reset tooltip
                                    editorPane.setToolTipText(null);
                                }
                            });
                        } else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                                try {
                                    Desktop.getDesktop().browse(e.getURL().toURI());
                                } catch (Exception ex) {
                                }

                        }
                    }
                });

                JOptionPane.showMessageDialog(null,
                        editorPane,
                        "About",
                        JOptionPane.INFORMATION_MESSAGE);

            }
        });



        menu.add(file);
        menu.add(data);
        menu.add(analysis);
        menu.add(options);
        menu.add(window);
        menu.add(helpM);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="panels">


        // <editor-fold defaultstate="collapsed" desc="labels">


        final JLabel loadDataLabel = new JLabel();
        loadDataLabel.setIcon(new ImageIcon(PromStudy.class.getResource("/images/data.png")));
        loadDataLabel.setToolTipText("Load Learning Set");
        loadDataLabel.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                manager.io.loadData();
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
                loadDataLabel.setIcon(new ImageIcon(PromStudy.class.getResource("/images/dataRollover.png")));
            }

            public void mouseExited(MouseEvent e) {
                loadDataLabel.setIcon(new ImageIcon(PromStudy.class.getResource("/images/data.png")));
            }
        });

        final JLabel zoomIn = new JLabel();
        zoomIn.setIcon(new ImageIcon(PromStudy.class.getResource("/images/zoomIn.png")));
        zoomIn.setToolTipText("Zoom In");
        zoomIn.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                manager.zoomIn();
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
                zoomIn.setIcon(new ImageIcon(PromStudy.class.getResource("/images/zoomInRollover.png")));
            }

            public void mouseExited(MouseEvent e) {
                zoomIn.setIcon(new ImageIcon(PromStudy.class.getResource("/images/zoomIn.png")));
            }
        });
        final JLabel zoomOut = new JLabel();
        zoomOut.setIcon(new ImageIcon(PromStudy.class.getResource("/images/ZoomOut.png")));
        zoomOut.setToolTipText("Zoom Out");
        zoomOut.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                manager.zoomOut();
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
                zoomOut.setIcon(new ImageIcon(PromStudy.class.getResource("/images/ZoomOutRollover.png")));
            }

            public void mouseExited(MouseEvent e) {
                zoomOut.setIcon(new ImageIcon(PromStudy.class.getResource("/images/ZoomOut.png")));
            }
        });

        final JLabel pointSizeInc = new JLabel();
        pointSizeInc.setIcon(new ImageIcon(PromStudy.class.getResource("/images/sizeInc.png")));
        pointSizeInc.setToolTipText("Increase Point Size");
        pointSizeInc.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                manager.pointSizeInc();

            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
                pointSizeInc.setIcon(new ImageIcon(PromStudy.class.getResource("/images/sizeIncRollover.png")));
            }

            public void mouseExited(MouseEvent e) {
                pointSizeInc.setIcon(new ImageIcon(PromStudy.class.getResource("/images/sizeInc.png")));
            }
        });

        final JLabel pointSizeDec = new JLabel();
        pointSizeDec.setIcon(new ImageIcon(PromStudy.class.getResource("/images/sizeDec.png")));
        pointSizeDec.setToolTipText("Decrease Point Size");
        pointSizeDec.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                manager.pointSizeDec();

            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
                pointSizeDec.setIcon(new ImageIcon(PromStudy.class.getResource("/images/sizeDecRollover.png")));
            }

            public void mouseExited(MouseEvent e) {
                pointSizeDec.setIcon(new ImageIcon(PromStudy.class.getResource("/images/sizeDec.png")));
            }
        });
        final JLabel  snapShotLabel = new JLabel();
        snapShotLabel.setIcon(new ImageIcon(PromStudy.class.getResource("/images/camera.png")));
        snapShotLabel.setToolTipText("Take Snapshot");
        snapShotLabel.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                manager.takeSnapshot();
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
                snapShotLabel.setIcon(new ImageIcon(PromStudy.class.getResource("/images/cameraRollover.png")));
            }

            public void mouseExited(MouseEvent e) {
                snapShotLabel.setIcon(new ImageIcon(PromStudy.class.getResource("/images/camera.png")));
            }
        });

        toolsPanel = new JToolBar();

        toolsPanel.setLayout(new BoxLayout(toolsPanel, BoxLayout.X_AXIS));
        toolsPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        toolsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        toolsPanel.add(loadDataLabel);
        toolsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        toolsPanel.addSeparator();
        toolsPanel.add(zoomIn);
        toolsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        toolsPanel.add(zoomOut);
        toolsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        toolsPanel.addSeparator();
        toolsPanel.add(pointSizeInc);
        toolsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        toolsPanel.add(pointSizeDec);
        toolsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        toolsPanel.addSeparator();
        toolsPanel.add(snapShotLabel);
        //</editor-fold>

        dataPanel = new JPanel();
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS));
        dataPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        dataPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel labelPan1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelPan1.add(new JLabel("Nothing here yet."));

        dataPanel.add(labelPan1);
        dataPanel.add(Box.createRigidArea(new Dimension(0, 10)));


        workSpacePanel = new JDesktopPane();
        workSpacePanel.setBackground(Color.GRAY);
        workSpacePanel.setBorder(BorderFactory.createEmptyBorder());
        // </editor-fold>
        // <editor-fold desc="console">
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setLineWrap(true);
        consoleArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane consoleScroll = new JScrollPane();
        consoleScroll.setPreferredSize(new Dimension(250, 110));
        consoleScroll.setViewportView(consoleArea);
        consoleScroll.setBorder(BorderFactory.createEmptyBorder());

        consolePanel = new JPanel();
        consolePanel.setLayout(new BorderLayout());
        consolePanel.add(consoleScroll, BorderLayout.CENTER);
        consolePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));// </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="prepairing frame">
        setTitle("PromStudy");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setJMenuBar(menu);
        add(toolsPanel, BorderLayout.NORTH);
        add(dataPanel, BorderLayout.EAST);
        add(consolePanel, BorderLayout.SOUTH);
        add(workSpacePanel, BorderLayout.CENTER);
        // </editor-fold>
    }

    public void disposeAllIFrames() {
        for (JInternalFrame iframe : workSpacePanel.getAllFrames()) {
            iframe.dispose();
        }
    }

    public static void tile(JDesktopPane desktopPane) {
        JInternalFrame[] frames = desktopPane.getAllFrames();
        if (frames.length == 0) {
            return;
        }
        tile(frames, desktopPane.getBounds());
    }

    private static void tile(JInternalFrame[] frames, Rectangle dBounds) {
        int cols = (int) Math.sqrt(frames.length);
        int rows = (int) (Math.ceil(((double) frames.length) / cols));
        int lastRow = frames.length - cols * (rows - 1);
        int width, height;

        if (lastRow == 0) {
            rows--;
            height = dBounds.height / rows;
        } else {
            height = dBounds.height / rows;
            if (lastRow < cols) {
                rows--;
                width = dBounds.width / lastRow;
                for (int i = 0; i < lastRow; i++) {
                    frames[cols * rows + i].setBounds(i * width, rows * height,
                            width, height);
                }
            }
        }

        width = dBounds.width / cols;
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                frames[i + j * cols].setBounds(i * width, j * height,
                        width, height);
            }
        }
    }

    public static void cascade(JDesktopPane desktopPane) {
        JInternalFrame[] frames = desktopPane.getAllFrames();
        if (frames.length == 0) {
            return;
        }

        cascade(frames, desktopPane.getBounds(), 24);
    }

    private static void cascade(JInternalFrame[] frames, Rectangle dBounds, int separation) {
        int margin = frames.length * separation + separation;
        int width = dBounds.width - margin;
        int height = dBounds.height - margin;
        for (int i = 0; i < frames.length; i++) {
            frames[i].setBounds(separation + dBounds.x + i * separation,
                    separation + dBounds.y + i * separation,
                    width, height);
        }
    }



    public void writeToConsole(String text) {
        consoleArea.append(text);
    }

    public void createFrame(String title, final DataComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                c.dispatchEvent(e);

            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
        MyInternalFrame frame = new MyInternalFrame(title, sp);
        frame.setVisible(true);
        frame.addInternalFrameListener(new InternalFrameListener() {
            public void internalFrameOpened(InternalFrameEvent e) {
            }

            public void internalFrameClosing(InternalFrameEvent e) {
            }

            public void internalFrameClosed(InternalFrameEvent e) {
                manager.removeFromComponentList(c);
            }

            public void internalFrameIconified(InternalFrameEvent e) {
            }

            public void internalFrameDeiconified(InternalFrameEvent e) {
            }

            public void internalFrameActivated(InternalFrameEvent e) {
                manager.setSelectedComponent(c);
            }

            public void internalFrameDeactivated(InternalFrameEvent e) {
            }
        });
        workSpacePanel.add(frame);
        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
        }
    }

}
