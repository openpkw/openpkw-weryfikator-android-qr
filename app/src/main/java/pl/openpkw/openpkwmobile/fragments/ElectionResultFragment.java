package pl.openpkw.openpkwmobile.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.utils.StringUtils;

public class ElectionResultFragment extends Fragment {

    public ElectionResultFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_election_result, container, false);
        WebView electionResultWebView = (WebView) view.findViewById(R.id.election_result_webview);
        electionResultWebView.getSettings().setJavaScriptEnabled(true); // enable javascript
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            electionResultWebView.setWebViewClient(new WebViewClient() {
                @SuppressLint("NewApi")
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {

                    Toast.makeText(getActivity().getApplicationContext(), error.getDescription(), Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            electionResultWebView.setWebViewClient(new WebViewClient() {
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Toast.makeText(getActivity().getApplicationContext(), description, Toast.LENGTH_LONG).show();
                }
            });
        }
        electionResultWebView.loadUrl(getElectionResultUrl());
        return view;
    }

    private String getElectionResultUrl(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        return sharedPref.getString(StringUtils.URL_ELECTION_RESULT_PREFERENCE, StringUtils.URL_DEFAULT_ELECTION_RESULT).trim();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}