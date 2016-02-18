package org.androidcru.crucentralcoast.presentation.views.ministryteams;

import android.os.Bundle;

import com.orhanobut.logger.Logger;

import org.androidcru.crucentralcoast.CruApplication;
import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.data.models.MinistryTeam;
import org.androidcru.crucentralcoast.presentation.views.forms.FormActivity;
import org.androidcru.crucentralcoast.presentation.views.forms.FormContentFragment;

import java.util.ArrayList;

public class JoinMinistryTeamActivity extends FormActivity
{
    public MinistryTeam ministryTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if(bundle == null || bundle.getString("MINISTRY_TEAM", "").isEmpty())
        {
            Logger.e("JoinMinistryTeamActivity requires that you pass an event ID.");
            Logger.e("Finishing activity...");
            finish();
            return;
        }
        else
        {
            ministryTeam = CruApplication.gson.fromJson(bundle.getString("MINISTRY_TEAM", ""), MinistryTeam.class);
            addDataObject(ministryTeam);
        }

        ArrayList<FormContentFragment> fragments = new ArrayList<>();

        MinistryTeamInformationFragment ministryTeamInformationFragment = new MinistryTeamInformationFragment();
        ministryTeamInformationFragment.setArguments(bundle);

        fragments.add(ministryTeamInformationFragment);
        fragments.add(new BasicInfoFragment());

        setAdapter(new MinistryTeamPagerAdapter(getSupportFragmentManager(), this, fragments));
    }
}