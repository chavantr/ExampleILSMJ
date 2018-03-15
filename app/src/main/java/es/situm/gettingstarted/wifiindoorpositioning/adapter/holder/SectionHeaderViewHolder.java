package es.situm.gettingstarted.wifiindoorpositioning.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import es.situm.gettingstarted.R;


public class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
    final TextView tvTitle;


    public SectionHeaderViewHolder(View headerView) {
        super(headerView);
        tvTitle = (TextView) headerView.findViewById(R.id.tv_section_name);
    }
}
