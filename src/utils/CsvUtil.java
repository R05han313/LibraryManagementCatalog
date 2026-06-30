package utils;

import data.LibraryData;
import models.Book;
import javax.swing.*;
import java.awt.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

/**
 * Utility for exporting the book catalog to CSV and importing books from CSV.
 * Designed to be defensive against malformed input rows.
 */
public class CsvUtil {

    public static void exportBooks(Component parent, LibraryData data) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("library_catalog_export.csv"));
        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION)
            return;

        File file = chooser.getSelectedFile();
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(
                    "Title,Author,ISBN,Genre,Publisher,Year,TotalCopies,AvailableCopies,Price,Shelf,Rating,Status,Description\n");
            for (Book b : data.getBooks()) {
                writer.write(String.join(",",
                        csvEscape(b.getTitle()), csvEscape(b.getAuthor()), csvEscape(b.getIsbn()),
                        csvEscape(b.getGenre()), csvEscape(b.getPublisher()), String.valueOf(b.getPublicationYear()),
                        String.valueOf(b.getTotalCopies()), String.valueOf(b.getAvailableCopies()),
                        String.valueOf(b.getPrice()), csvEscape(b.getShelfLocation()),
                        String.valueOf(b.getRating()), b.getStatus().toString(), csvEscape(b.getDescription())));
                writer.write("\n");
            }
            JOptionPane.showMessageDialog(parent, "Exported " + data.getBooks().size() + " books to " + file.getName(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Failed to export: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void importBooks(Component parent, LibraryData data, Runnable onComplete) {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION)
            return;

        File file = chooser.getSelectedFile();
        int imported = 0;
        int skipped = 0;
        StringBuilder errorDetails = new StringBuilder();

        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "The selected file is empty.", "Import Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int startIndex = lines.get(0).toLowerCase().contains("title") ? 1 : 0; // skip header if present

            for (int i = startIndex; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty())
                    continue;
                try {
                    String[] fields = parseCsvLine(line);
                    if (fields.length < 4) {
                        skipped++;
                        errorDetails.append("Line " + (i + 1) + ": too few fields.\n");
                        continue;
                    }
                    String title = fields.length > 0 ? fields[0] : "";
                    String author = fields.length > 1 ? fields[1] : "";
                    String isbn = fields.length > 2 ? fields[2] : "";
                    String genre = fields.length > 3 ? fields[3] : "General";
                    String publisher = fields.length > 4 ? fields[4] : "";
                    int year = fields.length > 5 ? safeParseInt(fields[5], 2000) : 2000;
                    int totalCopies = fields.length > 6 ? safeParseInt(fields[6], 1) : 1;
                    double price = fields.length > 8 ? safeParseDouble(fields[8], 0.0) : 0.0;
                    String shelf = fields.length > 9 ? fields[9] : "GEN-000";
                    String description = fields.length > 12 ? fields[12] : "";

                    if (title.isEmpty() || isbn.isEmpty()) {
                        skipped++;
                        errorDetails.append("Line " + (i + 1) + ": missing title or ISBN.\n");
                        continue;
                    }
                    if (data.isDuplicateIsbn(isbn, "")) {
                        skipped++;
                        errorDetails.append("Line " + (i + 1) + ": duplicate ISBN " + isbn + ".\n");
                        continue;
                    }

                    Book book = new Book(title, author, isbn, genre, publisher, year,
                            Math.max(0, totalCopies), Math.max(0, price), shelf, description);
                    data.addBook(book);
                    imported++;
                } catch (Exception rowEx) {
                    skipped++;
                    errorDetails.append("Line " + (i + 1) + ": " + rowEx.getMessage() + "\n");
                }
            }

            String summary = "Import complete.\n\nSuccessfully imported: " + imported + "\nSkipped: " + skipped;
            if (skipped > 0 && errorDetails.length() < 1000) {
                summary += "\n\nDetails:\n" + errorDetails;
            }
            JOptionPane.showMessageDialog(parent, summary, "Import Results",
                    imported > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            onComplete.run();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Failed to read file: " + e.getMessage(),
                    "Import Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static int safeParseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static double safeParseDouble(String s, double fallback) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String csvEscape(String value) {
        if (value == null)
            return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Minimal but correct CSV line parser handling quoted fields with embedded
     * commas.
     */
    private static String[] parseCsvLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}