package ua.knu.pashchenko_maksym.util;

import java.util.Scanner;

public final class IoUtil {

    private static final Scanner SCANNER = new Scanner(System.in);

    private IoUtil() {
    }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine();
    }

    public static String readNonEmptyLine(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine();
            if (!line.trim().isEmpty()) {
                return line.trim();
            }
            System.out.println("Введення не може бути порожнім. Спробуйте ще раз.");
        }
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Помилка: введіть ціле число. Спробуйте ще раз.");
            }
        }
    }

    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value < min || value > max) {
                System.out.printf("Введіть число від %d до %d.%n", min, max);
            } else {
                return value;
            }
        }
    }

    public static long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine();
            try {
                return Long.parseLong(line.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Помилка: введіть ціле число (long). Спробуйте ще раз.");
            }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine();
            try {
                return Double.parseDouble(line.trim().replace(',', '.'));
            } catch (NumberFormatException ex) {
                System.out.println("Помилка: введіть дійсне число. Спробуйте ще раз.");
            }
        }
    }

    public static int readMenuChoice(int minOption, int maxOption) {
        String prompt = String.format("Оберіть пункт [%d-%d]: ", minOption, maxOption);
        return readIntInRange(prompt, minOption, maxOption);
    }
}
