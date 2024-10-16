package com.example.projectcampusclutchdemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class MaterialAdapter extends ArrayAdapter<Material> {

    public MaterialAdapter(Context context, List<Material> materials) {
        super(context, 0, materials);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.material_item, parent, false);
        }

        Material material = getItem(position);

        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        TextView tvDescription = convertView.findViewById(R.id.tvDescription);
        TextView tvDownload = convertView.findViewById(R.id.tvDownload);

        tvTitle.setText(material.getTitle());
        tvDescription.setText(material.getDescription());

        tvDownload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(material.getFileUrl()));
            getContext().startActivity(intent);
        });

        return convertView;
    }
}
