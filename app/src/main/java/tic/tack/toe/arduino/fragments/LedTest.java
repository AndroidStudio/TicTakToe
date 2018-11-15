package tic.tack.toe.arduino.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tic.tack.toe.arduino.MainActivity;
import tic.tack.toe.arduino.R;
import tic.tack.toe.arduino.game.CMD;

public class LedTest extends BaseFragment {

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
        ViewGroup gridLayout = view.findViewById(R.id.gridLayout);

        for (int i = 0; i < 9; i++) {
            View childAt = gridLayout.getChildAt(i);
            childAt.setTag(i);
            childAt.setOnClickListener(v -> {
                int index = (Integer) v.getTag();
                mainActivity.setPixel(index, 1);
            });
        }

        view.findViewById(R.id.reset).setOnClickListener(v ->
                mainActivity.writeMessage(mainActivity.hexStringToByteArray(CMD.RESET)));
    }
}
