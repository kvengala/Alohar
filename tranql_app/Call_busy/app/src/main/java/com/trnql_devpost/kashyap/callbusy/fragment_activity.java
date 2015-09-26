package com.trnql_devpost.kashyap.callbusy;



        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.TextView;

        import com.trnql.smart.activity.ActivityEntry;
        import com.trnql.smart.base.SmartFragment;

public class fragment_activity extends SmartFragment {
    TextView activity_movement;
    TextView activity_tilting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment_activity, container, false);
        activity_movement = (TextView) view.findViewById(R.id.activity_movement);
        activity_tilting = (TextView) view.findViewById(R.id.activity_tilting);
        return view;
    }

    @Override
    protected void smartActivityChange(ActivityEntry userActivity) {
        String move_string = userActivity.getActivityString();
        String tilt_string = userActivity.isTilting() ? "Yes" : "No";

        activity_movement.setText("Movement:   %s" + move_string);
        activity_tilting.setText("Tilting:   %s" + tilt_string);
    }
}