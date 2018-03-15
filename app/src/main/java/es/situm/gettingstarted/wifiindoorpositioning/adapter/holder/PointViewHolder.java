package es.situm.gettingstarted.wifiindoorpositioning.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import es.situm.gettingstarted.R;


public class PointViewHolder extends RecyclerView.ViewHolder {
    final TextView tvIdentifier, tvIdentifier2, tvPointX, tvPointY;

    public PointViewHolder(View itemView) {
        super(itemView);
        tvIdentifier = (TextView) itemView.findViewById(R.id.point_identifier);
        tvIdentifier2 = (TextView) itemView.findViewById(R.id.point_identifier2);
        tvPointX = (TextView) itemView.findViewById(R.id.point_x);
        tvPointY = (TextView) itemView.findViewById(R.id.point_y);

    }
}
