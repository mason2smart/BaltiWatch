package com.appcentricity.baltiwatch.ui.login.ui.verification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appcentricity.baltiwatch.R;
import com.appcentricity.baltiwatch.ReportItem;
import com.appcentricity.baltiwatch.ReportViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class VerificationFragment extends Fragment {

    RecyclerView recyclerView;
    private FirestoreRecyclerAdapter<ReportItem, ReportViewHolder> adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_verification, container, false);
        recyclerView = root.findViewById(R.id.reports);
        setupRecycler();
        return root;
    }

    //set up recyclerview in gallery.java
    public void setupRecycler() {

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        // recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // Query Firestore
        Query query = db.collection("Reports");

        FirestoreRecyclerOptions<ReportItem> options = new FirestoreRecyclerOptions.Builder<ReportItem>().setQuery(query, ReportItem.class).build();

        adapter = new FirestoreRecyclerAdapter<ReportItem, ReportViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ReportViewHolder reportViewHolder, int position, @NonNull ReportItem reportItem) {
                reportViewHolder.setAttributes(reportItem.getType(), reportItem.getLocation(), reportItem.getId());
            }

            @NonNull
            @Override
            public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_item, parent, false);
                return new ReportViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        super.onStart();
        adapter.startListening();

    }
}