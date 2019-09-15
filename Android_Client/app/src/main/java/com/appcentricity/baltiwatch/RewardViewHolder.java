package com.appcentricity.baltiwatch;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;


public class RewardViewHolder extends RecyclerView.ViewHolder {
    TextView thisCost;
    TextView thisRewardTitle;
    Button redeem;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public RewardViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void setAttributes(String Cost, String rewardTitle) {
        thisCost = itemView.findViewById(R.id.Cost);
        thisCost.setText("Cost: " + Cost + " Points.");
        final int inCost = Integer.parseInt(Cost);

        redeem = itemView.findViewById(R.id.Redeem);
        redeem.setText("Redeem");
        redeem.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
                if (usr != null) {
                    String uid = usr.getUid();
                    db.document("users/" + usr.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot doc = task.getResult();
                            Map<String, Object> map = doc.getData();
                            long rewards = (long)map.get("rewards");
                            if (inCost <= rewards) {
                                db.collection("users").document(usr.getUid()).update("rewards", rewards - inCost);
                            }
                        }
                    });
                }
            }


            private void addRewards(int points) {
                FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
                if (usr != null) {
                    String uid = usr.getUid();
                    DocumentReference userRewardsRef = db.collection("users").document(uid);
                    userRewardsRef.update("rewards", FieldValue.increment(points));
                }
            }
        });

        thisRewardTitle = itemView.findViewById(R.id.rewardTitle);
        thisRewardTitle.setText(rewardTitle);

    }
}
