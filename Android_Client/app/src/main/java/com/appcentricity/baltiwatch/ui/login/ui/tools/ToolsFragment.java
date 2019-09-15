package com.appcentricity.baltiwatch.ui.login.ui.tools;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appcentricity.baltiwatch.R;
import com.appcentricity.baltiwatch.RewardItem;
import com.appcentricity.baltiwatch.RewardViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

public class ToolsFragment extends Fragment {
    FirebaseAuth auth;
    TextView rewardsPts;
    private ToolsViewModel toolsViewModel;
    RecyclerView recyclerView;
    private FirestoreRecyclerAdapter<RewardItem, RewardViewHolder> adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_tools, container, false);

        recyclerView = root.findViewById(R.id.rewards);
        setupRecyclerView();
       // View view =  inflater.inflate(R.layout.fragment_tools, container, false);

        rewardsPts = root.findViewById(R.id.numPoints);
        updateRewards();
        return root;
    }

    public void updateRewards() {
        auth = FirebaseAuth.getInstance();
        try {
            final DocumentReference docRef = db.collection("users").document(auth.getUid());
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w("report", "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) { //update rewards on change
                            rewardsPts.setText("Rewards Points: " + snapshot.getDouble("rewards").intValue() + " pts");
                    } else {
                        Log.d("report", "Current data: null");
                    }
                }
            });
        }
        catch (NullPointerException e){
            Log.e("report", "ERROR UPDATING REWARDS: "+e.toString());
        }
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