package org.kanatti.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple ASCII table printer with proper column alignment.
 */
public class TablePrinter {
    private final String[] headers;
    private final List<String[]> rows = new ArrayList<>();
    private final int[] columnWidths;

    public TablePrinter(String... headers) {
        this.headers = headers;
        this.columnWidths = new int[headers.length];

        // Initialize with header widths
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }
    }

    public void addRow(String... values) {
        if (values.length != headers.length) {
            throw new IllegalArgumentException("Row must have " + headers.length + " columns");
        }

        rows.add(values);

        // Update column widths
        for (int i = 0; i < values.length; i++) {
            columnWidths[i] = Math.max(columnWidths[i], values[i].length());
        }
    }

    public void print() {
        printSeparator();
        printRow(headers);
        printSeparator();

        for (String[] row : rows) {
            printRow(row);
        }

        printSeparator();
    }

    private void printSeparator() {
        for (int i = 0; i < headers.length; i++) {
            System.out.print("+");
            System.out.print("-".repeat(columnWidths[i] + 2));
        }
        System.out.println("+");
    }

    private void printRow(String[] values) {
        for (int i = 0; i < values.length; i++) {
            System.out.print("| ");

            String value = values[i];
            int padding = columnWidths[i] - value.length();

            // Right-align numbers, left-align text
            if (value.matches(".*\\d+.*")) {
                System.out.print(" ".repeat(padding) + value);
            } else {
                System.out.print(value + " ".repeat(padding));
            }

            System.out.print(" ");
        }
        System.out.println("|");
    }
}
