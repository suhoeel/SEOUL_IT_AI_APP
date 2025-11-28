package com.portfolio.school.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.portfolio.school.MainActivity;
import com.portfolio.school.R;
import com.portfolio.school.customview.TypeWriter;
import com.portfolio.school.model.DataResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private ArrayList<DataResponse> list;
    private final ChatHistoryListener chatHistoryListener;

    public ChatHistoryAdapter(
            Context context,
            ChatHistoryListener chatHistoryListener
    ) {
        this.context = context;
        this.chatHistoryListener = chatHistoryListener;
        list = new ArrayList<>();
    }

    public void setList(
            List<DataResponse> items
    ) {
        list.clear();
        list.addAll(items);
        notifyDataSetChanged();
    }

    private class ChatHistoryViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout parent;
        TextView title;
        ImageView deleteView;

        public ChatHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.item_chat_history);
            title = itemView.findViewById(R.id.title);
            deleteView = itemView.findViewById(R.id.iv_delete);
        }

        void bind(int position) {
            DataResponse data = list.get(position);

            Date date = new Date(data.getTimestamp());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            String formattedDate = localDate.format(formatter);
            title.setText(formattedDate);

            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("history", true);
                    intent.putExtra("date", formattedDate);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });

            deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(
                            context,
                            com.google.android.material.R.style.MaterialAlertDialog_Material3
                    );
                    dialog.setTitle("삭제 확인");
                    dialog.setMessage("정말로 삭제하시겠습니까?");
                    dialog.setPositiveButton("삭제", (dialogInterface, i) -> {
                        chatHistoryListener.delete(formattedDate);
                        list.remove(position);
                        notifyItemRemoved(position);
                    });
                    dialog.setNegativeButton("취소", (dialogInterface, i) -> {

                    });
                    dialog.show();
                }
            });


        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_history_layout, parent, false);
        return new ChatHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ChatHistoryViewHolder) holder).bind(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
