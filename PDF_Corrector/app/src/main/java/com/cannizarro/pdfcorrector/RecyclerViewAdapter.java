package com.cannizarro.pdfcorrector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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

    //vars
    ArrayList<Bitmap> mpage;
    ActionMode actionMode = null;
    Context mContext;

    // Declaring static because selected index should also exist in memory until the recyclerVIew is up
    public static ArrayList<Integer> mSelectedIndex=new ArrayList<>();

    public RecyclerViewAdapter(ArrayList<Bitmap> mpage, Context mContext) {
        this.mpage = mpage;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_list_item,viewGroup,false);
        final ViewHolder viewHolder=new ViewHolder(view);

            viewHolder.parent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if(actionMode != null)
                        return false;

                    if(mSelectedIndex.size() < 1) {

                        int position = viewHolder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mSelectedIndex.add(position);
                            notifyDataSetChanged();
                        }
                    }

                    actionMode = ((PDFProcess) mContext).startActionMode(mActionModeCallback);

                    return true;
                }
            });
            viewHolder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mSelectedIndex.size() > 0 ){

                    int position = viewHolder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (mSelectedIndex.contains(position))
                        {
                            mSelectedIndex.remove(new Integer(position));
                            if(mSelectedIndex.size() == 0)
                            {
                                actionMode.finish();
                            }
                        }
                        else {
                            mSelectedIndex.add(new Integer(position));
                        }
                            notifyDataSetChanged();
                    }
                }
                }
            });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {


        Glide.with(mContext).load(mpage.get(i)).into(viewHolder.page);
        viewHolder.pageNo.setText(i+1 + ".");

        if(mSelectedIndex.contains(i)){
            viewHolder.parent.setBackgroundColor(Color.parseColor("#479afc"));
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

    private ActionMode.Callback mActionModeCallback=new ActionMode.Callback() {
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
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId())
            {

                case R.id.right:
                    PDFProcess.getInstance().rotateRight();

                    return true;

                case R.id.left:
                    PDFProcess.getInstance().rotateLeft();

                    return true;

                case R.id.del:
                    new AlertDialog.Builder(PDFProcess.getInstance())
                            .setIcon(R.drawable.ic_delete_black_24dp)
                            .setTitle("Are you sure ?")
                            .setMessage("Delete the image(s) selected from current instance of the PDF.")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    PDFProcess.getInstance().delete();
                                    Toast.makeText(PDFProcess.getInstance(), "Selected image(s) deleted.", Toast.LENGTH_SHORT).show();
                                    mode.finish();
                                }
                            })
                            .setNegativeButton("No",null)
                            .show();
                    return true;

                case R.id.save:
                    new AlertDialog.Builder(PDFProcess.getInstance())
                            .setIcon(R.drawable.ic_save_black_alert_24dp)
                            .setTitle("Saving the image(s).")
                            .setMessage("Saving the image(s) to path  :-  " + PDFProcess.getInstance().myDir.getAbsolutePath())
                            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    for(Integer iter : RecyclerViewAdapter.mSelectedIndex)
                                    {
                                        PDFProcess.Save save=new PDFProcess.Save();
                                        save.execute(iter);
                                    }

                                    Toast.makeText(mContext, "Images saved to path :" + PDFProcess.getInstance().myDir.getAbsolutePath(), Toast.LENGTH_LONG).show();

                                }
                            })
                            .setNegativeButton("Don't Save",null)
                            .show();
                    return true;

                case R.id.all:

                    mSelectedIndex.clear();
                    for(int i=0;i<PDFProcess.images.size();i++)
                    {
                        mSelectedIndex.add(i);
                    }
                    PDFProcess.getInstance().addRecycler();

                    return true;

                default:
                        return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

            mSelectedIndex.clear();
            PDFProcess.getInstance().addRecycler();
            actionMode=null;
        }
    };
}
