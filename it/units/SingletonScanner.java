package it.units;

import java.util.Scanner;

public class SingletonScanner {
    private static Scanner scanner = null;
    protected SingletonScanner() {}
    static Scanner getScanner() {
        if (scanner == null) {
            scanner = new Scanner(System.in);
        }
        return scanner;
    }
    static String getString(String prompt) {
        System.out.print(prompt + " ");
        return scanner.nextLine();
    }
}
