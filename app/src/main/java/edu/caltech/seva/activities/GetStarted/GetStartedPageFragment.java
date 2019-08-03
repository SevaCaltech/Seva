package edu.caltech.seva.activities.GetStarted;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.caltech.seva.R;

public class GetStartedPageFragment extends Fragment implements View.OnClickListener{
    private static final String POSITION = "POSITION";
    private String[] descriptionArr = { "1. Our sensors tell technicians somthing is wrong.",
            "2. Our app guides you step by step through the repair process.",
            "3. Once you have completed the repair, our sensors confirm that everything is" +
                    " fixed and working."};
    private String[] picArr = {"getstarted1", "getstarted2", "getstarted3"};
    private String titleStr = "Welcome to the Seva app!";
    private TextView[] dots;
    private LinearLayout dotsLayout;


    public GetStartedPageFragment() {

    }
    public static GetStartedPageFragment newInstance(int position) {
        GetStartedPageFragment pageFragment = new GetStartedPageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(POSITION, position);
        pageFragment.setArguments(bundle);
        return pageFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.get_started_page1, container, false);
        Bundle arguments = getArguments();
        int position = arguments.getInt(POSITION);

        Button login = (Button) rootView.findViewById(R.id.ok_login);
        TextView description = (TextView) rootView.findViewById(R.id.get_started_description);
        TextView desc_noTitle = (TextView) rootView.findViewById(R.id.get_started_desc_no_title);
        TextView title = (TextView) rootView.findViewById(R.id.get_started_title);
        ImageView getStartedPic = (ImageView) rootView.findViewById(R.id.get_started_pic);
        dotsLayout = (LinearLayout) rootView.findViewById(R.id.layoutDots);
        addBottomDots(position-1);
        login.setVisibility(View.GONE);
        login.setOnClickListener(this);

        if (position == 1) {
            title.setText(titleStr);
            description.setVisibility(View.VISIBLE);
            desc_noTitle.setVisibility(View.GONE);
        }
        else {
            title.setVisibility(View.GONE);
            desc_noTitle.setVisibility(View.VISIBLE);
            description.setVisibility(View.GONE);
        }

        if (position == 3)
            login.setVisibility(View.VISIBLE);
        description.setText(descriptionArr[position-1]);
        desc_noTitle.setText(descriptionArr[position-1]);
        int picID = getResources().getIdentifier(picArr[position-1], "drawable", getActivity().getPackageName());
        getStartedPic.setImageResource(picID);
        return rootView;
    }

    public void addBottomDots(int currentPage) {
        dots = new TextView[3];
        int colorActive = getResources().getInteger(R.color.dot_active);
        int colorInactive = getResources().getInteger(R.color.dot_inactive);
        dotsLayout.removeAllViews();
        for(int i=0; i<dots.length; i++) {
            dots[i] = new TextView(getContext());
            dots[i].setText(Html.fromHtml("•"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorInactive);
            dotsLayout.addView(dots[i]);
        }
        if (dots.length > 0)
            dots[currentPage].setTextColor(colorActive);
    }

    @Override
    public void onClick(View view) {
        ((GetStartedActivity) getActivity()).launchLoginScreen();
    }
}
