// Lilly Waterman

//example run: assets/cardinal_trim.wav 10

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    private static final int MIN_BARS = 1;
    private static final int MAX_BARS = 10_000;
    private static final String ALLOWED_EXTENSION = ".wav";
    private static final double MIN_SAMPLE = -1.0;
    private static final double MAX_SAMPLE = 1.0;

    // A
    private static String getUserInput() {
        return javax.swing.JOptionPane.showInputDialog(
                null,
                "Enter: <soundFile.wav> <numberOfBars>",
                "Music Visualizer",
                javax.swing.JOptionPane.PLAIN_MESSAGE);
    }

    private static File validateFilename(String rawName) {
        if (rawName == null || rawName.isEmpty()) {
            System.err.println("Error: filename is empty.");
            return null;
        }
        if (!rawName.toLowerCase().endsWith(ALLOWED_EXTENSION)) {
            System.err.println("Error: file must be a .wav file.");
            return null;
        }
        try {
            Path workDir  = Paths.get("").toAbsolutePath();
            Path resolved = workDir.resolve(rawName).normalize();
            if (!resolved.startsWith(workDir)) {
                System.err.println("Error: path traversal attempt blocked.");
                return null;
            }
            File file = resolved.toFile();
            if (!file.exists() || !file.isFile()) {
                System.err.println("Error: file not found: " + rawName);
                return null;
            }
            return file;
        } catch (Exception e) {
            System.err.println("Error: invalid file path.");
            return null;
        }
    }

    private static int validateBars(String rawBars) {
        try {
            int n = Integer.parseInt(rawBars);
            if (n < MIN_BARS || n > MAX_BARS) {
                System.err.println("Error: number of bars must be between "
                        + MIN_BARS + " and " + MAX_BARS + ".");
                return -1;
            }
            return n;
        } catch (NumberFormatException e) {
            System.err.println("Error: '" + rawBars + "' is not a valid integer.");
            return -1;
        }
    }

    // B
    private static double[] readAndValidateAudio(File file) {
        double[] samples;
        try {
            samples = StdAudio.read(file.getPath());
        } catch (Exception e) {
            System.err.println("Error: could not read audio file.");
            return null;
        }
        if (samples == null || samples.length == 0) {
            System.err.println("Error: audio file is empty or unreadable.");
            return null;
        }
        for (double sample : samples) {
            if (sample < MIN_SAMPLE || sample > MAX_SAMPLE) {
                System.err.println("Error: illegal sample value " + sample
                        + ". Values must be in [-1, 1].");
                return null;
            }
        }
        return samples;
    }

    // C — MinMax algorithm: track both the minimum and maximum sample per bar
    private static double[][] computeBarHeights(double[] samples, int numBars) {
        double[][] heights = new double[numBars][2]; // [bar][0] = min, [bar][1] = max
        int total = samples.length;
        for (int bar = 0; bar < numBars; bar++) {
            int start = (int) ((long) bar       * total / numBars);
            int end   = (int) ((long) (bar + 1) * total / numBars);
            if (end > total) end = total;
            double min = 0.0;
            double max = 0.0;
            for (int i = start; i < end; i++) {
                if (samples[i] < min) min = samples[i];
                if (samples[i] > max) max = samples[i];
            }
            heights[bar][0] = min; // most negative sample in slice
            heights[bar][1] = max; // most positive sample in slice
        }
        return heights;
    }

    // D — draw bar using min and max so it extends asymmetrically above and below center
    private static void drawBar(double xCenter, double barWidth, double min, double max, int bar, int numBars) {
        float hue = (float) bar / numBars;
        StdDraw.setPenColor(java.awt.Color.getHSBColor(hue, 0.85f, 0.85f));

        // center of bar sits between min and max in canvas space
        // canvas y=0.5 is the midpoint; samples in [-1,1] are scaled by 0.5
        double barCenter  = 0.5 + (max + min) / 2.0 * 0.5;
        double halfHeight = (max - min) / 2.0 * 0.5;

        StdDraw.filledRectangle(xCenter, barCenter, barWidth * 0.45, halfHeight);
    }

    private static void playAndVisualize(double[] samples, double[][] barHeights) {
        int numBars = barHeights.length;
        int total   = samples.length;
        double barWidth = 1.0 / numBars;

        StdDraw.setCanvasSize(900, 200);
        StdDraw.setXscale(0, 1);
        StdDraw.setYscale(0, 1);
        StdDraw.enableDoubleBuffering();
        StdDraw.clear(StdDraw.WHITE);

        int bar = 0;                      // initialize
        while (bar < numBars) {           // condition
            int start = (int) ((long) bar       * total / numBars);
            int end   = (int) ((long) (bar + 1) * total / numBars);
            if (end > total) end = total;

            double[] slice = new double[end - start];
            System.arraycopy(samples, start, slice, 0, slice.length);
            StdAudio.play(slice);

            double xCenter = (bar + 0.5) * barWidth;
            drawBar(xCenter, barWidth, barHeights[bar][0], barHeights[bar][1], bar, numBars);
            StdDraw.show();

            bar++;                        // increment
        }

        StdAudio.drain();
    }

    private static void showError(String message) {
        System.err.println("Error: " + message);
        javax.swing.JOptionPane.showMessageDialog(
                null, message, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {

        // A
        String input = getUserInput();
        if (input == null) {
            showError("No input provided. Exiting.");
            return;
        }
        String[] tokens = input.trim().split("\\s+");
        if (tokens.length != 2) {
            showError("Please enter exactly two arguments: <soundFile.wav> <numberOfBars>");
            return;
        }

        File soundFile = validateFilename(tokens[0]);
        if (soundFile == null) {
            showError("Invalid filename. Must be a .wav file inside the current directory.");
            return;
        }

        int numBars = validateBars(tokens[1]);
        if (numBars < 0) {
            showError("Invalid number of bars. Enter a whole number between "
                    + MIN_BARS + " and " + MAX_BARS + ".");
            return;
        }

        // B
        double[] samples = readAndValidateAudio(soundFile);
        if (samples == null) {
            showError("Could not load audio. Make sure it is a valid .wav file.");
            return;
        }

        if (numBars > samples.length) {
            numBars = samples.length;
        }

        // C
        double[][] barHeights = computeBarHeights(samples, numBars);

        // D
        playAndVisualize(samples, barHeights);
    }
}