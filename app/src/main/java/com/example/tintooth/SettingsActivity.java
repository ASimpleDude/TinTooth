package com.example.tintooth;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private EditText mNameField, mPhoneField, mDesField;
    private ProgressBar spinner;
    private Button mConfirm;
    private ImageButton mBack;
    private ImageView mProfileImage;
    private Spinner mGender;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private String userId, userName, userPhone, profileImageUrl, userGender, userDescription;
    private Uri resultUri;
    private final ActivityResultLauncher<Intent> mActivityResultLauncher
            = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

            });
    private void bindingView() {
        spinner = findViewById(R.id.pBar);
        spinner.setVisibility(View.GONE);

        mNameField = findViewById(R.id.name);
        mPhoneField = findViewById(R.id.phone);
        mDesField= findViewById(R.id.description_setting) ;

        mProfileImage =  findViewById(R.id.profileImage);
        mBack = findViewById(R.id.settingsBack);

        mConfirm = findViewById(R.id.confirm);
        mGender =  findViewById(R.id.spinner_gender_settings);

        androidx.appcompat.widget.Toolbar toolbar= findViewById(R.id.settings_toolbartag);
        setSupportActionBar(toolbar);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.genders,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mGender.setAdapter(adapter);
    }
    private void bindingAction(){
        mProfileImage.setOnClickListener(v -> {

            if(!checkPermission()){
                Toast.makeText(SettingsActivity.this, "Allow access to continue!", Toast.LENGTH_SHORT).show();
                requestPermission();
            }
            else{
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });
        mConfirm.setOnClickListener(v -> saveUserInformation());

        mBack.setOnClickListener(v -> {
            spinner.setVisibility(View.VISIBLE);
            finish();return;
        });

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        bindingView();
        bindingAction();

         mAuth = FirebaseAuth.getInstance();
        if (mAuth != null && mAuth.getCurrentUser() != null)
            userId = mAuth.getCurrentUser().getUid();
        else {
            Toast.makeText(this, "Not login", Toast.LENGTH_SHORT).show();
            finish();
        }

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        Toast.makeText(this, "User"+userId, Toast.LENGTH_SHORT).show();

        getUserInfo();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }
    public boolean checkPermission(){
        int result= ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    public void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{READ_EXTERNAL_STORAGE}, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                mActivityResultLauncher.launch(Intent.createChooser(intent, "select picture"));
            } else {
                Toast.makeText(this, "Allow access to continue", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.Contact){
            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("About Us")
                    .setMessage("Hoàng Minh Việt\nNguyễn Công Quân\n Bùi Thanh Tùng\n Phan Đức Mạnh")
                    .setNegativeButton("Dismiss", null)

                    .show();
        }
        else if(item.getItemId()==R.id.logout){
            spinner.setVisibility(View.VISIBLE);
            mAuth.signOut();
            Toast.makeText(this, "Log out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SettingsActivity.this, Choose_Login_And_Reg.class);
            startActivity(intent);
            finish();
            spinner.setVisibility(View.GONE);
        }
        else if(item.getItemId()==R.id.deleteAccount){
            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("Are you sure?")
                    .setMessage("Delete account will also remove all your data from system." +
                            "Trap boy and trap girl usually use this function :(")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    spinner.setVisibility(View.VISIBLE);
                                    if(task.isSuccessful()){
                                        deleteUserAccount(userId);
                                        Toast.makeText(SettingsActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SettingsActivity.this, Choose_Login_And_Reg.class);
                                        startActivity(intent);
                                        finish();
                                        spinner.setVisibility(View.GONE);
                                    }
                                    else{
                                        Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                        Intent intent = new Intent(SettingsActivity.this, Choose_Login_And_Reg.class);
                                        startActivity(intent);
                                        finish();
                                        spinner.setVisibility(View.VISIBLE);
                                    }
                                    return;
                                }
                            });
                        }
                    }).setNegativeButton("Dismiss", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } return super.onOptionsItemSelected(item);
    }
    public void deleteMatch(String matchId, String chatId){
        DatabaseReference matchId_in_UserId_dbReference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(userId).child("connections").child("matches").child(matchId);
        DatabaseReference userId_in_matchId_dbReference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(userId).child("connections").child("matches").child(userId);
        DatabaseReference yeps_in_matchId_dbReference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(userId).child("connections").child("yeps").child(userId);
        DatabaseReference yeps_in_UserId_dbReference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(userId).child("connections").child("yeps").child(matchId);
        DatabaseReference matchId_chat_dbReference = FirebaseDatabase.getInstance().getReference().child("Chat")
                .child(chatId);
        matchId_chat_dbReference.removeValue();
        matchId_in_UserId_dbReference.removeValue();
        userId_in_matchId_dbReference.removeValue();
        yeps_in_matchId_dbReference.removeValue();
        yeps_in_UserId_dbReference.removeValue();
    }

    private void deleteUserAccount(String userId) {
        DatabaseReference curruser_ref = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        DatabaseReference curruser_matches_ref = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(userId).child("connections").child("matches");
        curruser_matches_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot match: dataSnapshot.getChildren()){
                        deleteMatch(match.getKey(), match.child("ChatId").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        curruser_matches_ref.removeValue();
        curruser_ref.removeValue();
    }
    private void getUserInfo(){
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        userName=map.get("name").toString();
                        mNameField.setText(userName);
                    }
                    if(map.get("phone")!=null){
                        userPhone=map.get("phone").toString();
                        mPhoneField.setText(userPhone);
                    }if(map.get("gender")!=null){
                        userGender=map.get("gender").toString();


                    }
                    if(map.get("description")!=null){
                        userDescription=map.get("description").toString();
                        mDesField.setText(userDescription);
                    }
                    if (userGender.equals("Male")){
                        mGender.setSelection(0);
                    } else if (userGender.equals("Female")){
                        mGender.setSelection(1);
                    } else {
                        mGender.setSelection(2);
                    }
                        Glide.clear(mProfileImage);
                        if(map.get("profileImageUrl")!=null){
                            profileImageUrl=map.get("profileImageUrl").toString();
                            switch (profileImageUrl){
                                case "default":
                                    Glide.with(getApplication()).load(R.drawable.profile).into(mProfileImage);
                                    break;
                                default:
                                    Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                                    break;
                            }
                        }
                    }
                }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingsActivity.this, "Fails to upload image", Toast.LENGTH_SHORT).show();
            }
        });}
        private void saveUserInformation(){
            userName= mNameField.getText().toString();
            userPhone=mPhoneField.getText().toString();
            userGender=mGender.getSelectedItem().toString();
            userDescription=mDesField.getText().toString();

            Map userInfo = new HashMap();
            userInfo.put("name", userName);
            userInfo.put("phone", userPhone);
            userInfo.put("gender", userGender);
            userInfo.put("description", userDescription);
            mUserDatabase.updateChildren(userInfo);
            if(resultUri!=null){
                StorageReference filepath= FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);
                Bitmap bitmap= null;

                try{
                    bitmap= MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);

                }
                catch(IOException e){
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = filepath.putBytes(data);
                uploadTask.addOnFailureListener(e -> finish());
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uri.isComplete());
                    Uri downloadUri = uri.getResult();
                    Map userInfo1 = new HashMap();
                    userInfo1.put("profileImageUrl", downloadUri.toString());
                    mUserDatabase.updateChildren(userInfo1);
                    finish();
                    return;
                });
            } else{finish();}
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1&& resultCode== Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri=imageUri;
            mProfileImage.setImageURI(resultUri);
        }
    }
}
