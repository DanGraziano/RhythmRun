package edu.northeastern.rhythmrun;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RunAdapter extends RecyclerView.Adapter<RunAdapter.RunViewHolder> implements View.OnClickListener {

    private ArrayList<RunModel> runsList;
    private Context context;
    private RecyclerView recyclerView;

    public RunAdapter(ArrayList<RunModel> runsList) {
        this.runsList = runsList;
    }

    public RunAdapter(ArrayList<RunModel> runsList, RecyclerView recyclerView) {
        this.runsList = runsList;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public RunViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.run_row, parent, false);
        return new RunViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull RunViewHolder holder, int position) {
        RunModel run = runsList.get(position);

        // Bind the data from the RunModel to the views in the item view
        holder.numOfMiles.setText(run.getDistance());
        holder.runDate.setText(run.getDate());
        holder.runMinutes.setText(run.getTime());
        holder.miPerMin.setText(run.getAvgPace());

        // Set click listener on the item view
        holder.itemView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return runsList.size();
    }

    @Override
    public void onClick(View view) {
        // Get the position of the clicked item
        int position = recyclerView.getChildLayoutPosition(view);

        // Get the corresponding RunModel
        RunModel run = runsList.get(position);

        // TODO Replace intent with correct post-workout stats class instead of Settings
        Intent intent = new Intent(context, Settings.class);
        /* Can use if we don't want to pull from DB

        intent.putExtra("date", run.getDate());
        intent.putExtra("distance", run.getDistance());
        intent.putExtra("avgCadence", run.getAvgCadence());
        intent.putExtra("avgPace", run.getAvgPace());
        intent.putExtra("time", run.getTime());
*/
        // Start the RunDetailActivity or RunDetailFragment
        context.startActivity(intent);
    }

    static class RunViewHolder extends RecyclerView.ViewHolder {

        TextView numOfMiles;
        TextView runDate;
        TextView runMinutes;
        TextView miPerMin;

        RunViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize TextViews
            numOfMiles = itemView.findViewById(R.id.numOfMiles);
            runDate = itemView.findViewById(R.id.runDate);
            runMinutes = itemView.findViewById(R.id.runMinutes);
            miPerMin = itemView.findViewById(R.id.miPerMin);
        }
    }
}