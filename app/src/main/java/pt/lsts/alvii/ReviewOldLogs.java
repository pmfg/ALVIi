package pt.lsts.alvii;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Pedro Gonçalves on 7/11/17.
 * LSTS - FEUP
 */

public class ReviewOldLogs extends AppCompatActivity {
    final Context context = this;
    private static final int REQUEST_PATH = 987;
    private String TAG = "MEU Old Log";
    private String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "alvii";
    private TextView configTex;
    private TextView exifTex;
    private ImageView img;
    boolean firstBack = true;
    private File storageDir = new File(path);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menulog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_open_file:
                //Toast.makeText(this, "New File!", Toast.LENGTH_SHORT).show();
                viewFiles();
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
                    storageDir.toString(), ""));
        }
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_review_old_logs);
        img = (ImageView) findViewById(R.id.imageViewDownload);
        exifTex = (TextView) findViewById(R.id.textViewExif);
        configTex = (TextView) findViewById(R.id.textConfigFile);
        configTex.setMovementMethod(new ScrollingMovementMethod());
        viewFiles();
    }

    // Listen for results.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // See which child activity is calling us back.
        if (requestCode == REQUEST_PATH) {
            if (resultCode == RESULT_OK) {
                //Log.i(TAG, "path: "+data.getStringExtra("GetFileName"));
                showFile(data.getStringExtra("GetPath"), data.getStringExtra("GetFileName"));
            }
        }
    }

    public void viewFiles() {
        firstBack = true;
        Intent intent = new Intent(context, FileChooser.class);
        Bundle b = new Bundle();
        b.putString("BUNDLE_PATH", path);
        intent.putExtras(b);
        startActivityForResult(intent, REQUEST_PATH);
    }

    private void showFile(String filePath, String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf("."));
        //Log.i(TAG, extension);
        if (extension.equals(".txt") || extension.equals(".ini") || extension.equals(".stacktrace"))
            showTextFile(filePath, fileName);
        else if(fileName.substring(fileName.indexOf(".")).equals(".lsf") || fileName.substring(fileName.indexOf(".")).equals(".lsf.gz"))
            mraLite(filePath, fileName);
        else if(extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(".bmp") || extension.equals(".png"))
            showImage(filePath, fileName);
        else
            Toast.makeText(this, "File not supported", Toast.LENGTH_SHORT).show();
    }

    private void showTextFile(String filePath, String fileName) {
        exifTex.setVisibility(View.INVISIBLE);
        img.setVisibility(View.INVISIBLE);

        File m_f = new File(filePath, fileName);
        //Read text from file
        StringBuilder text = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(m_f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;

        try {
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        configTex.setVisibility(View.VISIBLE);
        configTex.setText(text.toString());
    }

    private void showImage(String filePath, String fileName) {
        try {
            configTex.setVisibility(View.INVISIBLE);

            File m_f = new File(filePath, fileName);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(m_f));

            img.setImageBitmap(b);
            img.setVisibility(View.VISIBLE);

            Metadata metadata = null;
            try {
                metadata = ImageMetadataReader.readMetadata(m_f);
            } catch (ImageProcessingException e) {
                Log.i(TAG, e.toString());
            } catch (IOException e) {
                Log.i(TAG, "2: " + e.toString());
            }

            String exifDataText = "";// = new String();
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    //Log.i(TAG, directory.getName() + " : " + tag.getTagName() + " : " + tag.getDescription());
                    exifDataText += directory.getName() + " : " + tag.getTagName() + " : " + tag.getDescription() + "\n";
                }
                if (directory.hasErrors()) {
                    for (String error : directory.getErrors()) {
                        Log.i(TAG, "ERROR: " + error);
                    }
                }
            }
            //Log.i(TAG, exifData);
            exifTex.setMovementMethod(new ScrollingMovementMethod());
            exifTex.setVisibility(View.VISIBLE);
            exifTex.setText(exifDataText);

        } catch (FileNotFoundException e) {
            Log.i(TAG, e.toString());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (firstBack) {
                firstBack = false;
                Toast.makeText(this, "Press back again", Toast.LENGTH_SHORT).show();
            } else {
                exitApp();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exitApp() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.finish();
    }

    private void mraLite(String filePath, String fileName) {
        Intent intent = new Intent(ReviewOldLogs.this, MRALite.class);
        Bundle b = new Bundle();
        b.putString("BUNDLE_PATH", filePath);
        b.putString("BUNDLE_FILENAME", fileName);
        intent.putExtras(b);
        startActivity(intent);
    }
}
