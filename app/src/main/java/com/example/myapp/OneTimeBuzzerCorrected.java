package com.example.myapp;


import android.util.Log;

import net.mabboud.android_tone_player.OneTimeBuzzer;

import static java.lang.Math.exp;
import static java.lang.Math.pow;

public class OneTimeBuzzerCorrected extends OneTimeBuzzer {
    @Override
    protected void playTone(double freqInHz, double seconds) {
        int sampleRate = 32000;

        double dnumSamples = seconds * sampleRate;
        dnumSamples = Math.ceil(dnumSamples);
        int numSamples = (int) dnumSamples;
        double sample[] = new double[numSamples];
        byte soundData[] = new byte[2 * numSamples];

        // Fill the sample array
        for (int i = 0; i < numSamples; ++i)
            sample[i] = Math.sin(freqInHz * 2 * Math.PI * i / (sampleRate));

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalized.
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        int i = 0;

        // Amplitude ramp as a percent of sample count
        int ramp = numSamples / 6;

        // Ramp amplitude up (to avoid clicks)
        for (i = 0; i < ramp; ++i) {
            double dVal = sample[i];
            // Ramp down to zero
            double coeff = (double)(i)/(double)(ramp);
            coeff = pow(coeff, 4);
            final short val = (short) (dVal * 32767 * coeff);
            // in 16 bit wav PCM, first byte is the low order byte
            soundData[idx++] = (byte) (val & 0x00ff);
            soundData[idx++] = (byte) ((val & 0xff00) >>> 8);
        }


        // Max amplitude for most of the samples
        for (i = i; i < numSamples - ramp; ++i) {
            double dVal = sample[i];
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            soundData[idx++] = (byte) (val & 0x00ff);
            soundData[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        /*
        Log.e("Buzzer", "numSamples is "+ Integer.valueOf(numSamples).toString());
        Log.e("Buzzer", "ramp is "+ Integer.valueOf(ramp).toString());
        Log.e("Buzzer", "i is "+ Integer.valueOf(i).toString());
        Log.e("Buzzer", "numSamples - i is "+ Integer.valueOf(numSamples - i).toString());
        */
        // Ramp amplitude down
        for (i = i; i < numSamples; ++i) {
            double dVal = sample[i];
            // Ramp down to zero
            double coeff = (double)(numSamples - i)/(double)(ramp);
            coeff = pow(coeff, 4);
            final short val = (short) (dVal * 32767 * coeff);
            // in 16 bit wav PCM, first byte is the low order byte
            soundData[idx++] = (byte) (val & 0x00ff);
            soundData[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        playSound(sampleRate, soundData);
    }
}
