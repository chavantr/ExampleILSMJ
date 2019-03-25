package es.situm.gettingstarted.drawbuilding;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import es.situm.gettingstarted.R;
import es.situm.gettingstarted.drawbuilding.models.UserInfoHolder;

public class LoginActivity extends AppCompatActivity implements OnLoginListener {

    private ProgressDialogUtil progressDialogUtil;
    private EditText txtUsername;
    private EditText txtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtPassword = (EditText)findViewById(R.id.txtPassword);
        txtUsername = (EditText)findViewById(R.id.txtUsername);


        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    init();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.btnSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, TeacherAgreementActivity.class);
                startActivity(intent);
            }
        });

        progressDialogUtil = new ProgressDialogUtil(this);
    }


    private void notifyUser() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Login")
                .setMessage("Login Successful, You are agree to share your current location")
                .setCancelable(true)
                .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();

    }

    private void init() throws JSONException {
        progressDialogUtil.show();
        LoginAsync loginAsync = new LoginAsync();
        JSONObject jRequest = new JSONObject();
        JSONObject param = new JSONObject();
        param.put("Username", txtUsername.getText().toString());
        param.put("Password", txtPassword.getText().toString());
        jRequest.put("request", param);
        loginAsync.setOnLoginListener(this, jRequest);
    }

    @Override
    public void onLoginSuccess(Teacher result) {
        if (null != result) {
            UserInfoHolder.getInstance().setTeacher(result);
            notifyUser();
        } else {
            Toast.makeText(this, "Please enter valid username or password", Toast.LENGTH_LONG).show();
        }
    }


}
