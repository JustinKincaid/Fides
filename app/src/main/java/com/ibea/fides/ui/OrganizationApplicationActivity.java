package com.ibea.fides.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.ibea.fides.BaseActivity;
import com.ibea.fides.R;
import com.ibea.fides.models.Organization;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.widget.Toast.makeText;

public class OrganizationApplicationActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    @Bind(R.id.nameInput) EditText mNameInput;
    @Bind(R.id.organizationInput) EditText mOrganizationNameInput;
    @Bind(R.id.addressInput) EditText mAddressInput;
    @Bind(R.id.cityInput) EditText mCityInput;
    @Bind(R.id.stateSpinner) Spinner mStateSpinner;

    @Bind(R.id.zipcodeInput) EditText mZipInput;
    @Bind(R.id.descriptionInput) EditText mDescriptionInput;
    @Bind(R.id.passwordInput) EditText mPasswordInput;
    @Bind(R.id.passwordConfirmInput) EditText mPasswordConfirmInput;
    @Bind(R.id.emailInput) EditText mEmailInput;
    @Bind(R.id.submitButton) Button mSubmitButton;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private ProgressDialog mAuthProgressDialog;

    String mName;
    String mEmail;
    String ein;
    String userName;
    String address;
    String city;
    String mState;
    String zip;
    String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_application);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        createAuthStateListener();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.states_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStateSpinner.setAdapter(adapter);
        mStateSpinner.setOnItemSelectedListener(this);

        // Set Click Listener
        mSubmitButton.setOnClickListener(this);

        createAuthProgressDialog();
    }
    // Add AuthStateListener on Start
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    // Remove AuthStateListener on Activity Stop
    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View view) {
        // Sends email to us claiming an organization
        if (view == mSubmitButton) {
            createOrganization();
        }
    }

    public void createOrganization() {
        String orgName = mOrganizationNameInput.getText().toString().trim();
        userName = mNameInput.getText().toString().trim();
        address = mAddressInput.getText().toString().trim();
        city = mCityInput.getText().toString().trim();
        zip = mZipInput.getText().toString().trim();
        description = mDescriptionInput.getText().toString().trim();
        String password = mPasswordInput.getText().toString().trim();
        String passwordConfirm = mPasswordConfirmInput.getText().toString().trim();
        String email = mEmailInput.getText().toString().trim();

        // Confirm validity of inputs
        boolean validName = isValidName(orgName);
        boolean validEmail = isValidEmail(email);
        boolean validPassword = isValidPassword(password, passwordConfirm);

        if(!validName || !validEmail || !validPassword) {
            return;
        }


        mName = orgName;
        mEmail = email;

        // Display Progress Dialog
        mAuthProgressDialog.show();

        // Upload user authentication info to Firebase
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mAuthProgressDialog.dismiss();

                // If Auth Success, call function passing in User info. If Auth Not Successful, alert.
                if(task.isSuccessful()) {
                    createFirebaseUserProfile(task.getResult().getUser());
                    Intent intent = new Intent(mContext, LogInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    makeText(mContext, "Account Creation Failed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        mState = parent.getItemAtPosition(pos).toString();
    }

    private void createAuthStateListener() {
        // Create new AuthStateListener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Get user info
                FirebaseUser user = firebaseAuth.getCurrentUser();
            }
        };
    }

    private boolean isValidName(String name) {
        if (name.equals("")) {
            mNameInput.setError("Please enter your name");
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        boolean isGoodEmail = (email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if(!isGoodEmail) {
            mEmailInput.setError("Please enter a valid email address");
            return false;
        }
        return isGoodEmail;
    }

    private boolean isValidPassword(String password, String confirmPassword) {
        if (password.length() < 6) {
            mPasswordInput.setError("Please create a password containing at least 6 characters");
            return false;
        } else if (!password.equals(confirmPassword)) {
            mPasswordInput.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    // Create Progress Dialog
    private void createAuthProgressDialog() {
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading...");
        mAuthProgressDialog.setMessage("Creating Account...");
        mAuthProgressDialog.setCancelable(false);
    }

    // Upload User DisplayName to Firebase
    private void createFirebaseUserProfile(FirebaseUser user) {
        // Add Display Name to User Authentication in Firebase
        Organization newOrg = new Organization(user.getUid(), mName, userName, address, city, mState, zip, description);
        newOrg.setContactEmail(mEmail);
        dbPendingOrganizations.child(user.getUid()).setValue(newOrg);

        UserProfileChangeRequest addProfileName = new UserProfileChangeRequest.Builder().setDisplayName(mName).build();

        // Create Toast, overriding background property of activity
        Toast toast = Toast.makeText(mContext, "Your Application Has Been Received", Toast.LENGTH_LONG);
        View view = toast.getView();
        view.setBackgroundColor(Color.argb(150,0,0,0));
        view.setPadding(30,30,30,30);
        toast.setView(view);
        toast.show();


        user.updateProfile(addProfileName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {}
        });
    }
}

