package com.example.projectcampusclutchdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AssignmentAdapter extends ArrayAdapter<Assignment> {
    private Context context;
    private List<Assignment> assignments;

    public AssignmentAdapter(@NonNull Context context, @NonNull List<Assignment> assignments) {
        super(context, 0, assignments);
        this.context = context;
        this.assignments = assignments;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.assignment_item, parent, false);
        }

        Assignment assignment = getItem(position); // Get the assignment at this position

        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        TextView tvDescription = convertView.findViewById(R.id.tvDescription);
        TextView tvDeadline = convertView.findViewById(R.id.tvDeadline);

        if (assignment != null) {
            tvTitle.setText(assignment.getTitle());
            tvDescription.setText(assignment.getDescription());
            tvDeadline.setText("Deadline: " + assignment.getDeadline());
        }

        return convertView;
    }
}
