package tic.tack.toe.arduino.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.SeekBar;

import com.github.mata1.simpledroidcolorpicker.interfaces.OnColorChangedListener;

import java.util.Objects;

import tic.tack.toe.arduino.CustomColorPicker;
import tic.tack.toe.arduino.R;
import tic.tack.toe.arduino.game.GameSettings;

public class GameSettingsLedFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.game_led_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentController.setCurrentFragment(Objects.requireNonNull(getActivity()),
                        new GameFragment());
            }
        });

        view.findViewById(R.id.previousButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentController.setCurrentFragment(Objects.requireNonNull(getActivity()),
                        new GameSymbolFragment());
            }
        });

        final CustomColorPicker colorPicker = view.findViewById(R.id.colorPicker);
        colorPicker.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                colorPicker.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                colorPicker.setColor(Color.RED);
            }
        });

        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                String hex = String.format("%02x%02x%02x",
                        Color.red(color),
                        Color.green(color),
                        Color.blue(color));
                setLedColor(hex);
            }
        });
        colorPicker.invalidate();

        AppCompatSeekBar seekBar = view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setBrightness(seekBar.getProgress());
            }
        });
    }

    private void setBrightness(int brightness) {
        GameSettings.getInstance().setLedBrightness(brightness);
    }

    private void setLedColor(String color) {
        try {
            Color.parseColor(String.valueOf("#".concat(color)));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        GameSettings.getInstance().setPlayer_01Color(color);
    }
}
