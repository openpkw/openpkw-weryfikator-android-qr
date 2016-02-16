package pl.openpkw.openpkwmobile.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import pl.openpkw.openpkwmobile.R;

public class PasswordRestoreFragment extends Fragment {
    private Button prestoreButton;
    private EditText prestoreEmail;
    private Toast validationFailed;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_password_restore, container, false);
        prestoreButton = (Button) v.findViewById(R.id.prestore_button_restore);
        prestoreEmail = (EditText) v.findViewById(R.id.prestore_edittext_email);
        prestoreButton.setOnClickListener(onRestoreButtonClick);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        validationFailed = Toast.makeText(getActivity().getApplicationContext(),
                getActivity().getString(R.string.toast_prestore_emailnotvalid),
                Toast.LENGTH_SHORT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (validationFailed != null)
            validationFailed.cancel();
    }

    View.OnClickListener onRestoreButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CharSequence emailSequence = prestoreEmail.getText();
            if (emailSequence != null && emailSequence.toString().trim().length() > 0)
                emailSequence = emailSequence.toString().trim();

            if (!isEmailValid(emailSequence)) {
                validationFailed.show();
                return;
            }
            //TODO: send request to server
            Toast.makeText(getActivity().getApplicationContext(),
                    getActivity().getString(R.string.toast_prestore_emailvalid),
                    Toast.LENGTH_LONG).show();
            Activity parent = getActivity();
            if (parent != null) {
                parent.finish();
            }
        }
    };

    private boolean isEmailValid(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
