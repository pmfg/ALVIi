package pt.lsts.alvii;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileChooser extends ListActivity {

    private String TAG = "MEU FileChooser";
    private File currentDir;
    private FileArrayAdapter adapter;
    private int id_loc = 1;
    private File storageDir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "alvii");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
                    storageDir.toString(), ""));
        }
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        currentDir = new File(b.getString("BUNDLE_PATH"));
        fill(currentDir);
    }

    private void fill(File f) {
        File[] dirs = f.listFiles();
        this.setTitle("Current Dir: " + f.getName());
        List<Item> dir = new ArrayList<Item>();
        List<Item> fls = new ArrayList<Item>();
        try {
            for (File ff : dirs) {
                Date lastModDate = new Date(ff.lastModified());
                DateFormat formater = DateFormat.getDateTimeInstance();
                String date_modify = formater.format(lastModDate);
                if (ff.isDirectory()) {
                    File[] fbuf = ff.listFiles();
                    int buf = 0;
                    if (fbuf != null) {
                        buf = fbuf.length;
                    } else buf = 0;
                    String num_item = String.valueOf(buf);
                    if (buf == 0) num_item = num_item + " item";
                    else num_item = num_item + " items";

                    dir.add(new Item(ff.getName(), num_item, date_modify, ff.getAbsolutePath(), "directory_icon"));
                } else {
                    if(ff.getName().equals("IMC.xml.gz"))
                        fls.add(new Item(ff.getName(), ff.length() + " Byte", date_modify, ff.getAbsolutePath(), "imc_xml_icon"));
                    else if(ff.getName().equals("Data.lsf.gz") || ff.getName().equals("Data.lsf"))
                        fls.add(new Item(ff.getName(), ff.length() + " Byte", date_modify, ff.getAbsolutePath(), "gz_log_icon"));
                    else {
                        String extension = ff.getName().substring(ff.getName().lastIndexOf("."));
                        //Log.i(TAG, extension);
                        if (extension.equals(".txt"))
                            fls.add(new Item(ff.getName(), ff.length() + " Byte", date_modify, ff.getAbsolutePath(), "file_icon"));
                        else if (extension.equals(".xml"))
                            fls.add(new Item(ff.getName(), ff.length() + " Byte", date_modify, ff.getAbsolutePath(), "xml_icon"));
                        else if (extension.equals(".ini"))
                            fls.add(new Item(ff.getName(), ff.length() + " Byte", date_modify, ff.getAbsolutePath(), "config_icon"));
                        else if (extension.equals(".png") || extension.equals(".jpg") || extension.equals(".JPG") || extension.equals(".PNG"))
                            fls.add(new Item(ff.getName(), ff.length() + " Byte", date_modify, ff.getAbsolutePath(), "image_icon"));
                        else
                            fls.add(new Item(ff.getName(), ff.length() + " Byte", date_modify, ff.getAbsolutePath(), "unknown_icon"));
                    }
                }
            }
        } catch (Exception e) {}

        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);

        if(f.getName().equals("0"))
            dir.add(0, new Item("..", "Main Folder", "", f.getParent(), "directory_up"));
        else
            dir.add(0, new Item("..", f.getName(), "", f.getParent(), "directory_up"));
        adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        Item o = adapter.getItem(position);
        if (o.getImage().equalsIgnoreCase("directory_icon")) {
                currentDir = new File(o.getPath());
                fill(currentDir);
                id_loc++;
        }
        else if (o.getImage().equalsIgnoreCase("directory_up")) {
            if(id_loc >= 1){
                currentDir = new File(o.getPath());
                fill(currentDir);
                id_loc--;
            }
            else if(id_loc <= 0){
                Toast.makeText(this, "Can not go up more", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            onFileClick(o);
        }
    }

    private void onFileClick(Item o) {
        Intent intent = new Intent();
        intent.putExtra("GetPath", currentDir.toString());
        intent.putExtra("GetFileName", o.getName());
        intent.putExtra("GetFilePath", o.getPath());
        setResult(RESULT_OK, intent);
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, intent);
        } else {
            getParent().setResult(Activity.RESULT_OK, intent);
        }
        super.finish();
    }
}
