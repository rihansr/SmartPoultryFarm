package com.rs.smartpoultryfarm.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.model.Field;
import com.rs.smartpoultryfarm.util.AppExtensions;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("NotifyDataSetChanged")
public class FieldAdapter extends RecyclerView.Adapter<FieldAdapter.ViewHolder> {

    private List<Field> fields = new ArrayList<>();

    public FieldAdapter() {}

    public void setFields(List<Field> fields) {
        if(!AppExtensions.isNullOrEmpty(fields)) this.fields = fields;
        else this.fields = new ArrayList<>();
        notifyDataSetChanged();
    }

    private Field field(int pos){
        return fields.get(pos);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.sample_field, parent, false);
        return new ViewHolder(view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final LottieAnimationView   icon;
        private final AppCompatTextView     label;
        private final AppCompatTextView     curValue;
        private final AppCompatTextView     unit;
        private final AppCompatTextView     status;
        private final AppCompatTextView     prevValue;

        private ViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.field_icon);
            label = v.findViewById(R.id.field_label);
            curValue = v.findViewById(R.id.field_cur_value);
            unit = v.findViewById(R.id.field_unit);
            status = v.findViewById(R.id.field_status);
            prevValue = v.findViewById(R.id.field_prev_value);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Field field = field(position);
        holder.icon.setAnimation(field.getIcon());
        holder.label.setText(field.getLabel());
        holder.curValue.setText(field.getCurValue() == null ? AppExtensions.string(R.string.nullSymbol) : field.getCurValue());
        holder.unit.setText(field.getUnit());
        holder.status.setText(field.getStatus());
        holder.prevValue.setVisibility(field.getPrevValue() == null ? View.GONE : View.VISIBLE);
        holder.prevValue.setText(String.format("%s %s",
                field.getPrevValue() == null ? AppExtensions.string(R.string.nullSymbol) : field.getPrevValue(),
                field.getUnit())
        );

    }

    @Override
    public int getItemCount() {
        return fields.size();
    }
}
