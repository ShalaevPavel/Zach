package org.example;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class CustomMouseEvent extends EventObject {
    private int button;
    private int x, y;

    public CustomMouseEvent(Object source, int button, int x, int y) {
        super(source);
        this.button = button;
        this.x = x;
        this.y = y;
    }

    public int getButton() {
        return button;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

interface CustomMouseListener {
    void mouseClicked(CustomMouseEvent e);
}
public class MousePanel extends JPanel {
    private SimpleDateFormat dateFormat;
    private JLabel dateLabel;
    private JTextArea textArea;

    private JTextArea monthTextArea;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private File file = new File("mouse_events.txt");
    private ResourceBundle messages;
    private Locale currentLocale;

    private java.util.List<CustomMouseListener> listeners = new java.util.ArrayList<>();


    public MousePanel() {
        currentLocale = Locale.getDefault();
        messages = ResourceBundle.getBundle("messages", currentLocale);
        dateFormat = new SimpleDateFormat(messages.getString("dateFormat"));
        System.out.println(getCurrentMonth());

//        String month = messages.getString(getCurrentMonth()) ;
//        System.out.println(month);


        setPreferredSize(new Dimension(500, 500));
        textArea = new JTextArea(20, 50);
        JScrollPane scrollPane = new JScrollPane(textArea);
        this.add(scrollPane);

        dateLabel = new JLabel(messages.getString(getCurrentMonth()) + " " + dateFormat.format(new Date()));
        this.add(dateLabel);

        JButton changeLangButton = new JButton(messages.getString("changeLang"));
        changeLangButton.addActionListener(e -> changeLanguage());
        this.add(changeLangButton);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int button = e.getButton() == MouseEvent.BUTTON1 ? 1 : 2; // 1 for left, 2 for right
                // Pass MousePanel.this as the source
                CustomMouseEvent customEvent = new CustomMouseEvent(MousePanel.this, button, e.getX(), e.getY());
                for (CustomMouseListener listener : listeners) {
                    listener.mouseClicked(customEvent);
                }
            }
        });
    }

    public void addCustomMouseListener(CustomMouseListener listener) {
        listeners.add(listener);
    }

    private void changeLanguage() {
        currentLocale = currentLocale.equals(Locale.ENGLISH) ? Locale.FRENCH : Locale.ENGLISH;
        messages = ResourceBundle.getBundle("messages", currentLocale);
        dateFormat = new SimpleDateFormat(messages.getString("dateFormat"));
        dateLabel.setText(messages.getString(getCurrentMonth())+ " " + dateFormat.format(new Date()));
        ((JButton) getComponent(2)).setText(messages.getString("changeLang"));
    }

    private void updateTextArea() {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            SwingUtilities.invokeLater(() -> textArea.setText(content));
            countFloatingPoints(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void countFloatingPoints(String content) {
        Pattern pattern = Pattern.compile("\\b\\d+\\,\\d+\\b");
        Matcher matcher = pattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        System.out.println("Floating-point numbers count: " + count);
    }

    private String getCurrentMonth() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
        return monthFormat.format(new Date());
    }

    private static CustomMouseListener createDrawingListener() {
        return e -> {
            Graphics g = ((Component) e.getSource()).getGraphics();
            int diameter = 20;
            g.drawOval(e.getX() - diameter / 2, e.getY() - diameter / 2, diameter, diameter);
            g.drawString(e.getButton() == 1 ? "L" : "R", e.getX(), e.getY());
        };
    }

    private static CustomMouseListener createLoggingListener(String panelName) {
        return e -> {
            String message = panelName + " " + (e.getButton() == 1 ? "L" : "R") + " " + e.getX() + "," + e.getY() + "\n";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("mouse_events.txt", true))) {
                writer.write(message);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        };
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Mouse Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            MousePanel panel1 = new MousePanel();
            MousePanel panel2 = new MousePanel();

            panel1.addCustomMouseListener(createDrawingListener());
            panel2.addCustomMouseListener(createDrawingListener());

            panel1.addCustomMouseListener(createLoggingListener("First"));
            panel2.addCustomMouseListener(createLoggingListener("Second"));

            frame.setLayout(new GridLayout(1, 2));
            frame.add(panel1);
            frame.add(panel2);
            frame.pack();
            frame.setVisible(true);
        });
    }
}
