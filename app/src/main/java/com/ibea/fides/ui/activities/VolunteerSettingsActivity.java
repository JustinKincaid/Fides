package com.ibea.fides.ui.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ibea.fides.BaseActivity;
import com.ibea.fides.R;
import com.ibea.fides.models.Volunteer;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.ibea.fides.R.id.stateSpinner;
import static com.ibea.fides.R.id.zipedittext;

public class VolunteerSettingsActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    @Bind(R.id.tempPicture) ImageView mProfilePicImageView;
    @Bind(R.id.usernameedittext) EditText mUserNameEditText;
    @Bind(R.id.cityedittext) EditText mCityEditText;
    @Bind(stateSpinner) Spinner mStateInput;
    @Bind(zipedittext) EditText mZipEditText;
    @Bind(R.id.updateButton) FloatingActionButton mUpdateButton;

    Volunteer thisVol;

    String mCity;
    String mState;
    String mZip;
    String mUsername;

    // image storage reference variables
    FirebaseStorage mStorage;
    StorageReference mStorageRef;
    StorageReference mImageRef;

    public static final int GET_FROM_GALLERY = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_settings);
        ButterKnife.bind(this);

        autoFill();

        // State Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.states_array,R.layout.custom_spinner_item_settings);
        adapter.setDropDownViewResource(R.layout.custom_spinner_list_settings);
        mStateInput.setAdapter(adapter);

        mProfilePicImageView.setOnClickListener(this);
        mUpdateButton.setOnClickListener(this);

        // assign image storage reference variables
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReferenceFromUrl("gs://fides-6faeb.appspot.com");
        mImageRef = mStorageRef.child("images/" + uId + ".jpg");

        mImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getApplicationContext())
                        .load(uri)
                        .placeholder(R.drawable.avatar_blank)
                        .resize(450,400)
                        .centerCrop()
                        .into(mProfilePicImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        setTitle("Account Settings");
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        mState = parent.getItemAtPosition(pos).toString();
    }

    public void autoFill() {
        dbVolunteers.child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                thisVol = dataSnapshot.getValue(Volunteer.class);
                if(thisVol != null) {
                    if(!thisVol.getName().equals(""))
                        mUserNameEditText.setText(thisVol.getName());
                    if(!thisVol.getCity().equals(""))
                        mCityEditText.setText(thisVol.getCity());
                    if(!thisVol.getZipcode().equals(""))
                        mZipEditText.setText(thisVol.getZipcode());

                    String state = thisVol.getState();
                    Resources res = getResources();
                    String[] states = res.getStringArray(R.array.states_array);
                    int index = Arrays.asList(states).indexOf(state);
                    mStateInput.setSelection(index);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void onClick(View view) {
        if(view == mProfilePicImageView) {
            startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
        }
        else if (view == mUpdateButton) {
            boolean updated = false;

            if(isValidUsername(mUserNameEditText.getText().toString().trim())) {
                updateUsername();
                updated = true;
            }
            if(isValidCity(mCityEditText.getText().toString().trim())) {
                updateCity();
                updated = true;
            }
            if(mStateInput.getSelectedItem().equals(thisVol.getState())) {
                updateState();
                updated = true;
            }
            if(isValidZip(mZipEditText)) {
                updateZip();
                updated = true;
            }

            if(updated) {
                Toast.makeText(mContext, "Profile updated", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(mContext, "No changes made", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImageUri = intent.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                mProfilePicImageView.setImageBitmap(bitmap);

                // save picture to firebase storage
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] serializedImageFile = baos.toByteArray();

                UploadTask uploadTask = mImageRef.putBytes(serializedImageFile);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    }
                });

            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////
    // DB UPDATERS

    private void updateUsername() {
        String tempUsername = mUserNameEditText.getText().toString().trim();
        boolean validInput = isValidUsername(tempUsername);
        if (!validInput){
            return;
        }

        mUsername = tempUsername;

        dbVolunteers.child(uId).child("name").setValue(mUsername);
        thisVol.setName(mUsername);
        mUserNameEditText.setHint(mUserNameEditText.getText());
        mUserNameEditText.getText().clear();
    }

    private void updateCity() {
        String tempCity = mCityEditText.getText().toString().trim();
        boolean validInput = isValidCity(tempCity);
        if (!validInput) {
            return;
        }

        mCity = tempCity;

        dbVolunteers.child(uId).child("city").setValue(mCity);
        thisVol.setCity(mCity);
        mCityEditText.setHint(mCityEditText.getText());
        mCityEditText.getText().clear();
    }

    private void updateState() {
        mState = mStateInput.getSelectedItem().toString().trim();
        thisVol.setState(mState);
        dbOrganizations.child(uId).child("state").setValue(mState);
    }

    private void updateZip() {
        String tempZip = mZipEditText.getText().toString().trim();
        boolean validInput = isValidZip(mZipEditText);
        if (!validInput) {
            return;
        }

        mZip = tempZip;

        dbVolunteers.child(uId).child("zipcode").setValue(mZip);
        thisVol.setZipcode(mZip);
        mZipEditText.setHint(mZipEditText.getText());
        mZipEditText.getText().clear();
    }


    ///////////////////////////////////////////////////////////////////////
    // TEXT INPUT VALIDATORS

    private boolean isValidUsername(String data) {
        if (data.equals("")) {
            mUserNameEditText.setError("Please enter your new mOrgName");
            return false;
        }
        return true;
    }

    private boolean isValidCity(String data) {
        if (data.equals("")) {
            mCityEditText.setError("Please enter your city");
            return false;
        }
        return true;
    }

    public Boolean isValidZip(EditText field){
        String onlyNumbers = "[0-9]+";
        String catcher = field.getText().toString().trim();

        if(catcher.length() != 0){
            if(catcher.length() == 5 && catcher.matches(onlyNumbers)){
                return true;
            }else{
                field.setError("Invalid");
                return false;
            }
        }else return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_volunteertutorial) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

//          TODO: Set up an actual message
            builder.setMessage("This is the Volunteers Setting Page");

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Log.d("Justin", "Dismiss");
                }
            });


            AlertDialog dialog = builder.create();

            dialog.show();

        }
        return super.onOptionsItemSelected(item);
    }
}
