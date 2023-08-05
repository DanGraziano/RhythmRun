package edu.northeastern.rhythmrun;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RunAdapter extends RecyclerView.Adapter<RunAdapter.RunViewHolder> {

    private ArrayList<RunModel> runsList;

    public RunAdapter(ArrayList<RunModel> runsList) {
        this.runsList = runsList;
    }

    @NonNull
    @Override
    public RunViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.run_row, parent, false);
        return new RunViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RunViewHolder holder, int position) {
        RunModel run = runsList.get(position);

        // Bind the data from the RunModel to the views in the item view
        holder.numOfMiles.setText(run.getDistance());
        holder.runDate.setText(run.getDate());
        holder.runMinutes.setText(run.getTime());
        holder.miPerMin.setText(run.getPace());
    }

    @Override
    public int getItemCount() {
        return runsList.size();
    }

    static class RunViewHolder extends RecyclerView.ViewHolder {

        TextView numOfMiles;
        TextView runDate;
        TextView runMinutes;
        TextView miPerMin;

        RunViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize your TextViews or other UI elements here for the item view
            numOfMiles = itemView.findViewById(R.id.numOfMiles);
            runDate = itemView.findViewById(R.id.runDate);
            runMinutes = itemView.findViewById(R.id.runMinutes);
            miPerMin = itemView.findViewById(R.id.miPerMin);
        }
    }
}
