package com.app;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        try {
            MainApp.main(args);
        } catch (Throwable t) {
            try {
                File desktop = new File(System.getProperty("user.home"), "Desktop");
                File log = new File(desktop, "erro_critico.log");
                try (PrintWriter pw = new PrintWriter(new FileWriter(log, true))) {
                    pw.println("--- " + LocalDateTime.now() + " ---");
                    t.printStackTrace(pw);
                }
            } catch (Exception ignored) {}
            throw t;
        }
    }
}