package pl.openpkw.openpkwmobile.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import pl.openpkw.openpkwmobile.R;

public class SplashFragment extends Fragment {

    private TextView rulesTv;

    public SplashFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_splash, container, false);

        rulesTv = (TextView) rootView.findViewById(R.id.rulesTV);

        rulesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getResources().getString(R.string.rules_url);
                Intent rulesIntent = new Intent(Intent.ACTION_VIEW);
                rulesIntent.setData(Uri.parse(url));
                startActivity(rulesIntent);
            }
        });

        return rootView;
    }
}
