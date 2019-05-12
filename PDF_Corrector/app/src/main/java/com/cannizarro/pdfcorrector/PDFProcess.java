package com.cannizarro.pdfcorrector;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.PDPageTree;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.graphics.PDXObject;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class PDFProcess extends AppCompatActivity {

    //PDF related vars
    static PDDocument document;
    Uri filePath;
    File root;
    InputStream file;
    String pdfName="";
    static File myDir;
    Bitmap bit;
    int i, countPages=0;
    static ArrayList<Bitmap> images;
    RecyclerView recyclerView;
    static PDFProcess pro;
    int noOfPages=0;
    static RecyclerViewAdapter adapter;
    static boolean isLoaded=false;


    //UI Related
    Animation anim;
    ProgressBar progressBar;
    TextView saving;




    public class CreatingImageList extends AsyncTask<PDResources,Void,Void>
    {
        @Override
        protected Void doInBackground(PDResources... resources) {
             try {
                 //Tom Roush code that he commented against my issue of not having resources.getImages() method
                 for (COSName name : resources[0].getXObjectNames()) {
                     PDXObject xobj = resources[0].getXObject(name);
                     if (xobj instanceof PDImageXObject) {
                         bit = ((PDImageXObject) xobj).getImage();
                         //Image acquired.
                         if (bit != null) {
                             images.add(bit);
                             i = i + 1;
                         }
                     }
                 }
                 countPages = countPages + 1;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            PDFProcess.getInstance().adapter.notifyDataSetChanged();


            if(countPages == noOfPages)
            {
                isLoaded=true;
                if(i == 0)
                {
                    Intent intent=new Intent(PDFProcess.this,MainActivity.class);
                    intent.putExtra("images",i);
                    startActivity(intent);
                }

                try {
                    document.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                countPages = 0;
            }

        }
    }

    //Creating a child of AsyncTask class named Save to run the saving the image procedure for saving each image in order of their pages
    public static class Save extends AsyncTask<Integer, Void, Void>
    {
        @Override
        protected Void doInBackground(Integer... imageIndices) {
            try {
                String path = myDir.getAbsolutePath() + "/" + imageIndices[0] + ".jpg";
                FileOutputStream fileOut = new FileOutputStream(path);
                images.get(imageIndices[0]).compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
                fileOut.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }


    public class PDFPages extends AsyncTask<Bitmap,Integer,Void>
    {

        @Override
        protected Void doInBackground(Bitmap... voids) {

            try {
                Bitmap image=voids[0];

                PDPage page = new PDPage();
                document.addPage(page);
                // Define a content stream for adding to the PDF
                PDPageContentStream contentStream = new PDPageContentStream(document, page);

                PDImageXObject ximage= JPEGFactory.createFromImage(document,image,0.4f);

                float w = image.getWidth();
                float h = image.getHeight();

                float x_pos = page.getCropBox().getWidth();
                float y_pos = page.getCropBox().getHeight();

                if (w > h) {
                    h = h * (x_pos / w);
                    w = x_pos;
                } else {
                    w = w * (y_pos / h);
                    h = y_pos;
                }

                float x_adjusted = (x_pos - w) / 2;
                float y_adjusted = (y_pos - h) / 2;

                contentStream.drawImage(ximage, x_adjusted, y_adjusted, w, h);

                // Make sure that the content stream is closed:
                contentStream.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            countPages = countPages + 1;

            if(countPages == images.size()) {
                try {
                    // Save the final pdf document to a file
                    final String path = myDir.getAbsolutePath() + "/" + pdfName + "_Corrected.pdf" ;

                    document.save(path);
                    document.close();

                    Toast.makeText(PDFProcess.this, "PDF successfully written to :" + path, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                progressBar.setVisibility(View.INVISIBLE);
                saving.setVisibility(View.INVISIBLE);
                anim.cancel();

            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfprocess);

        setup();


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

        addRecycler();

        createImages();


    }


    private void setup()
    {
        pro=this;

        isLoaded=false;

        recyclerView=findViewById(R.id.recyclerView);
        progressBar=findViewById(R.id.progressBar);
        saving=findViewById(R.id.saving);
        progressBar.setMax(10);
        root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        PDFBoxResourceLoader.init(getApplicationContext());
        images=new ArrayList<>();
    }


    public static PDFProcess getInstance()
    {
        return pro;
    }


    public void createImages()
    {
        try
        {
            //Loading the pdf file
            document = PDDocument.load(file);
            //Getting all the pages in list
            PDPageTree pages= document.getDocumentCatalog().getPages();

            noOfPages=pages.getCount();
            Iterator iter = pages.iterator();

            myDir = new File(root.getAbsolutePath(), "PDF Corrector/" + pdfName);
            if (!myDir.exists()) {
                myDir.mkdirs();
            }

            i=0;        // i used for counting number of images
            while(iter.hasNext())
            {
                PDPage page=(PDPage) iter.next();
                PDResources resources=page.getResources();

                CreatingImageList object= new CreatingImageList();
                object.execute(resources);

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void addRecycler()
    {
            adapter=new RecyclerViewAdapter(images, PDFProcess.getInstance());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(PDFProcess.getInstance()));
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

                if(isLoaded)
                {
                new AlertDialog.Builder(PDFProcess.getInstance())
                        .setIcon(R.drawable.ic_save_black_alert_24dp)
                        .setTitle("Saving as PDF.")
                        .setMessage("Saving as PDF to path  :-  " + PDFProcess.getInstance().myDir.getAbsolutePath())
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                    progressBar.setVisibility(View.VISIBLE);
                                    saving.setVisibility(View.VISIBLE);
                                    animate();
                                    createPdf();
                            }
                        })
                        .setNegativeButton("Don't Save",null)
                        .show();
                }
                else
                {
                    Toast.makeText(pro, "Please wait until all images get loaded.", Toast.LENGTH_SHORT).show();
                }

                return true;

            default:
                return false;

        }
    }


    public void delete()
    {
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
        }
        RecyclerViewAdapter.mSelectedIndex.clear();
        adapter.notifyDataSetChanged();

    }


    public void createPdf() {
        document = new PDDocument();

        for(Bitmap image : images)
        {
            PDFPages page=new PDFPages();
            page.execute(image);
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
        adapter.notifyDataSetChanged();
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
        adapter.notifyDataSetChanged();
    }


    public void animate()
    {
        anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(1000); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        saving.startAnimation(anim);
    }
}
