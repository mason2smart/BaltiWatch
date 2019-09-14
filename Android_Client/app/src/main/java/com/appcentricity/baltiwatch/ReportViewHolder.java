package com.appcentricity.baltiwatch;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.GeoPoint;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ReportViewHolder extends RecyclerView.ViewHolder {
    private View itemViews;

    public ReportViewHolder(View itemView) {
        super(itemView);


    }

    public void setAttributes(String typeIn, GeoPoint LocationIn) {
        DecimalFormat d = new DecimalFormat("*.##");

        TextView longitude = itemView.findViewById(R.id.Longitude);
        longitude.setText("longitude" + Math.round(LocationIn.getLongitude()*100)/100.0);
        System.out.println(LocationIn+"\n\n\n\n");
        TextView latitude = itemView.findViewById(R.id.Latitude);
        latitude.setText("latitude: " + Math.round(LocationIn.getLatitude()*100)/100.0);
        Button verify = itemView.findViewById(R.id.Verify);
        verify.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }
        );
        verify.setText("Verify");
        TextView type = itemView.findViewById(R.id.Type);
        type.setText(typeIn);
    }

}
