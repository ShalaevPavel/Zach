package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.*;

public class MousePanel extends JPanel {
    private SimpleDateFormat dateFormat;
    private JLabel dateLabel;
    private JTextArea textArea;

    private JTextArea monthTextArea;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private File file = new File("mouse_events.txt");
    private ResourceBundle messages;
    private Locale currentLocale;

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
                String buttonText = e.getButton() == MouseEvent.BUTTON1 ? "L" : "R";
                String time = dateFormat.format(new Date());
                String message = buttonText + " " + e.getX() + "," + e.getY() + " " + "\n";

                Graphics g = getGraphics();

                g.drawOval(e.getX() - 5, e.getY() - 5, 10, 10);  // Draw a circle instead of a rectangle
                g.drawString(buttonText, e.getX(), e.getY());


                executorService.submit(() -> {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                        writer.write(message);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    updateTextArea();
                });
            }
        });
    }

    private void changeLanguage() {
        currentLocale = currentLocale.equals(Locale.ENGLISH) ? Locale.FRENCH : Locale.ENGLISH;
        messages = ResourceBundle.getBundle("messages", currentLocale);
        dateFormat = new SimpleDateFormat(messages.getString("dateFormat"));
        dateLabel.setText(messages.getString(getCurrentMonth()) + " " + dateFormat.format(new Date()));
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
        Pattern pattern = Pattern.compile("\\b\\d+\\.\\d+\\b");
        Matcher matcher = pattern.matcher(content);
        int count = 0;
        StringBuilder floatingPoints = new StringBuilder();
        while (matcher.find()) {
            floatingPoints.append(matcher.group()).append("\n");
        }
        System.out.println("Floating-point numbers: \n" + floatingPoints);
    }


    private String getCurrentMonth() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
        return monthFormat.format(new Date());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Mouse Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            MousePanel mousePanel = new MousePanel();
            frame.getContentPane().add(mousePanel);
            frame.pack();
            frame.setVisible(true);
        });
    }
}