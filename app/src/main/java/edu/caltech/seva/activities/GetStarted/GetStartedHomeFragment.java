package edu.caltech.seva.activities.GetStarted;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import edu.caltech.seva.R;

public class GetStartedHomeFragment extends Fragment implements View.OnClickListener {
    private Button login, get_started;

    public GetStartedHomeFragment() {

    }

    public static GetStartedHomeFragment newInstance() {
        GetStartedHomeFragment homeFragment = new GetStartedHomeFragment();
        return homeFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.get_started_home, container, false);
        login = (Button) rootView.findViewById(R.id.get_started_button);
        get_started = (Button) rootView.findViewById(R.id.login_button);
        login.setOnClickListener(this);
        get_started.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_button:
                ((GetStartedActivity)getActivity()).launchLoginScreen();

            case R.id.get_started_button:
                ((GetStartedActivity)getActivity()).setItem(1);

        }
    }

}
