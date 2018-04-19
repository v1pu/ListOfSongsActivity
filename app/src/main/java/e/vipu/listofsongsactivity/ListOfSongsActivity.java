package e.vipu.listofsongsactivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class ListOfSongsActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    ListView listview;
    Button btnPlayStop;
    TextView txtSongName;
    CardView cardView;
    ArrayList<SongObject> listOfContents;
    AdapterClass adapter;
    String path1;
    static String absolutePath, songName;
    public static boolean playing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_songs);

        // If Android Marshmello or above, then check if permission is granted
        if (Build.VERSION.SDK_INT >= 23)
            checkPermission();
        else
            initViews();
        //Gives you the full path of phone memory
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();


        //Calling the function which fetches the list of music files
        initList(path);

        //initializing the adapter and passing the context, list item and list of references of SongObject
        adapter=new AdapterClass(this,R.layout.list_item,listOfContents);
        listview.setAdapter(adapter);


    }


    private void initViews() {

//initializing views
        btnPlayStop = (Button) findViewById(R.id.btnPlayStop);
        txtSongName = (TextView) findViewById(R.id.txtSongName);
        cardView = (CardView) findViewById(R.id.cardView);
        listview = (ListView) findViewById(R.id.listView);
        listOfContents = new ArrayList<>();
        btnPlayStop.setOnClickListener(this);
    listview.setOnItemClickListener(this);
        //If music is playing already on starting the app, player should be visible with Stop button
        if (playing) {
            txtSongName.setText(songName);
            cardView.setVisibility(View.VISIBLE);
            btnPlayStop.setText("Stop");
        }
    }



    //handling events when user clicks on any music file in list view

    //Handling events when button Play/Stop is clicked in the player

    @Override
    public void onClick(View view) {

        if (playing) {
            //If song is playing and user clicks on Stop button
            //Stop the song by calling stopService() and change boolean value
            //text on button should be changed to 'Play'
            playing = false;
            btnPlayStop.setText("Play");
            Intent i = new Intent(ListOfSongsActivity.this, MusicService.class);
            stopService(i);
        } else if (!playing) {
            //If song is not playing and user clicks on Play button
            //Start the song by calling startService() and change boolean value
            //text on button should be changed to 'Stop'
            playing = true;
            btnPlayStop.setText("Stop");
            Intent i = new Intent(ListOfSongsActivity.this, MusicService.class);
            startService(i);
        }
    }
    void initList(String path){
        try {
            File file = new File(path);
            File[] filesArray=file.listFiles();
            String fileName;
            for(File file1:filesArray) {
                if (file1.isDirectory()) {
                    initList(file1.getAbsolutePath());
                } else {
                    fileName=file1.getName();
                    if ((fileName.endsWith(".mp3")) || (fileName.endsWith(".mp4"))){
                        listOfContents.add(new SongObject(file1.getName(), file1.getAbsolutePath()));
                    }

                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        //player is visible
        cardView.setVisibility(View.VISIBLE);

        //If some other song is already playing, stop the service
        if (playing) {
            Intent i = new Intent(ListOfSongsActivity.this, MusicService.class);
            stopService(i);
        }

        playing = true;

        //getting absolute path of selected song from bean class 'SongObject'
        SongObject sdOb = listOfContents.get(position);
        absolutePath = sdOb.getAbsolutePath();

        //Play the selected song by starting the service
        Intent start = new Intent(ListOfSongsActivity.this, MusicService.class);
        startService(start);

        //Get and set the name of song in the player
        songName = listOfContents.get(position).getFileName();
        txtSongName.setText(songName);
        btnPlayStop.setText("Stop");
    }
    void checkPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            //if permission granted, initialize the views
            initViews();
        }else{
            //show the dialog requesting to grant permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initViews();
                }else{
                    //permission is denied (this is the first time, when "never ask again" is not checked)
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                        finish();
                    }
                    //permission is denied (and never ask again is  checked)
                    else {
                        //shows the dialog describing the importance of permission, so that user should grant
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("You have forcefully denied Read storage permission.\n\nThis is necessary for the working of app."+"\n\n"+"Click on 'Grant' to grant permission")
                                //This will open app information where user can manually grant requested permission
                                .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", getPackageName(), null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                })
                                //close the app
                                .setNegativeButton("Don't", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                    }
                                });
                        builder.setCancelable(false);
                        builder.create().show();
                    }
                }
        }
    }
}




