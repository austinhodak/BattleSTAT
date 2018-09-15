package com.respondingio.battlegroundsbuddy.stats;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.respondingio.battlegroundsbuddy.R;
import com.respondingio.battlegroundsbuddy.models.MatchData;
import java.util.List;
import nl.dionsegijn.konfetti.KonfettiView;

public class MatchesListAdapter extends RecyclerView.Adapter<MatchesListAdapter.MyViewHolder> {

    private List<MatchData> mMatchDataList;
    private Context mContext;
    private final OnItemClickListener mOnItemClickListener;

    public MatchesListAdapter(List<MatchData> matchDataList, Context context, OnItemClickListener listener) {
        this.mMatchDataList = matchDataList;
        this.mContext = context;
        this.mOnItemClickListener = listener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar mProgressBar;
        public TextView matchDuration, matchTime, placeTV, killsTV, damageTV, distanceTV;
        public ImageView mapIcon, confetti;
        public KonfettiView mKonfettiView;
        public View mView, div;

        public MyViewHolder(View view) {
            super(view);
            mView = view;

            mProgressBar = view.findViewById(R.id.match_pg);
            matchDuration = view.findViewById(R.id.match_duration);
            matchTime = view.findViewById(R.id.match_time);
            mapIcon = view.findViewById(R.id.map_icon);
            placeTV = view.findViewById(R.id.match_place);
            mKonfettiView = view.findViewById(R.id.viewKonfetti);
            killsTV = view.findViewById(R.id.match_kills);
            damageTV = view.findViewById(R.id.match_damage);
            distanceTV = view.findViewById(R.id.match_distance);
            div = view.findViewById(R.id.match_div);
            confetti = view.findViewById(R.id.confetti);
        }

        public void bind(final MatchData item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stats_match_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.matchDuration.setText("--");
        holder.matchTime.setText("--");
        holder.placeTV.setText("#--/--");
        holder.killsTV.setText("--");
        holder.damageTV.setText("--");
        holder.distanceTV.setText("--");

        MatchData matchData = mMatchDataList.get(position);
        holder.bind(matchData, mOnItemClickListener);
        if (matchData.isLoading()) {
            holder.mProgressBar.setVisibility(View.VISIBLE);
        } else {
            holder.mProgressBar.setVisibility(View.INVISIBLE);
        }

        if (matchData.getMatchTopData() != null) {
            holder.matchDuration.setText(matchData.getDuration());
            holder.matchTime.setText(matchData.getFormattedCreatedAt());
            holder.mapIcon.setImageDrawable(mContext.getResources().getDrawable(matchData.getMapIcon()));
            holder.mapIcon.setVisibility(View.VISIBLE);
        }

        if (matchData.getCurrentPlayerData() != null) {
            Long winPlace = matchData.getCurrentPlayerData().getLong("winPlace");
            Long totalPlayers = matchData.getMatchTopData().getLong("participantCount");

            holder.placeTV.setText("#" + winPlace + "/" + totalPlayers);
            holder.killsTV.setText(matchData.getCurrentPlayerData().getLong("kills").toString());
            holder.damageTV.setText(matchData.getCurrentPlayerData().getLong("damageDealt").toString());
            holder.distanceTV.setText(matchData.getTotalDistanceTravelled());

            if (winPlace == 1) {
                holder.div.setBackgroundColor(mContext.getResources().getColor(R.color.md_green_A400));
                holder.confetti.setVisibility(View.VISIBLE);
            } else if (winPlace <= 10) {
                holder.div.setBackgroundColor(mContext.getResources().getColor(R.color.md_orange_A400));
                holder.confetti.setVisibility(View.GONE);
            } else {
                holder.div.setBackgroundColor(mContext.getResources().getColor(R.color.md_light_dividers));
                holder.confetti.setVisibility(View.GONE);

            }
        }
    }

    @Override
    public int getItemCount() {
        Log.d("LIST SIZE", mMatchDataList.size() + "");
        return mMatchDataList.size();
    }


    public interface OnItemClickListener {
        void onItemClick(MatchData item);
    }


}
