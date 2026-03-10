// Lilly Waterman

public class Main {
    public static void main(String[] args) {

        double[] fullSound = StdAudio.read("assets/cardinal_trim.wav");
        System.out.println("Total samples: " + fullSound.length);
        StdAudio.play(fullSound);
        StdAudio.drain();

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