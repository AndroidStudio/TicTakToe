package tic.tack.toe.arduino.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import tic.tack.toe.arduino.R;
import tic.tack.toe.arduino.sockets.UDID;

public class DiagnosticFragment extends BaseFragment {

    private TextView diagnosticTextView;

    boolean loggingEnabled = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.diagnostic_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        diagnosticTextView = view.findViewById(R.id.diagnosticTextView);
        TextView identifier = view.findViewById(R.id.identifier);
        identifier.setText(String.valueOf("Gracz: " + UDID.getUDID()));

        view.findViewById(R.id.close).setOnClickListener(v -> getActivity()
                .getSupportFragmentManager().popBackStack());
        Button stop = view.findViewById(R.id.stop);
        stop.setOnClickListener(v -> {
            loggingEnabled = !loggingEnabled;
            if (loggingEnabled) {
                stop.setText("Wyłącz logi");
            } else {
                stop.setText("Włącz logi");
            }
        });
    }

    @Override
    public void onMessage(String message) {
        if (!TextUtils.isEmpty(message) && diagnosticTextView != null && loggingEnabled) {
            String text = diagnosticTextView.getText().toString();
            if (text.length() > 10000) {
                text = "";
            }
            diagnosticTextView.setText("server-->" + message + "\n" + text);
        }

    }
}
