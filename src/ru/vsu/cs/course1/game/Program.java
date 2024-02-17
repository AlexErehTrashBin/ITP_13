package ru.vsu.cs.course1.game;

import java.util.Locale;

public class Program {
    public static void main(String[] args) {
        Locale.setDefault(Locale.ROOT);

        java.awt.EventQueue.invokeLater(() -> new MainForm().setVisible(true));
    }
}