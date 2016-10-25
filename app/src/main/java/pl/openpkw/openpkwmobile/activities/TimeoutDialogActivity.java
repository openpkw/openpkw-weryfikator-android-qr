package pl.openpkw.openpkwmobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.utils.Utils;

public class TimeoutDialogActivity extends Activity implements DialogInterface.OnCancelListener {

    private AlertDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra("EXIT", false))
            finish();
        else
            showTimeoutDialog();

    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        finish();
    }

    private void showTimeoutDialog(){
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(TimeoutDialogActivity.this, Utils.DIALOG_STYLE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
        builder.setMessage(R.string.session_timeout_message)
                .setTitle(R.string.dialog_warning_title)
                .setCancelable(false)
                .setPositiveButton(R.string.session_timeout_login, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent loginIntent = new Intent(TimeoutDialogActivity.this, LoginActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        loginIntent.putExtra(Utils.IS_RE_LOGIN,true);
                        startActivity(loginIntent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.session_timeout_quit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent finishIntent = new Intent(TimeoutDialogActivity.this,TimeoutDialogActivity.class);
                        finishIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        finishIntent.putExtra("EXIT",true);
                        startActivity(finishIntent);
                    }
                });
        dialog = builder.create();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M)
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    public static void createDialog(Context context){
        Intent dialogIntent = new Intent(context, TimeoutDialogActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(dialogIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(dialog!=null)
            dialog.dismiss();
    }

}
