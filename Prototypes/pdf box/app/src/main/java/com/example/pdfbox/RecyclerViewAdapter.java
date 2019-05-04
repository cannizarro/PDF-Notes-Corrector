package com.example.pdfbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";

    //vars
    ArrayList<Bitmap> mpage;
    Context mContext;

    public RecyclerViewAdapter(ArrayList<Bitmap> mpage, Context mContext) {
        this.mpage = mpage;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_list_item,viewGroup,false);
        Log.i("helll","assigning to viewholder");
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder,final int i) {

        Log.d(TAG, "onBindViewHolder: called.");
        viewHolder.page.setImageBitmap(mpage.get(i));
        viewHolder.pageNo.setText(String.valueOf(i+1) + ".");
        viewHolder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, String.valueOf(i), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mpage.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView page;
        TextView pageNo;
        RelativeLayout parent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            page=itemView.findViewById(R.id.page);
            pageNo=itemView.findViewById(R.id.pageNo);
            parent=itemView.findViewById(R.id.parent_layout);
        }
    }
}
