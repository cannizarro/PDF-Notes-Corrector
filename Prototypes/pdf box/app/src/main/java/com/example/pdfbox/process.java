package com.example.pdfbox;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.PDPageTree;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.pdmodel.graphics.PDXObject;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class process extends AppCompatActivity {

    private static final String TAG = "process";

    //vars
    Animation anim;
    ProgressBar progressBar;
    TextView saving;
    Uri filePath;
    ImageView img;
    File root;
    InputStream file;
    String pdfName="";
    File myDir;
    Bitmap bit;
    int i;
    ArrayList<Bitmap> images;
    RecyclerView recyclerView;
    //Creating a child of AsyncTask class named Save to run the saving the image procedure for saving each image in order of their pages
    public class Save extends AsyncTask<Integer, Void, Void>
    {
        @Override
        protected Void doInBackground(Integer... imageIndices) {
            try {
                String path = myDir.getAbsolutePath() + "/" + imageIndices[0] + ".jpg";
                Log.i("pathh", path);
                FileOutputStream fileOut = new FileOutputStream(path);
                images.get(imageIndices[0]).compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
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

    public class Pdf extends AsyncTask<Void,Integer,Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {

            createPdf();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.INVISIBLE);
            saving.setVisibility(View.INVISIBLE);
            anim.cancel();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);


        //Setup

        progressBar=findViewById(R.id.progressBar);
        saving=findViewById(R.id.saving);
        progressBar.setMax(10);
        root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        PDFBoxResourceLoader.init(getApplicationContext());
        images=new ArrayList<>();

        //Setup completed

        Intent intent=getIntent();
        filePath= Uri.parse(intent.getStringExtra("Uri"));

        pdfName=filePath.getLastPathSegment();
        String[] split = pdfName.split("/");
        pdfName = split[split.length - 1];

        if(pdfName.lastIndexOf('.') > -1) {
            pdfName = pdfName.substring(0, pdfName.lastIndexOf('.'));
        }

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
            if(i == 0)
            {
                Intent intent=new Intent(process.this,MainActivity.class);
                intent.putExtra("images",i);
                startActivity(intent);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch(item.getItemId())
        {
            case R.id.pdf:

                progressBar.setVisibility(View.VISIBLE);
                saving.setVisibility(View.VISIBLE);

                anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(1000); //You can manage the blinking time with this parameter
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                saving.startAnimation(anim);
                progressBar.setProgress(0);
                Pdf obj=new Pdf();
                obj.execute();

                break;

            case R.id.left:

                rotateLeft();

                break;

            case R.id.right:

                rotateRight();

                break;

            case R.id.save:


                for(Integer iter : RecyclerViewAdapter.mSelectedIndex)
                {
                    Save save=new Save();
                    save.execute(iter);
                }

                Toast.makeText(this, "Images saved to path :" + myDir.getAbsolutePath(), Toast.LENGTH_LONG).show();

                break;

            case R.id.del:

                delete();

                break;

        }

        return false;
    }



    public void delete()
    {
        Log.i("recyy out",RecyclerViewAdapter.mSelectedIndex.toString() + "*/*///*/*/*" + images.size());
        int index;

        for(int i=0 ; i<RecyclerViewAdapter.mSelectedIndex.size() ; i++)
        {
            index=RecyclerViewAdapter.mSelectedIndex.get(i);
            images.remove((int)index);
            for(int iter : RecyclerViewAdapter.mSelectedIndex)
            {
                if(iter > index)
                {
                    RecyclerViewAdapter.mSelectedIndex.set(RecyclerViewAdapter.mSelectedIndex.indexOf(iter),iter-1);
                }
            }
            Log.i("recyy",RecyclerViewAdapter.mSelectedIndex.toString() + "*/*///*/*/*" + images.size());
        }
        RecyclerViewAdapter.mSelectedIndex.clear();
        addRecycler();

    }


    public void createPdf() {
        PDDocument document = new PDDocument();


        // Create a new font object selecting one of the PDF base fonts
        PDFont font = PDType1Font.HELVETICA;
        // Or a custom font
//        try
//        {
//            // Replace MyFontFile with the path to the asset font you'd like to use.
//            // Or use LiberationSans "com/tom_roush/pdfbox/resources/ttf/LiberationSans-Regular.ttf"
//            font = PDType0Font.load(document, assetManager.open("MyFontFile.TTF"));
//        }
//        catch (IOException e)
//        {
//            Log.e("PdfBox-Android-Sample", "Could not load font", e);
//        }

        try {
            float x,y;

            y=PDRectangle.A4.getHeight();
            x=PDRectangle.A4.getWidth()/2;
            for(Bitmap image : images)
            {


                PDPage page = new PDPage();
                document.addPage(page);
                // Define a content stream for adding to the PDF
                PDPageContentStream contentStream = new PDPageContentStream(document, page);

                PDImageXObject ximage =LosslessFactory.createFromImage(document, image);

                float w=image.getWidth();
                float h=image.getHeight();

                float x_pos = page.getCropBox().getWidth();
                float y_pos = page.getCropBox().getHeight();

                if(w>h)
                {
                    h=h*(x_pos/w);
                    w=x_pos;
                }
                else
                {
                    w=w*(y_pos/h);
                    h=y_pos;
                }

                float x_adjusted = ( x_pos - w ) / 2;
                float y_adjusted = ( y_pos - h ) / 2;

                contentStream.drawImage(ximage, x_adjusted, y_adjusted, w,h);


                    // Make sure that the content stream is closed:
                contentStream.close();
            }


            // Save the final pdf document to a file
            final String path = myDir.getAbsolutePath() + "/Created.pdf";
            document.save(path);
            document.close();



            this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(process.this, "PDF successfully written to :" + path, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            Log.e("PdfBox-Android-Sample", "Exception thrown while creating PDF", e);
        }
    }

    public void rotateLeft()
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap image;
        for(Integer index: RecyclerViewAdapter.mSelectedIndex)
        {
            image=images.get(index);
            images.set(index,Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true));
        }
        addRecycler();
    }

    public void rotateRight()
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        Bitmap image;
        for(Integer index: RecyclerViewAdapter.mSelectedIndex)
        {
            image=images.get(index);
            images.set(index,Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true));
        }
        addRecycler();
    }

}























