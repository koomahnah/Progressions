package com.example.myapp;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private Button checkButton;
    private Button nextButton;
    private Button playSoundButton;
    private TextView gameMsg, gameStatus, gamePoints;
    private Integer sequenceLen;
    private Integer sequenceSpeed;
    private Integer sequenceSpacing;
    private Boolean lowerLighter;
    private Integer playClickCount = 0;
    private Double soundDuration, waitDuration;
    private ArrayList<ColorBrick> brickOrder = new ArrayList<>();
    private ArrayList<Integer> soundOrder = new ArrayList<>();
    private Integer currentRound;
    private Integer pointsEarned;
    private class ColorBrick {
        int color;
        int level;
        public ColorBrick(int level, int colorMax) {
            int whitenessFactor;
            if (lowerLighter)
                whitenessFactor = (colorMax - level) * 255 / colorMax;
            else
                whitenessFactor = (colorMax - sequenceLen + level - 1) * 255 / colorMax;
            this.color = Color.rgb(255, whitenessFactor, whitenessFactor);
            this.level = level;
        }
    }
    public interface ItemTouchHelperContract {
        void onRowMoved(int fromPosition, int toPosition);
        void onRowSelected(RecyclerViewAdapter.MyViewHolder myViewHolder);
        void onRowClear(RecyclerViewAdapter.MyViewHolder myViewHolder);
    }
    private class ItemMoveCallback extends ItemTouchHelper.Callback {
        private final ItemTouchHelperContract mAdapter;
        public ItemMoveCallback(ItemTouchHelperContract adapter) {
            mAdapter = adapter;
        }
        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }
        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        }
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            return makeMovementFlags(dragFlags, 0);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            mAdapter.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder,
                                      int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder instanceof RecyclerViewAdapter.MyViewHolder) {
                    RecyclerViewAdapter.MyViewHolder myViewHolder=
                            (RecyclerViewAdapter.MyViewHolder) viewHolder;
                    mAdapter.onRowSelected(myViewHolder);
                }
            }
            super.onSelectedChanged(viewHolder, actionState);
        }
        @Override
        public void clearView(RecyclerView recyclerView,
                              RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);

            if (viewHolder instanceof RecyclerViewAdapter.MyViewHolder) {
                RecyclerViewAdapter.MyViewHolder myViewHolder=
                        (RecyclerViewAdapter.MyViewHolder) viewHolder;
                mAdapter.onRowClear(myViewHolder);
            }
        }

    }
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>
            implements ItemTouchHelperContract {
        private ArrayList<ColorBrick> data;
        public class MyViewHolder extends RecyclerView.ViewHolder {
            View rowView;

            public MyViewHolder(View itemView) {
                super(itemView);
                rowView = itemView;
            }
        }
        public RecyclerViewAdapter(ArrayList<ColorBrick> data) {
            this.data = data;
        }
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_row, parent, false);
            return new MyViewHolder(itemView);
        }
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.rowView.setBackgroundColor(data.get(position).color);
        }
        @Override
        public int getItemCount() {
            return data.size();
        }
        @Override
        public void onRowMoved(int fromPosition, int toPosition) {
            Collections.swap(data, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
        }
        @Override
        public void onRowSelected(MyViewHolder myViewHolder) {
        }
        @Override
        public void onRowClear(MyViewHolder myViewHolder) {
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).level;
        }
    }
    private void shuffleSoundOrder(Boolean isChangeRequired) {
        if (!isChangeRequired) {
            Collections.shuffle(soundOrder);
            return;
        }
        ArrayList<Integer> oldSoundOrder = new ArrayList<>(soundOrder);
        /* Shuffle until order changes */
        while (true) {
            Collections.shuffle(soundOrder);
            for (int i = 0; i < sequenceLen; i++)
                if (soundOrder.get(i) != oldSoundOrder.get(i))
                    return;
        }

    }
    private void restartGame() {
        reorderBricksTo(Arrays.asList(1,2,3,4));
        shuffleSoundOrder(sequenceLen > 2);
        gameMsg.setText("");
        playClickCount = 0;
        nextButton.setEnabled(false);
        playSoundButton.setEnabled(true);
        checkButton.setEnabled(true);
        currentRound++;
        if (currentRound == 5) {
            nextButton.setText("FINISH");
        }
        updateGameInfo();
        playSoundSample(500);
    }
    public void onNextButtonClicked(View view) {
        Button nextButton = (Button) view;
        if (currentRound == 10) {
            this.finish();
            return;
        }
        restartGame();
        nextButton.setEnabled(false);
    }
    private void reorderBricksTo(List<Integer> order) {
        for (int i = 0; i < sequenceLen; i++) {
            int placeInBrickOrder = -1;
            for (int j = 0; j < sequenceLen; j++)
                if (brickOrder.get(j).level == order.get(i)) {
                    placeInBrickOrder = j;
                }
            if (i != placeInBrickOrder) {
                Collections.swap(brickOrder, placeInBrickOrder, i);
            }
        }
        recyclerViewAdapter.notifyDataSetChanged();
    }
    private void updateGameInfo() {
        if (pointsEarned == 1)
            gamePoints.setText(pointsEarned.toString() + " point");
        else
            gamePoints.setText(pointsEarned.toString() + " points");
        gameStatus.setText(currentRound.toString() + "/10");
    }
    public void onCheckButtonClicked(View view) {
        Boolean failed = false;

        for (int i = 0; i < sequenceLen; i++)
            if (brickOrder.get(i).level != soundOrder.get(i))
                failed = true;
        if (failed) {
            gameMsg.setText("Wrong!");
            gameMsg.setTextColor(Color.rgb(128,0,0));
            reorderBricksTo(soundOrder);
            playSoundButton.setEnabled(true);
        } else {
            gameMsg.setText("Good!");
            gameMsg.setTextColor(Color.rgb(0, 128, 0));
            pointsEarned++;
            updateGameInfo();
        }
        nextButton.setEnabled(true);
        checkButton.setEnabled(false);
    }
    private void playSoundSample(final Integer delay) {
        Thread audioThread;
        audioThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (Exception e) {
                }
                for (int i = 0; i < sequenceLen; i++) {
                    OneTimeBuzzerCorrected buzzer = new OneTimeBuzzerCorrected();
                    buzzer.setDuration(soundDuration);
                    buzzer.setVolume(40);
                    buzzer.setToneFreqInHz(800 + soundOrder.get(i) * 10 * (sequenceSpacing + 1) * (sequenceSpacing + 1));
                    buzzer.play();
                    try {
                        Thread.sleep(waitDuration.longValue());
                    } catch (Exception e) {
                    }
                }
            }
        });
        audioThread.start();
    }
    public void onPlaySoundButtonClicked(View view) {
        if (currentRound > 0)
            playSoundButton.setEnabled(false);
        if (currentRound == 0 && playClickCount > 0)
            playSoundButton.setEnabled(false);
        playClickCount++;
        playSoundSample(0);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        sequenceLen = getIntent().getIntExtra("sequenceLen", -1);
        sequenceSpacing = getIntent().getIntExtra("sequenceSpacing", -1);
        sequenceSpeed = getIntent().getIntExtra("sequenceSpeed", -1);
        lowerLighter = getIntent().getBooleanExtra("lowerLighter", true);

        switch (sequenceSpeed) {
            case 0:
                soundDuration = 1.0;
                break;
            case 1:
                soundDuration = 0.7;
                break;
            case 2:
                soundDuration = 0.4;
                break;
            case 3:
                soundDuration = 0.2;
        }
        waitDuration = 0.95 * soundDuration * 1000;

        currentRound = 0;
        pointsEarned = 0;
        recyclerView = findViewById(R.id.recyclerView);
        gameMsg = findViewById(R.id.gameMsg);
        gamePoints = findViewById(R.id.gamePoints);
        gameStatus = findViewById(R.id.gameStatus);
        checkButton = findViewById(R.id.checkButton);
        nextButton = findViewById(R.id.nextButton);
        playSoundButton = findViewById(R.id.playSoundButton);
        for (int i = 1; i <= sequenceLen; i++) {
            brickOrder.add(new ColorBrick(i, sequenceLen));
            soundOrder.add(i);
        }
        Collections.shuffle(soundOrder);
        recyclerViewAdapter = new RecyclerViewAdapter(brickOrder);
        recyclerViewAdapter.setHasStableIds(true);
        ItemTouchHelper.Callback callback =
            new ItemMoveCallback(recyclerViewAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(recyclerViewAdapter);

    }
}
