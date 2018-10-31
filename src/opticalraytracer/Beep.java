package opticalraytracer;

import javax.sound.sampled.*;

final public class Beep extends Thread {

    float sampleRate = 32000;
    int freqHz;
    int durationMsec;
    double level;

    // 0 <= level <= 1.0
    public Beep(int freqHz, int durationMsec, double level) {
        this.freqHz = freqHz;
        this.durationMsec = durationMsec;
        this.level = level * 32767;
    }

    private double envelope(double a, double b, double t, double tc) {
        return ((b - t) * (-a + t)) / ((b - t + tc) * (-a + t + tc));
    }

    public void run() {
        try {
            int bsize = (int) (2 * sampleRate * durationMsec / 1000);
            byte[] buf = new byte[bsize];
            double step = 2.0 * Math.PI * freqHz / sampleRate;
            double angle = 0;
            int i = 0;
            while (i < bsize) {
                int n = (int) (Math.sin(angle) * level * envelope(0, bsize, i, 1000));
                buf[i++] = (byte) (n % 256);
                buf[i++] = (byte) (n / 256);
                angle += step;
            }

            AudioFormat af = new AudioFormat(sampleRate, 16, 1, true, false);
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();
            sdl.write(buf, 0, buf.length);
            Thread.sleep(durationMsec * 2);
            sdl.drain();
            sdl.close();
            //System.out.println("DONE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void beep(double level) {
        Beep b = new Beep(1000, 100, level);
        b.start();
        // in standalone mode, must join thread
        // to prevent an ugly artifact
        try {
            b.join();
        } catch (Exception e) {
        }
    }

    public static void beep() {
        Beep b = new Beep(1000, 100, .5);
        b.start();
        // in standalone mode, must join thread
        // to prevent an ugly artifact
        try {
            b.join();
        } catch (Exception e) {
        }
    }

    public static void main(String[] args) {
        beep(.5);
    }
}
