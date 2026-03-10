// Lilly Waterman

public class Main {
    public static void main(String[] args) {

        // Step 5: Read the full sound file and play all 3 bird calls
        double[] fullSound = StdAudio.read("assets/cardinal_trim.wav");
        System.out.println("Total samples: " + fullSound.length);
        StdAudio.play(fullSound);
        StdAudio.drain();

        // Step 7: Extract just ONE bird call (roughly the first third)
        // Adjust 'end' up or down after listening until you isolate one clean call
        int start = 0;
        int end = fullSound.length / 3;

        double[] oneBird = new double[end - start];
        for (int i = 0; i < oneBird.length; i++) {
            oneBird[i] = fullSound[start + i];
        }

        StdAudio.play(oneBird);
        StdAudio.drain();
    }
}