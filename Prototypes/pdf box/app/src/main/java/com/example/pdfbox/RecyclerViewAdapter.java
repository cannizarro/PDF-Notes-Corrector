package com.example.pdfbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";

    //vars
    ArrayList<Bitmap> mpage;
    android.view.ActionMode actionMode = null;
    // Declaring static because selected index should also exit in memeory until the recyclerVIew is up
    public static ArrayList<Integer> mSelectedIndex=new ArrayList<>();
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
        final ViewHolder viewHolder=new ViewHolder(view);


            viewHolder.parent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if(actionMode != null)
                        return false;

                    if(mSelectedIndex.size() < 1) {

                        int position = viewHolder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            //mSelectedItemPosition = position;
                            mSelectedIndex.add(position);
                            notifyDataSetChanged();
                        }
                        Log.i("recyy", mSelectedIndex.toString());
                    }

                    actionMode = ((process) mContext).startActionMode(mActionModeCallback);

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
        Glide.with(mContext).load(mpage.get(i)).into(viewHolder.page);
        viewHolder.pageNo.setText(i+1 + ".");

        if(mSelectedIndex.contains(i)){
            viewHolder.parent.setBackgroundColor(Color.RED);
        }else{
            viewHolder.parent.setBackgroundResource(R.drawable.border);
        }
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

    private android.view.ActionMode.Callback mActionModeCallback=new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater=mode.getMenuInflater();
            menuInflater.inflate(R.menu.main_context_menu,menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId())
            {
                case R.id.right:
                    process.getInstance().rotateRight();

                    return true;
                case R.id.left:
                    process.getInstance().rotateLeft();

                    return true;
                case R.id.del:
                    process.getInstance().delete();
                    mode.finish();
                    return true;
                case R.id.save:

                    for(Integer iter : RecyclerViewAdapter.mSelectedIndex)
                    {
                        process.Save save=new process.Save();
                        save.execute(iter);
                    }

                    Toast.makeText(mContext, "Images saved to path :" + process.getInstance().myDir.getAbsolutePath(), Toast.LENGTH_LONG).show();

                    return true;
                default:
                        return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

            actionMode=null;
        }
    };
}
