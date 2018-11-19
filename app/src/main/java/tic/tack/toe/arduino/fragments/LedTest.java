package tic.tack.toe.arduino.fragments;

import android.graphics.Color;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import tic.tack.toe.arduino.MainActivity;
import tic.tack.toe.arduino.R;
import tic.tack.toe.arduino.game.CMD;
import tic.tack.toe.arduino.game.FieldType;
import tic.tack.toe.arduino.game.GameSettings;

import static android.graphics.PorterDuff.Mode.SRC_IN;

public class LedTest extends BaseFragment {
    private final GameSettings gameSettings = GameSettings.getInstance();
    private final FieldType[] mFieldTypeArray = new FieldType[9];
    private ViewGroup gridLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.led_test, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.close).setOnClickListener(v ->
                getActivity().getSupportFragmentManager().popBackStack());
        MainActivity mainActivity = (MainActivity) getActivity();
        gridLayout = view.findViewById(R.id.gridLayout);

        for (int i = 0; i < 9; i++) {
            View childAt = gridLayout.getChildAt(i);
            childAt.setTag(i);
            childAt.setOnClickListener(v -> {
                int index = (Integer) v.getTag();
                mFieldTypeArray[index] = FieldType.PLAYER_01;
                mainActivity.setPixel(index, 1);
                updateUI(index);
            });
        }


        view.findViewById(R.id.reset).setOnClickListener(v -> {
            for (int i = 0; i < 9; i++) {
                mFieldTypeArray[i] = FieldType.EMPTY;
                updateUI(i);
            }
            mainActivity.writeMessage(mainActivity.hexStringToByteArray(CMD.RESET));
        });
    }

    private void updateUI(int index) {
        ImageView view = (ImageView) gridLayout.getChildAt(index);
        FieldType fieldType = this.mFieldTypeArray[index];

        switch (fieldType) {
            case EMPTY:
                view.setImageResource(0);
                break;
            case PLAYER_01:
                view.setImageResource(R.drawable.ic_game_symbol_06);
                view.setColorFilter(new PorterDuffColorFilter(
                        Color.parseColor("#" + gameSettings.getPlayer_01Color()), SRC_IN));
                break;
        }
    }
}
