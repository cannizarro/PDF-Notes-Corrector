package com.example.pdfbox;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageTree;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.graphics.PDXObject;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class process extends AppCompatActivity {

    private static final String TAG = "process";

    //vars
    Uri filePath;
    ImageView img;
    File root;
    InputStream file;
    String pdfName="";
    Bitmap bit;
    int i;
    File myDir;
    ArrayList<Bitmap> images;
    RecyclerView recyclerView;
    //Creating a child of AsyncTask class named Save to run the saving the image procedure for saving each image in order of their pages
    public class Save extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String path = myDir.getAbsolutePath() + "/" + String.valueOf(i) + ".jpg";
                Log.i("pathh", path);
                FileOutputStream fileOut = new FileOutputStream(path);
                bit.compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
                fileOut.close();

                Log.i("pdff", "Image Saved named : " + String.valueOf(i));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        //checking io permissions
        if (ContextCompat.checkSelfPermission(process.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(process.this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(process.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(process.this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        //checking permissions ended


        //Setup

        root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        PDFBoxResourceLoader.init(getApplicationContext());
        images=new ArrayList<>();

        //Setup completed

        Intent intent=getIntent();
        filePath= Uri.parse(intent.getStringExtra("Uri"));

        pdfName=filePath.getLastPathSegment();
        String[] split = pdfName.split("/");
        pdfName = split[split.length - 1];

        pdfName=pdfName.substring(0,pdfName.lastIndexOf('.'));

        //Converting Uri into input stream
        try {
            file=getContentResolver().openInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Logging for debugging
        Log.i("pathh", pdfName);

        createImages();

        Log.i("helll","Completed CreateImages() from onCreate");

        addRecycler();


    }


    public void createImages()
    {
        try {
            //Loading the pdf file
            PDDocument document = PDDocument.load(file);
            //Getting all the pages in list
            PDPageTree pages= document.getDocumentCatalog().getPages();
            Iterator iter = pages.iterator();

            myDir = new File(root.getAbsolutePath(), "PDF/" + pdfName);
            if (!myDir.exists()) {
                myDir.mkdirs();
            }

            i=0;
            while(iter.hasNext())
            {
                PDPage page=(PDPage) iter.next();
                PDResources resources=page.getResources();

                //Tom Roush code that he commented against my issue of not having resources.getImages() method
                for (COSName name : resources.getXObjectNames())
                {
                    PDXObject xobj = resources.getXObject(name);
                    if (xobj instanceof PDImageXObject)
                    {
                        bit = ((PDImageXObject)xobj).getImage();
                        //Image acquired.
                        if(bit != null) {
                            images.add(bit);
                        }
                        i=i+1;
                    }
                }
            }
            document.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.i("helll","Completed CreateImages()");
    }

    public void addRecycler()
    {
        recyclerView=findViewById(R.id.recyclerView);
        RecyclerViewAdapter adapter=new RecyclerViewAdapter(images,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


}























