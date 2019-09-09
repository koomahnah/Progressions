package com.example.myapp;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class MainActivity extends AppCompatActivity {

    private Integer sequenceLenSelected = -1;
    private Integer speedSelected = -1;
    private Integer spacingSelected = -1;
    private Boolean lowerLighterSelected = true;
    public void onPlayButtonClicked(View view) {
        SeekBar speedBar = findViewById(R.id.speedSeekBar);
        SeekBar spacingBar = findViewById(R.id.spacingSeekBar);
        Switch lowerLighterSwitch = findViewById(R.id.lowerLighterSwitch);
        speedSelected = speedBar.getProgress();
        spacingSelected = spacingBar.getProgress();
        lowerLighterSelected = lowerLighterSwitch.isChecked();
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("sequenceLen", sequenceLenSelected);
        intent.putExtra("sequenceSpeed", speedSelected);
        intent.putExtra("sequenceSpacing", spacingSelected);
        intent.putExtra("lowerLighter", lowerLighterSelected);
        startActivity(intent);
    }
    public void onSequenceButtonClicked(View view) {
        Button playButton = findViewById(R.id.playButton);
        playButton.setEnabled(true);
        RadioButton seqButton = (RadioButton) view;
        if (seqButton == findViewById(R.id.sequence2Button))
            sequenceLenSelected = 2;
        else if (seqButton == findViewById(R.id.sequence3Button))
            sequenceLenSelected = 3;
        else if (seqButton == findViewById(R.id.sequence4Button))
            sequenceLenSelected = 4;
        else if (seqButton == findViewById(R.id.sequence5Button))
            sequenceLenSelected = 5;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
