package com.appcentricity.baltiwatch;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ReportViewHolder extends RecyclerView.ViewHolder {
    private View itemViews;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public ReportViewHolder(View itemView) {
        super(itemView);


    }

    public void setAttributes(String typeIn, GeoPoint LocationIn, String Id) {
        DecimalFormat d = new DecimalFormat("*.##");

        TextView longitude = itemView.findViewById(R.id.Longitude);
        longitude.setText("longitude" + Math.round(LocationIn.getLongitude()*100)/100.0);

        TextView latitude = itemView.findViewById(R.id.Latitude);
        latitude.setText("latitude: " + Math.round(LocationIn.getLatitude()*100)/100.0);

        Button resolve = itemView.findViewById(R.id.Resolve);
        resolve.setText("Resolve");

        final String thisId = Id;
        resolve.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        db.collection("Reports").document(thisId).delete();
                    }
                }
        );
        resolve.setText("Verify");
        TextView type = itemView.findViewById(R.id.Type);
        type.setText(typeIn);
    }

}
