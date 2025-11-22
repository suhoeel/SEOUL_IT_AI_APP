package com.portfolio.school.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.portfolio.school.R;
import com.portfolio.school.customview.TypeWriter;
import com.portfolio.school.model.DataResponse;

import java.util.ArrayList;
import java.util.List;

public class GeminiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int USER = 0;
    public static final int GEMINI = 1;

    private final Context context;
    private ArrayList<DataResponse> list;

    public GeminiAdapter(Context context) {
        this.context = context;
        list = new ArrayList<>();
    }

    public void add(
            DataResponse item
    ) {
        list.add(item);
        notifyItemInserted(list.size() - 1);
    }

    public void addAll(
            List<DataResponse> items
    ) {
        list.clear();
        list.addAll(items);
        notifyDataSetChanged();
    }

    private class GeminiViewHolder extends RecyclerView.ViewHolder {
        TypeWriter text;

        public GeminiViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tv_gemini_response);
        }

        void bind(int position) {
            DataResponse data = list.get(position);
            if (getAdapterPosition() == list.size() - 1) {
                if(data.isAnimate()) {
                    text.animateText(data.getPrompt());
                    text.setCharacterDelay(30);
                } else {
                    text.setText(data.getPrompt());
                }
            } else {
                text.setText(data.getPrompt());
            }
        }
    }

    private class UserViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        CardView cardView;
        ImageView image;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tv_user_response);
            cardView = itemView.findViewById(R.id.iv_user_res_card);
            image = itemView.findViewById(R.id.iv_user_res);
        }

        void bind(int position) {
            DataResponse data = list.get(position);
            text.setText(data.getPrompt());

            // 모델에 저장된 Bitmap을 직접 사용
            if (data.getBitmap() != null) {
                cardView.setVisibility(View.VISIBLE);
                image.setVisibility(View.VISIBLE);
                image.setImageBitmap(data.getBitmap());
            } else {
                cardView.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == GEMINI) {
            View view = LayoutInflater.from(context).inflate(R.layout.gemini_layout, parent, false);
            return new GeminiViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.user_layout, parent, false);
            return new UserViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == GEMINI) {
            ((GeminiViewHolder) holder).bind(position);
        } else {
            ((UserViewHolder) holder).bind(position);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).isUser();
    }
}
