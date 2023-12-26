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
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private JTextArea textArea;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private File file = new File("mouse_events.txt");

    public MousePanel() {
        setPreferredSize(new Dimension(500, 500));
        textArea = new JTextArea(20, 50);
        JScrollPane scrollPane = new JScrollPane(textArea);
        this.add(scrollPane);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String buttonText = e.getButton() == MouseEvent.BUTTON1 ? "L" : "R";
                String time = dateFormat.format(new Date());
                String message = buttonText + " " + e.getX() + " , " + e.getY() + " " + time + "\n";

                Graphics g = getGraphics();
                g.drawRect(e.getX(), e.getY(), 10, 10);
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
        while (matcher.find()) {
            count++;
        }
        System.out.println("Floating-point numbers count: " + count);
    }

    private String getFormattedDate() {
        Locale ruLocale = new Locale("ru", "RU");
        Locale byLocale = new Locale("be", "BY");
        DateFormat ruDateFormat = DateFormat.getDateInstance(DateFormat.LONG, ruLocale);
        DateFormat byDateFormat = DateFormat.getDateInstance(DateFormat.LONG, byLocale);
        Date currentDate = new Date();
        return "RU: " + ruDateFormat.format(currentDate) + "\nBY: " + byDateFormat.format(currentDate);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Mouse Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            MousePanel mousePanel = new MousePanel();
            frame.getContentPane().add(mousePanel);
            frame.pack();
            frame.setVisible(true);
            JOptionPane.showMessageDialog(frame, mousePanel.getFormattedDate());
        });
    }
}
