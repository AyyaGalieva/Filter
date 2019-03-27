package ru.nsu.fit.g16201.galieva.Filter.View;

import ru.nsu.fit.g16201.galieva.Filter.Model.Model;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class GUI extends JFrame{
    private ImagePanel imagePanel;
    private MenuBar menuBar;
    private JToolBar toolBar;
    private JScrollPane scrollPane;
    private JLabel statusBar;

    private Model model;

    private Map<String, AbstractButton> buttonMap = new TreeMap<>();
    private Map<String, Menu> menuMap = new TreeMap<>();
    private Map<String, MenuItem> menuItemMap = new TreeMap<>();

    private boolean selectMode = false;

    public GUI(Model model) {
        this.model = model;

        setTitle("Filter");
        setSize(1200, 800);
        setMinimumSize(new Dimension(600, 400));
        setLocationByPlatform(true);

        imagePanel = new ImagePanel(model, new ImagePanelClickListener() {
            @Override
            public void onClick(Point p) {
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                close();
            }
        });

        menuBar = new MenuBar();
        toolBar = new JToolBar();
        this.setMenuBar(menuBar);

        statusBar = new JLabel();
        statusBar.setPreferredSize(new Dimension(150, 15));
        statusBar.setBackground(Color.white);

        scrollPane = new JScrollPane(imagePanel);
        add(scrollPane, BorderLayout.CENTER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        addButton("Save", "File", "Save image", true, "/resources/save.png", () -> {
            if (model.getImageC() == null) {
                showFailedToSave();
                return;
            }
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir") + "/test/saved/");
            fileChooser.setDialogTitle("Save state");
            int f = fileChooser.showSaveDialog(GUI.this);
            if (f == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (model != null) {
                    model.saveImage(file.getAbsolutePath());
                }
            }
        });

        addButton("Load", "File", "Load image", true, "/resources/load.png", () -> {
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir") + "/test/");
            fileChooser.setDialogTitle("Load state");
            int f = fileChooser.showOpenDialog(GUI.this);
            if (f == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (model != null) {
                    model.loadImage(file.getAbsolutePath());
                    imagePanel.load();
                }
            }
            if (selectMode) {
                selectMode = false;
                buttonMap.get("Select").setSelected(false);
                ((CheckboxMenuItem)menuItemMap.get("Select")).setState(false);
                imagePanel.setSelectMode(false);
            }
        });

        addButton("Select", "Filters", "Select area", false, "/resources/select.png", () -> {
            selectMode = !selectMode;
            imagePanel.setSelectMode(selectMode);
        });

        toolBar.addSeparator();

        addButton("B to C", "Filters", "Copy image B to image C", true, "/resources/BtoC.jpg", () -> {
            imagePanel.copyBtoC();
        });

        addButton("C to B", "Filters", "Copy image C to image B", true, "/resources/CtoB.jpg", () -> {
            imagePanel.copyCtoB();
        });

        toolBar.addSeparator();

        addButton("Discolor", "Filters", "Apply discoloring filter", true, "/resources/discolor.png", () -> {
            imagePanel.discolorFilter();
        });

        addButton("Negative", "Filters", "Apply negative filter", true, "/resources/negative.png", ()-> {
            imagePanel.negativeFilter();
        });

        toolBar.addSeparator();

        addButton("Ordered dithering", "Filters", "Apply ordered dithering", true, "/resources/ordered.png", ()-> {
            if (model.getImageB() != null) {
                imagePanel.orderedDithering(model.getCountRed(), model.getCountGreen(), model.getCountBlue());
                showColorCountDialog('O');
            }        });

        addButton("Floyd-Steinberg dithering", "Filters", "Apply Floyd-Steinberg dithering", true, "/resources/floyd.png", ()-> {
            if (model.getImageB() != null) {
                imagePanel.floydSteinbergDithering(model.getCountRed(), model.getCountGreen(), model.getCountBlue());
                showColorCountDialog('F');
            }
        });

        toolBar.addSeparator();

        addButton("Zoom x2", "Filters", "Apply zoom filter", true, "/resources/zoom.png", ()-> {
            imagePanel.zoomFilter();
        });

        addButton("Rotation", "Filters", "Apply rotation filter", true, "/resources/rotate.png", ()-> {
            if (model.getImageB() != null) {
                imagePanel.rotationFilter(model.getAngle());
                JPanel panel = new JPanel();
                JDialog dialog = new JDialog();
                dialog.setTitle("Rotation");
                dialog.setResizable(false);
                dialog.setSize(400, 100);
                dialog.add(panel);
                dialog.setLocationRelativeTo(this);

                panel.setBorder(new EmptyBorder(5, 5, 5, 5));
                panel.setLayout(new GridLayout(2, 2, 5, 10));
                panel.add(new JLabel("Angle:"));
                panel.add(new JLabel(""));

                try {
                    JTextField parameterField = new JTextField();
                    parameterField.setText(Integer.toString(model.getAngle()));

                    panel.add(parameterField);

                    JSlider slider = new JSlider();
                    slider.setValue(model.getAngle());
                    slider.setMinimum(-180);
                    slider.setMaximum(180);
                    slider.addChangeListener(e -> {
                        int value = ((JSlider) e.getSource()).getValue();
                        if (parameterField.getText().equals(Integer.toString(value)))
                            return;
                        EventQueue.invokeLater(() -> parameterField.setText(Integer.toString(value)));
                        imagePanel.rotationFilter(slider.getValue());
                    });

                    parameterField.addCaretListener(e -> {
                        String value = parameterField.getText();
                        if (value.isEmpty() || value.equals(Integer.toString(slider.getValue())))
                            return;
                        slider.setValue(Integer.parseInt(value));
                        imagePanel.rotationFilter(slider.getValue());
                    });

                    panel.add(slider);

                    dialog.setVisible(true);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        });

        toolBar.addSeparator();

        addButton("Roberts operator", "Filters", "Apply Roberts operator", true, "/resources/roberts.png", ()-> {
            if (model.getImageB() != null) {
                imagePanel.robertsOperator(model.getRobertsParameter());
                showThresholdDialog('R');
            }
        });

        addButton("Sobel operator", "Filters", "Apply Sobel operator", true, "/resources/sobel.png", ()-> {
            if (model.getImageB() != null) {
                imagePanel.sobelOperator(model.getSobelParameter());
                showThresholdDialog('S');
            }
        });

        toolBar.addSeparator();

        addButton("Sharpening", "Filters", "Apply sharpening filter", true, "/resources/sharp.png", ()-> {
            imagePanel.sharpening();
        });

        addButton("Blur", "Filters", "Apply blur filter", true, "/resources/blur.png", ()-> {
            imagePanel.blurFilter();
        });

        addButton("Stamping", "Filters", "Apply stamping filter", true, "/resources/stamp.png", ()-> {
            imagePanel.stamping();
        });

        addButton("Median", "Filters", "Apply median filter", true, "/resources/median.png", ()-> {
            imagePanel.medianFilter();
        });

        addButton("Aquarel", "Filters", "Apply aquarel filter", true, "/resources/aquarel.png", ()-> {
            imagePanel.aquarelFilter();
        });

        addButton("Gamma correction", "Filters", "Apply gamma correction", true, "/resources/gamma.png", ()-> {
            if (model.getImageB() != null) {
                imagePanel.gammaCorrection(model.getGamma());
                JPanel panel = new JPanel();
                JDialog dialog = new JDialog();
                dialog.setTitle("Gamma correction");
                dialog.setResizable(false);
                dialog.setSize(400, 100);
                dialog.add(panel);
                dialog.setLocationRelativeTo(this);

                panel.setBorder(new EmptyBorder(5, 5, 5, 5));
                panel.setLayout(new GridLayout(2, 2, 5, 10));
                panel.add(new JLabel("Gamma:"));
                panel.add(new JLabel(""));

                try {
                    JTextField parameterField = new JTextField();
                    parameterField.setText(Integer.toString(model.getGamma()));

                    panel.add(parameterField);

                    JSlider slider = new JSlider();
                    slider.setValue(model.getGamma());
                    slider.setMinimum(0);
                    slider.setMaximum(200);
                    slider.addChangeListener(e -> {
                        int value = ((JSlider) e.getSource()).getValue();
                        if (parameterField.getText().equals(Integer.toString(value)))
                            return;
                        EventQueue.invokeLater(() -> parameterField.setText(Integer.toString(value)));
                        imagePanel.gammaCorrection(slider.getValue());
                    });

                    parameterField.addCaretListener(e -> {
                        String value = parameterField.getText();
                        if (value.isEmpty() || value.equals(Integer.toString(slider.getValue())))
                            return;
                        slider.setValue(Integer.parseInt(value));
                        imagePanel.gammaCorrection(slider.getValue());
                    });

                    panel.add(slider);

                    dialog.setVisible(true);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        });

        toolBar.addSeparator();

        addButton("Info", "Help", "Show author's info", true, "/resources/info.jpg", () ->
                JOptionPane.showMessageDialog(null, "Filter v.1.0\n" + "Author:\t Ayya Galieva, gr. 16201",
                        "Author info", JOptionPane.INFORMATION_MESSAGE));

        add(toolBar, BorderLayout.NORTH);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void showColorCountDialog(char dithering) {
        JPanel panel = new JPanel();
        JDialog dialog = new JDialog();
        if (dithering == 'O')
            dialog.setTitle("Ordered dithering");
        if (dithering == 'F')
            dialog.setTitle("Floyd-Steinberg dithering");
        dialog.setResizable(false);
        dialog.setSize(400, 150);
        dialog.add(panel);
        dialog.setLocationRelativeTo(this);

        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.setLayout(new GridLayout(3, 2, 2, 10));

        try {
            panel.add(new JLabel("R count:"));
            JSpinner redParameter = new JSpinner(new SpinnerNumberModel(model.getCountRed(), 2, 255, 1));
            panel.add(redParameter);
            redParameter.addChangeListener(e -> {
                int red = (Integer)redParameter.getValue();
                if (dithering == 'O')
                    imagePanel.orderedDithering(red, model.getCountGreen(), model.getCountBlue());
                if (dithering == 'F')
                    imagePanel.floydSteinbergDithering(red, model.getCountGreen(), model.getCountBlue());
            });
            panel.add(new JLabel("G count:"));
            JSpinner greenParameter = new JSpinner(new SpinnerNumberModel(model.getCountGreen(), 2, 255, 1));
            panel.add(greenParameter);
            greenParameter.addChangeListener(e -> {
                int green = (Integer)greenParameter.getValue();
                if (dithering == 'O')
                    imagePanel.orderedDithering( model.getCountRed(), green, model.getCountBlue());
                if (dithering == 'F')
                    imagePanel.floydSteinbergDithering( model.getCountRed(), green, model.getCountBlue());
            });
            panel.add(new JLabel("B count:"));
            JSpinner blueParameter = new JSpinner(new SpinnerNumberModel(model.getCountBlue(), 2, 255, 1));
            panel.add(blueParameter);
            blueParameter.addChangeListener(e -> {
                int blue = (Integer)blueParameter.getValue();
                if (dithering == 'O')
                    imagePanel.orderedDithering( model.getCountRed(), model.getCountGreen(), blue);
                if (dithering == 'F')
                    imagePanel.floydSteinbergDithering( model.getCountRed(), model.getCountGreen(), blue);
            });
            dialog.setVisible(true);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void showThresholdDialog(char operator) {
        JPanel panel = new JPanel();
        JDialog dialog = new JDialog();
        if (operator == 'R')
            dialog.setTitle("Roberts operator");
        if (operator == 'S')
            dialog.setTitle("Sobel operator");
        dialog.setResizable(false);
        dialog.setSize(400, 100);
        dialog.add(panel);
        dialog.setLocationRelativeTo(this);

        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.setLayout(new GridLayout(2, 2, 5, 10));
        panel.add(new JLabel("Threshold parameter:"));
        panel.add(new JLabel(""));

        try {
            JTextField parameterField = new JTextField();
            if (operator == 'R')
                parameterField.setText(Integer.toString(model.getRobertsParameter()));
            else if (operator == 'S')
                parameterField.setText(Integer.toString(model.getSobelParameter()));

            panel.add(parameterField);

            JSlider slider = new JSlider();
            if (operator == 'R')
                slider.setValue(model.getRobertsParameter());
            else if (operator == 'S')
                slider.setValue(model.getSobelParameter());
            slider.setMinimum(0);
            slider.setMaximum(255);
            slider.addChangeListener(e -> {
                int value = ((JSlider)e.getSource()).getValue();
                if (parameterField.getText().equals(Integer.toString(value)))
                    return;
                EventQueue.invokeLater(() -> parameterField.setText(Integer.toString(value)));
                if (operator == 'R')
                    imagePanel.robertsOperator(slider.getValue());
                else if (operator == 'S')
                    imagePanel.sobelOperator(slider.getValue());
            });

            parameterField.addCaretListener(e -> {
                String value = parameterField.getText();
                if (value.isEmpty() || value.equals(Integer.toString(slider.getValue())))
                    return;
                slider.setValue(Integer.parseInt(value));
                if (operator == 'R')
                    imagePanel.robertsOperator(slider.getValue());
                else if (operator == 'S')
                    imagePanel.sobelOperator(slider.getValue());
            });

            panel.add(slider);

            dialog.setVisible(true);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void addButton(String name, String menuName, String toolTipText, boolean shutdown, String imagePath, Runnable action) {
        AbstractButton button;
        MenuItem item;

        Image toolImage = null;
        try {
            toolImage = ImageIO.read(getClass().getResource(imagePath));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        if (shutdown) {
            if (toolImage != null) {
                button = new JButton();
                button.setIcon(new ImageIcon(toolImage));
            }
            else {
                button = new JButton(name);
            }
            item = new MenuItem(name);
            item.addActionListener(e -> {
                if (item.isEnabled()) {
                    action.run();
                }
            });
        }
        else {
            if (toolImage != null) {
                button = new JToggleButton();
                button.setIcon(new ImageIcon(toolImage));
            }
            else {
                button = new JToggleButton(name);
            }
            CheckboxMenuItem checkboxMenuItem = new CheckboxMenuItem(name);
            checkboxMenuItem.addItemListener(e -> {
                if (checkboxMenuItem.isEnabled())
                    action.run();
            });
            item = checkboxMenuItem;
        }

        button.setToolTipText(toolTipText);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            boolean pressedOrEntered = false;
            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.isEnabled() && pressedOrEntered)
                    action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                pressedOrEntered = true;
                statusBar.setText(toolTipText);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                statusBar.setText("");
                pressedOrEntered = false;
            }
        };

        button.addMouseListener(mouseAdapter);
        toolBar.add(button);

        if (!menuMap.containsKey(menuName)) {
            Menu menu = new Menu(menuName);
            menuMap.put(menuName, menu);
            menuBar.add(menu);
        }
        menuMap.get(menuName).add(item);
        menuItemMap.put(name, item);
        buttonMap.put(name, button);
    }

    private void close() {
        if (model.getImageC() != null) {
            int confirmation = JOptionPane.showConfirmDialog(null, "Save changes?", "Exit", JOptionPane.YES_NO_OPTION);

            if (confirmation == JOptionPane.OK_OPTION) {
                JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir") + "/test/saved/");
                fileChooser.setDialogTitle("Save");
                int f = fileChooser.showSaveDialog(GUI.this);
                if (f == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (model != null)
                        model.saveImage(file.getAbsolutePath());
                }
            }
        }
        System.exit(0);
    }

    public void showFailedToSave() {
        JOptionPane.showMessageDialog(this, "C image is empty", "error", JOptionPane.WARNING_MESSAGE);
    }
    public ImagePanel getImagePanel() {
        return imagePanel;
    }
}
