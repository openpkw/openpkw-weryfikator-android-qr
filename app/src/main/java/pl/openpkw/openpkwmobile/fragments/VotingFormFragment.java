package pl.openpkw.openpkwmobile.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import pl.openpkw.openpkwmobile.R;

/**
 * Created by fockeRR on 28.04.15.
 */
public class VotingFormFragment extends Fragment {
    private ScrollView mScrollView;
    private TextView mCommisionNumber;
    private TextView mCommisionId;
    private TextView mCommisionName;
    private TextView mCommisionAddress;
    private TableLayout mCandidates;
    private TableLayout mGeneralData;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_voting_form, container, false);
        mScrollView = (ScrollView) v.findViewById(R.id.fvoting_scrollview);
        mCommisionNumber = (TextView) v.findViewById(R.id.fvoting_commision_number);
        mCommisionId = (TextView) v.findViewById(R.id.fvoting_commision_id);
        mCommisionName = (TextView) v.findViewById(R.id.fvoting_commision_name);
        mCommisionAddress = (TextView) v.findViewById(R.id.fvoting_commision_address);
        mGeneralData = (TableLayout) v.findViewById(R.id.fvoting_generaldata);
        mCandidates = (TableLayout) v.findViewById(R.id.fvoting_candidates);
        populateDummmyData();
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void populateDummmyData() {
        mCommisionNumber.setText("Nr 5 Łódź");
        mCommisionId.setText("106101-5");
        mCommisionName.setText("Laboratorium Produkcji Ogrodniczej w Zespole Szkół Rzemiosła im. Jana Kilińskiego");
        mCommisionAddress.setText("ul. Liściasta 181, 91-220 Łódź");

        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams nameParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.7f);
        TableRow.LayoutParams votesParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.2f);
        TableRow.LayoutParams orderParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.1f);
        InputFilter[] maxVotesLength = new InputFilter[1];
        maxVotesLength[0] = new InputFilter.LengthFilter(6);
        for (int i = 0; i < 10; i++) {
            TableRow candidate = new TableRow(getActivity());
            candidate.setPadding(5, 5, 5, 5);
            TextView order = new TextView(getActivity());
            TextView name = new TextView(getActivity());
            EditText numberOfVotes = new EditText(getActivity());
            //order
            order.setText(String.valueOf(i));
            //set name
            name.setText("KOWALEWSKI Jan, Maria");
            //setMaxLength
            numberOfVotes.setMaxLines(1);
            numberOfVotes.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            numberOfVotes.setFilters(maxVotesLength);
            candidate.addView(order, orderParams);
            candidate.addView(name, nameParams);
            candidate.addView(numberOfVotes, nameParams);
            candidate.setWeightSum(1.0f);
            mCandidates.addView(candidate);
        }
    }
}
