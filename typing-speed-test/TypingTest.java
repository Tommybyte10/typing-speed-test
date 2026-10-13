import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * A typing speed test with difficulty levels, a countdown, a mistake
 * report, ratings, and an all-time personal best saved to a file.
 *
 * @author Thomas Sanchez
 */
public class TypingTest {

    private static final String BEST_FILE = "best.txt";

    private static final String[] EASY = {
        "Eva is a very good dancer.",
        "I like to code at night.",
        "Java is fun to learn.",
        "Salads are fresh and green.",
        "Practice every single day."
    };

    private static final String[] MEDIUM = {
        "The quick brown fox jumps over the lazy dog.",
        "Practice makes progress, not perfection.",
        "Every great developer was once a beginner.",
        "Java is the language I am learning this semester.",
        "Fresh salads are made to order every single day."
    };

    private static final String[] HARD = {
        "Rowan University's computer science program challenges students every semester!",
        "If the printer jams during a rush, check the paper roll, then restart the terminal.",
        "Inheritance lets a SavingsAccount reuse code from its abstract parent, Account.",
        "\"Debugging is twice as hard as writing the code,\" said Brian Kernighan.",
        "On October 13, 2026, I will pitch my three Java projects at the career fair."
    };

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Random random = new Random();
        ArrayList<Double> scores = new ArrayList<Double>();
        double allTimeBest = loadBest();
        boolean playing = true;

        System.out.println("=== Typing Speed Test ===");
        System.out.printf("Your all-time best: %.0f WPM%n", allTimeBest);

        String[] sentences = chooseDifficulty(input);

        while (playing) {
            String target = sentences[random.nextInt(sentences.length)];

            System.out.println("\nType this sentence exactly, then press Enter:");
            System.out.println(target);
            System.out.println("Press Enter when you are ready...");
            input.nextLine();
            countdown();

            long startTime = System.currentTimeMillis();
            String typed = input.nextLine();
            long endTime = System.currentTimeMillis();

            double seconds = (endTime - startTime) / 1000.0;
            double minutes = seconds / 60.0;
            double words = typed.length() / 5.0;
            double wpm = words / minutes;
            double accuracy = calculateAccuracy(target, typed);

            scores.add(wpm);

            System.out.printf("Time: %.1f seconds%n", seconds);
            System.out.printf("Speed: %.0f words per minute (%s)%n", wpm, getRating(wpm));
            System.out.printf("Accuracy: %.0f%%%n", accuracy);
            printMistakes(target, typed);

            System.out.print("\nPlay again? (y/n): ");
            String answer = input.nextLine();
            if (!answer.equalsIgnoreCase("y")) {
                playing = false;
            }
        }

        double best = 0;
        double total = 0;
        for (double score : scores) {
            total = total + score;
            if (score > best) {
                best = score;
            }
        }
        double average = total / scores.size();

        System.out.printf("%nYou played %d round(s).%n", scores.size());
        System.out.printf("Best this session: %.0f WPM%n", best);
        System.out.printf("Average: %.0f WPM (%s)%n", average, getRating(average));

        if (best > allTimeBest) {
            saveBest(best);
            System.out.println("New all-time personal best! Saved.");
        }

        input.close();
    }

    /**
     * Asks the player to pick Easy, Medium, or Hard.
     *
     * @param input the Scanner reading the keyboard
     * @return the array of sentences for that difficulty
     */
    public static String[] chooseDifficulty(Scanner input) {
        while (true) {
            System.out.print("Choose difficulty (1 = Easy, 2 = Medium, 3 = Hard): ");
            String choice = input.nextLine().trim();
            if (choice.equals("1")) {
                return EASY;
            } else if (choice.equals("2")) {
                return MEDIUM;
            } else if (choice.equals("3")) {
                return HARD;
            }
            System.out.println("Please type 1, 2, or 3.");
        }
    }

    /**
     * Prints 3... 2... 1... GO! with a one-second pause between each.
     */
    public static void countdown() {
        try {
            for (int i = 3; i >= 1; i--) {
                System.out.println(i + "...");
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("Countdown skipped.");
        }
        System.out.println("GO!");
    }

    /**
     * Compares the typed text to the target sentence, one character at a time.
     *
     * @param target the sentence the user was supposed to type
     * @param typed  what the user actually typed
     * @return the percent of characters typed correctly
     */
    public static double calculateAccuracy(String target, String typed) {
        int correct = 0;
        int shorter = Math.min(target.length(), typed.length());

        for (int i = 0; i < shorter; i++) {
            if (target.charAt(i) == typed.charAt(i)) {
                correct++;
            }
        }

        return (correct * 100.0) / target.length();
    }

    /**
     * Prints every word the player got wrong or skipped.
     *
     * @param target the sentence the user was supposed to type
     * @param typed  what the user actually typed
     */
    public static void printMistakes(String target, String typed) {
        String[] targetWords = target.split(" ");
        String[] typedWords = typed.trim().split(" ");
        int mistakes = 0;

        for (int i = 0; i < targetWords.length; i++) {
            if (i >= typedWords.length) {
                System.out.println("  Missing: \"" + targetWords[i] + "\"");
                mistakes++;
            } else if (!targetWords[i].equals(typedWords[i])) {
                System.out.println("  Expected \"" + targetWords[i]
                        + "\" but you typed \"" + typedWords[i] + "\"");
                mistakes++;
            }
        }

        if (mistakes == 0) {
            System.out.println("Perfect! No mistakes.");
        } else {
            System.out.println(mistakes + " word(s) wrong.");
        }
    }

    /**
     * Turns a words-per-minute number into a rating.
     *
     * @param wpm the speed to rate
     * @return Beginner, Good, Fast, or Pro
     */
    public static String getRating(double wpm) {
        if (wpm < 25) {
            return "Beginner";
        } else if (wpm < 40) {
            return "Good";
        } else if (wpm < 60) {
            return "Fast";
        } else {
            return "Pro";
        }
    }

    /**
     * Reads the all-time best score from best.txt.
     *
     * @return the saved best WPM, or 0 if there isn't one yet
     */
    public static double loadBest() {
        Path path = Paths.get(BEST_FILE);
        if (!Files.exists(path)) {
            return 0;
        }
        try {
            String text = new String(Files.readAllBytes(path)).trim();
            return Double.parseDouble(text);
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Saves a new all-time best score to best.txt.
     *
     * @param best the WPM to save
     */
    public static void saveBest(double best) {
        try {
            Files.write(Paths.get(BEST_FILE), String.valueOf(best).getBytes());
        } catch (IOException e) {
            System.out.println("Could not save your best score.");
        }
    }
}
