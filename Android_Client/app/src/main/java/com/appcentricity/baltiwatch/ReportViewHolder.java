package com.appcentricity.baltiwatch;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

public class ReportViewHolder extends RecyclerView.ViewHolder {
    private View itemViews;

    public ReportViewHolder(View itemView) {
        super(itemView);


    }

    public void setAttributes(String typeIn, double longitudeIn, double latitudeIn) {
        TextView longitude = itemView.findViewById(R.id.Longitude);
        longitude.setText("" + longitudeIn);
        TextView latitude = itemView.findViewById(R.id.Latitude);
        latitude.setText("" + latitudeIn);
        Button verify = itemView.findViewById(R.id.Verify);
        verify.setText("Verify");
        TextView type = itemView.findViewById(R.id.Type);
        type.setText(typeIn);
    }

}
