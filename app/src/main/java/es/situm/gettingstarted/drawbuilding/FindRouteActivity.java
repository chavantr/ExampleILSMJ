package es.situm.gettingstarted.drawbuilding;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import es.situm.gettingstarted.GettingStartedApplication;
import es.situm.gettingstarted.R;

public class FindRouteActivity extends AppCompatActivity implements OnResultListener, OnPointsListener {

    public static final String ROUTE = "Route";
    public static final String ID = "Id";
    public static final String S_LAT = "SLat";
    public static final String S_LON = "SLon";
    public static final String D_LAT = "DLat";
    public static final String D_LON = "DLon";
    public static final String ROUTE_TRAN = "RouteTran";
    public static final String TRAN_ID = "Id";
    public static final String LAT = "Lat";
    public static final String LNG = "Lng";
    private Button findRoute;
    private Spinner spnSourceLocation;
    private Spinner spnDestinationLocation;

    private List<String> lstSource;
    private List<String> lstDesti;
    private ProgressDialogUtil progressDialogUtil;

    private GettingStartedApplication gettingStartedApplication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_route);
        findRoute = (Button) findViewById(R.id.findRoute);


        progressDialogUtil = new ProgressDialogUtil(this);

        gettingStartedApplication = (GettingStartedApplication) getApplicationContext();
        findRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(spnSourceLocation.getSelectedItem().toString()) && !TextUtils.isEmpty(spnDestinationLocation.getSelectedItem().toString())) {
                    Toast.makeText(FindRouteActivity.this, "Loading routes", Toast.LENGTH_LONG).show();
                    initFindRoute();
                } else {
                    Toast.makeText(FindRouteActivity.this, "Enter source and destination", Toast.LENGTH_LONG).show();
                }
            }
        });

        spnSourceLocation = (Spinner) findViewById(R.id.spnStartLocation);
        spnDestinationLocation = (Spinner) findViewById(R.id.spnDestLocation);

        progressDialogUtil.show();
        GetRoutePointsAsync getRoutePointsAsync = new GetRoutePointsAsync();
        getRoutePointsAsync.setOnResultListener(this, IndoorConstants.URL + IndoorConstants.GET_ROUTE_POINTS);

    }

    @Override
    public void onSuccess(String result) {

        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject jroute = jsonObject.getJSONObject(ROUTE);
                if (null != jroute) {
                    RouteResponse routeResponse = new RouteResponse();
                    Route route = new Route();
                    route.setId(jroute.getInt(ID));
                    route.setsLatitude(jroute.getString(S_LAT));
                    route.setsLongitude(jroute.getString(S_LON));
                    route.setdLatitude(jroute.getString(D_LAT));
                    route.setdLongitude(jroute.getString(D_LON));
                    routeResponse.setRoute(route);
                    List<RouteTran> trans;
                    JSONArray jRouteTran = jsonObject.optJSONArray(ROUTE_TRAN);
                    if (null != jRouteTran && jRouteTran.length() > 0) {
                        trans = new ArrayList<>();
                        for (int i = 0; i < jRouteTran.length(); i++) {
                            JSONObject jNode = jRouteTran.getJSONObject(i);
                            if (null != jNode) {
                                RouteTran routeTran = new RouteTran();
                                routeTran.setId(jNode.getInt(TRAN_ID));
                                routeTran.setLatitude(jNode.getString(LAT));
                                routeTran.setLongitude(jNode.getString(LNG));
                                trans.add(routeTran);
                            }
                        }
                        routeResponse.setRouteTrans(trans);
                    }

                    gettingStartedApplication.setRouteResponse(routeResponse);

                    setResult(RESULT_OK);

                    finish();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(FindRouteActivity.this, "Route not found", Toast.LENGTH_LONG).show();
        }
    }

    private void initFindRoute() {
        FindRouteAsync findRouteAsync = new FindRouteAsync();
        findRouteAsync.setOnResultListener(this, "source=" + spnSourceLocation.getSelectedItem().toString() + "&desti=" + spnDestinationLocation.getSelectedItem().toString());
    }

    @Override
    public void onSuccessPoint(String result) {
        progressDialogUtil.hide();
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject jPoints = new JSONObject(result);
                JSONArray source = jPoints.getJSONArray("Source");
                JSONArray destination = jPoints.getJSONArray("Destination");
                if (null != source && source.length() > 0) {
                    lstSource = new ArrayList<>();
                    for (int i = 0; i < source.length(); i++) {
                        if (!lstSource.contains(source.getString(i)))
                            lstSource.add(source.getString(i));
                    }
                }
                if (null != destination && destination.length() > 0) {
                    lstDesti = new ArrayList<>();
                    for (int i = 0; i < destination.length(); i++) {
                        if (!lstDesti.contains(destination.getString(i)))
                            lstDesti.add(destination.getString(i));
                    }
                }

                ArrayAdapter<String> adapterSource = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, lstSource);

                adapterSource.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

                ArrayAdapter<String> adapterDest = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, lstDesti);

                adapterDest.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

                spnSourceLocation.setAdapter(adapterSource);

                spnDestinationLocation.setAdapter(adapterDest);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }


    }
}
