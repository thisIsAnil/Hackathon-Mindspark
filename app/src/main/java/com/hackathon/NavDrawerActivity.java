package com.hackathon;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import de.hdodenhof.circleimageview.CircleImageView;
import android.support.design.widget.NavigationView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hackathon.SignIn.SharedPrefManager;
import com.hackathon.textEditor.EditActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aniket on 17-Sep-17.
 */
public class NavDrawerActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener{

    Context mContext = this;
    private StorageReference riversRef;
    private StorageReference mStorageRef;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private TextView mFullNameTextView, mEmailTextView;
    private CircleImageView mProfileImageView;
    private String mUsername, mEmail;

    SharedPrefManager sharedPrefManager;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private List<String> files=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initNavigationDrawer();

        View header = navigationView.getHeaderView(0);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mFullNameTextView = (TextView) header.findViewById(R.id.fullName);
        mEmailTextView = (TextView) header.findViewById(R.id.email);
        mProfileImageView = (CircleImageView) header.findViewById(R.id.profileImage);

        recyclerView=(RecyclerView)findViewById(R.id.file_list_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(NavDrawerActivity.this,LinearLayoutManager.VERTICAL,false));

        recyclerView.setAdapter(new FileAdapter());
        // create an object of sharedPreferenceManager and get stored user data
        sharedPrefManager = new SharedPrefManager(mContext);
        mUsername = sharedPrefManager.getName();
        mEmail = sharedPrefManager.getUserEmail();
        String uri = sharedPrefManager.getPhoto();
        Uri mPhotoUri = Uri.parse(uri);

        //Set data gotten from SharedPreference to the Navigation Header view
        mFullNameTextView.setText(mUsername);
        mEmailTextView.setText(mEmail);

        Picasso.with(mContext)
                .load(mPhotoUri)
                .placeholder(android.R.drawable.sym_def_app_icon)
                .error(android.R.drawable.sym_def_app_icon)
                .into(mProfileImageView);

        configureSignIn();
        files=getFiles();
        findViewById(R.id.create_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NavDrawerActivity.this, EditActivity.class));
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // Initialize and add Listener to NavigationDrawer
    public void initNavigationDrawer(){

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                int id = item.getItemId();

                switch (id){
                    case R.id.payment:
                        Toast.makeText(getApplicationContext(),"Home",Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.logout:
                        signOut();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.client:
                        startActivity(new Intent(NavDrawerActivity.this,ClientActivity.class));
                        break;

                }
                return true;
            }
        });

        //set up navigation drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                drawerView.bringToFront();
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    // This method configures Google SignIn
    public void configureSignIn(){
// Configure sign-in to request the user's basic profile like name and email
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, options)
                .build();
        mGoogleApiClient.connect();
    }

    //method to logout
    private void signOut(){
        new SharedPrefManager(mContext).clear();
        mAuth.signOut();

        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Intent intent = new Intent(NavDrawerActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
        );
    }
    private List<String> getFiles(){
        File r=new File("/sdcard/demo");
        List<String> s=new ArrayList<>();
        if(r.exists()){
            File[] files=r.listFiles();
            for(File f:files){
                s.add(f.getAbsolutePath());
            }
            return s;
        }
        return new ArrayList<>();
    }
    private String[] getNamesFromFiles(List<String> s){
        String[] p=new String[s.size()];
        for(int i=0;i<s.size();i++){
            File f=new File(s.get(i));
            p[i]=(f.getName());
        }
        return p;
    }
    private void uploadFile(String filePath) {
        if (filePath != null) {
            //displaying a progress dialog while upload is going on
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            File f=new File(filePath);
            riversRef = mStorageRef.child("files/"+f.getName());
            riversRef.putFile(Uri.fromFile(f))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying a success toast
                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
        //if there is not any file
        else {
            //you can display an error toast
        }
    }

    private class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileHolder>{
        private View root;
        @Override
        public FileAdapter.FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            root=View.inflate(NavDrawerActivity.this,R.layout.file_list_adapter,null);
            return new FileHolder(root);
        }

        @Override
        public void onBindViewHolder(FileAdapter.FileHolder holder, int position) {
            if(holder==null)holder=new FileHolder(root);
            File f=new File(files.get(position));
            holder.fileName.setText(f.getName());
            final int p=position;
            final View hld=holder.more;
            holder.more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View pop=View.inflate(NavDrawerActivity.this,R.layout.more_options,null);
                    final PopupWindow popupWindow=new PopupWindow(pop, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
                    popupWindow.showAtLocation(hld, Gravity.END,0,0);

                    pop.findViewById(R.id.upload_pcx).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                            uploadFile(files.get(p));
                        }
                    });
                    pop.findViewById(R.id.view_pcx).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                            Intent intent=new Intent(NavDrawerActivity.this,ReaderActivity.class);
                            intent.putExtra(Intent.EXTRA_TEXT,files.get(p));
                            startActivity(intent);
                        }
                    });
                    pop.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            popupWindow.dismiss();
                            return false;
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return files.size();
        }
        class FileHolder extends RecyclerView.ViewHolder{
            private TextView fileName;
            private AppCompatImageButton more;
            public FileHolder(View v){
                super(v);
                fileName=(TextView)v.findViewById(R.id.file_name_tv);
                more=(AppCompatImageButton)v.findViewById(R.id.more_options);
            }

        }
    }
}
