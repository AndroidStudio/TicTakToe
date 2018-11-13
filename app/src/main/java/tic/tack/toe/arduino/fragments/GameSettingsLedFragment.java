package tic.tack.toe.arduino.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import org.json.JSONObject;

import java.util.Objects;

import tic.tack.toe.arduino.R;
import tic.tack.toe.arduino.dialog.MessageDialog;
import tic.tack.toe.arduino.game.GameSettings;
import tic.tack.toe.arduino.sockets.SocketConstants;
import tic.tack.toe.arduino.sockets.UDID;
import timber.log.Timber;

public class GameSettingsLedFragment extends BaseFragment {

    private static final String TAG = "GameSettingsLedFragment";
    private int mBrightness = 255;
    private String mSelectedColor = null;

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
        view.findViewById(R.id.nextButton).setOnClickListener(v -> initLedColor());

        view.findViewById(R.id.previousButton).setOnClickListener(v -> Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStack());
        view.findViewById(R.id.redButton).setOnClickListener(v -> setColor(v, "ff0000"));
        view.findViewById(R.id.greenButton).setOnClickListener(v -> setColor(v, "04ff00"));
        view.findViewById(R.id.blueButton).setOnClickListener(v -> setColor(v, "0008ff"));
        view.findViewById(R.id.whiteButton).setOnClickListener(v -> setColor(v, "f3f3f3"));

        setColor(view.findViewById(R.id.redButton), "ff0000");

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

    private void setColor(View view, String color) {
        View v = getView();
        this.mSelectedColor = color;

        v.findViewById(R.id.redButton).setScaleX(1);
        v.findViewById(R.id.redButton).setScaleY(1);

        v.findViewById(R.id.greenButton).setScaleX(1);
        v.findViewById(R.id.greenButton).setScaleY(1);

        v.findViewById(R.id.blueButton).setScaleX(1);
        v.findViewById(R.id.blueButton).setScaleY(1);

        v.findViewById(R.id.whiteButton).setScaleX(1);
        v.findViewById(R.id.whiteButton).setScaleY(1);

        view.setScaleX(1.2f);
        view.setScaleY(1.2f);

        View selectedColorView = v.findViewById(R.id.selectedColorView);
        selectedColorView.setBackgroundColor(Color.parseColor("#" + color));
    }

    private void initLedColor() {
        String color = this.mSelectedColor;
        GameSettings.getInstance().setPlayer_01Color(color);
        try {
            JSONObject messageObject = new JSONObject();
            messageObject.put(SocketConstants.TYPE, SocketConstants.LED_SETTINGS);
            messageObject.put(SocketConstants.COLOR, color);
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
                        MessageDialog.displayDialog(getActivity(), "Kolor niedostępny")
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
