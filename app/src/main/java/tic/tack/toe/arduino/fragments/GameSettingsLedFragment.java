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

import org.json.JSONObject;

import java.util.Objects;

import tic.tack.toe.arduino.CustomColorPicker;
import tic.tack.toe.arduino.R;
import tic.tack.toe.arduino.dialog.MessageDialog;
import tic.tack.toe.arduino.game.GameSettings;
import tic.tack.toe.arduino.sockets.SocketConstants;
import tic.tack.toe.arduino.sockets.UDID;
import timber.log.Timber;

public class GameSettingsLedFragment extends BaseFragment {

    private static final String TAG = "GameSettingsLedFragment";
    private String mSelectedColor = "ff0000";
    private CustomColorPicker mColorPicker;
    private int mBrightness = 255;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.game_led_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLedColor();
            }
        });

        view.findViewById(R.id.previousButton).setOnClickListener(v -> Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStack());

        this.mColorPicker = view.findViewById(R.id.colorPicker);
        this.mColorPicker.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mColorPicker.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mColorPicker.setColor(mColorPicker.getRandomColor());
            }
        });

        this.mColorPicker.setOnColorChangedListener(color -> {
            GameSettingsLedFragment.this.mSelectedColor = String.format("%02x%02x%02x",
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color));
            GameSettings.getInstance().setPlayer_01Color(mSelectedColor);
        });
        this.mColorPicker.invalidate();

        AppCompatSeekBar seekBar = view.findViewById(R.id.seekBar);
        seekBar.setProgress(GameSettings.getInstance().getLedBrightness());
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

    private void initLedColor() {
        try {
            JSONObject messageObject = new JSONObject();
            messageObject.put(SocketConstants.TYPE, SocketConstants.LED_SETTINGS);
            messageObject.put(SocketConstants.COLOR, this.mSelectedColor);
            messageObject.put(SocketConstants.UDID, UDID.getUDID());
            messageObject.put(SocketConstants.BRIGHTNESS, this.mBrightness);

            setMessage(messageObject);
            Timber.tag(TAG).e("initLedColor");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(String message) {
        super.onMessage(message);
        try {
            JSONObject responseObject = new JSONObject(message);
            String type = responseObject.getString(SocketConstants.TYPE);
            switch (type) {
                case SocketConstants.LED_SETTINGS:
                    String symbolStatus = responseObject.getString(SocketConstants.COLOR);
                    if (symbolStatus.equals(SocketConstants.OK)) {
                        ledColorInitialized();
                    } else {
                        MessageDialog.displayDialog(getActivity(), "Błąd ustawiania koloru")
                                .show();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ledColorInitialized() {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                    .popBackStack();

            FragmentController.setCurrentFragment(Objects.requireNonNull(getActivity()),
                    new GameFragment(), false);
        });
    }

    private void setBrightness(int brightness) {
        this.mBrightness = brightness;
        GameSettings.getInstance().setLedBrightness(brightness);
    }
}
