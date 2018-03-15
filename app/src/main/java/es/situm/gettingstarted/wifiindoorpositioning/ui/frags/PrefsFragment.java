package es.situm.gettingstarted.wifiindoorpositioning.ui.frags;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import es.situm.gettingstarted.R;


public class PrefsFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        addPreferencesFromResource(R.xml.settings);
    }
}
