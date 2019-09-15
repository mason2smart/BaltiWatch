package com.appcentricity.baltiwatch.ui.login.ui.tools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appcentricity.baltiwatch.R;
import com.appcentricity.baltiwatch.RewardItem;
import com.appcentricity.baltiwatch.RewardViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ToolsFragment extends Fragment {

    private ToolsViewModel toolsViewModel;
    RecyclerView recyclerView;
    private FirestoreRecyclerAdapter<RewardItem, RewardViewHolder> adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_tools, container, false);

        recyclerView = root.findViewById(R.id.rewards);
        setupRecyclerView();

        return root;
    }

    public void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Query query = db.collection("Rewards");

        FirestoreRecyclerOptions<RewardItem> options = new FirestoreRecyclerOptions.Builder<RewardItem>().setQuery(query, RewardItem.class).build();


        adapter = new FirestoreRecyclerAdapter<RewardItem, RewardViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RewardViewHolder rewardViewHolder, int position, @NonNull RewardItem rewardItem) {
                rewardViewHolder.setAttributes(rewardItem.getCost(), rewardItem.getRewardTitle());

            }


            @NonNull
            @Override
            public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reward_item, parent, false);
                return new RewardViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        super.onStart();
        adapter.startListening();
    }

}