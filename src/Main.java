// Lilly Waterman

public class Main {
    public static void main(String[] args) {

        double[] fullSound = StdAudio.read("assets/cardinal_trim.wav");
        System.out.println("Total samples: " + fullSound.length);
        StdAudio.play(fullSound);
        StdAudio.drain();

    }
}