package es.situm.gettingstarted.drawbuilding;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import es.situm.gettingstarted.R;

public class FindRouteActivity extends AppCompatActivity implements OnResultListener {

    private Button findRoute;
    private EditText txtSourceLocation;
    private EditText txtDestinationLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_route);
        findRoute = (Button) findViewById(R.id.findRoute);
        findRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(txtSourceLocation.getText().toString()) && !TextUtils.isEmpty(txtDestinationLocation.getText().toString())) {
                    Toast.makeText(FindRouteActivity.this, "Loading routes", Toast.LENGTH_LONG).show();
                    initFindRoute();
                } else {
                    Toast.makeText(FindRouteActivity.this, "Enter source and destination", Toast.LENGTH_LONG).show();
                }
            }
        });
        txtSourceLocation = (EditText) findViewById(R.id.txtSourceLocation);
        txtDestinationLocation = (EditText) findViewById(R.id.txtDestLocation);
    }

    @Override
    public void onSuccess(String result) {
        if (!TextUtils.isEmpty(result)) {

           // try {
                //JSONArray jsonArrayFindRoute = new JSONArray(result);
               // if (null != jsonArrayFindRoute && jsonArrayFindRoute.length() > 0) {




               // }
           // } catch (JSONException e) {
           //     e.printStackTrace();
           // }

        } else {

            Toast.makeText(FindRouteActivity.this, "Route not found", Toast.LENGTH_LONG).show();

        }
    }

    private void initFindRoute() {
        FindRouteAsync findRouteAsync = new FindRouteAsync();
        findRouteAsync.setOnResultListener(this, "source=" + txtSourceLocation.getText().toString() + "&desti=" + txtDestinationLocation.getText().toString());
    }
}
