package es.situm.gettingstarted.drawbuilding;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import es.situm.gettingstarted.GettingStartedApplication;
import es.situm.gettingstarted.R;

public class QRCodeScanActivity extends AppCompatActivity {

    private int EXTERNAL_REQUEST = 1001;
    private ProgressDialogUtil progressDialogUtil;
    private String[] permissions = {Manifest.permission.CAMERA};
    private View mLine;
    private CodeScanner mCodeScanner;
    private GettingStartedApplication gettingStartedApplication;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_qrcode_scan);
        progressDialogUtil = new ProgressDialogUtil(this);
        mLine = findViewById(R.id.imgLine);
        gettingStartedApplication = (GettingStartedApplication) getApplicationContext();

        CodeScannerView scannerView = (CodeScannerView) findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(QRCodeScanActivity.this, result.getText(), Toast.LENGTH_LONG).show();
                        gettingStartedApplication.setScanDestination(result.getText());
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }
        });
    }

    private void stopPreview() {
        mCodeScanner.releaseResources();
    }

    private void startPreview() {
        mCodeScanner.startPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPreview();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPreview();
    }
}
