package com.example.actionbar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    android.view.ActionMode actionMode = null;
    //vars
    ArrayList<Integer> mpage;
    // Declaring static because selected index should also exit in memeory until the recyclerVIew is up
    public static ArrayList<Integer> mSelectedIndex=new ArrayList<>();
    Context mContext;
    MainActivity activity;

    public RecyclerViewAdapter(ArrayList<Integer> mpage, Context mContext) {
        this.mpage = mpage;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_list_item,viewGroup,false);
        Log.i("helll","assigning to viewholder");
        final ViewHolder viewHolder=new ViewHolder(view);


            viewHolder.parent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if(mSelectedIndex.size() < 1) {

                        int position = viewHolder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            //mSelectedItemPosition = position;
                            mSelectedIndex.add(position);
                            notifyDataSetChanged();
                        }
                        Log.i("recyy", mSelectedIndex.toString());
                    }



                    return true;
                }
            });
            viewHolder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mSelectedIndex.size() >=1 ){

                    int position = viewHolder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (mSelectedIndex.contains(position))
                        {
                            mSelectedIndex.remove(new Integer(position));
                        }
                        else {
                            mSelectedIndex.add(new Integer(position));
                        }
                            notifyDataSetChanged();
                    }
                    Log.i("recyy",mSelectedIndex.toString());
                }
                }
            });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {

        Log.d(TAG, "onBindViewHolder: called.");
        viewHolder.pageNo.setText(Integer.toString(mpage.get(i)));


        if(mSelectedIndex.contains(i)) {
            Log.i("Red  =  ",Integer.toString(i));
            viewHolder.parent.setBackgroundColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return mpage.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView pageNo;
        RelativeLayout parent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            pageNo=itemView.findViewById(R.id.pageNo);
            parent=itemView.findViewById(R.id.parent_layout);
        }
    }

}




