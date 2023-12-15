package com.example.chaton.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;


import com.example.chaton.databinding.ActivitySignInBinding;
import com.example.chaton.utilities.Constants;
import com.example.chaton.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;



public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private PreferenceManager preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferences = new PreferenceManager(getApplicationContext());
        setListners();
    }
    private void setListners()
    {
        binding.textCreateNewAccount.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
        binding.buttonSignIN.setOnClickListener(v -> {
            if(invalidDetails())
            {
                signIn();
            }
        });
    }
    private void makeToast(String message)
    {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private void signIn()
    {
        loading(true);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task ->{
                   if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size() >0)
                   {
                       loading(false);
                       DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                       preferences.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                       preferences.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                       preferences.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                       preferences.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                       Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(intent);
                   }
                   else {
                       makeToast("Unable to sign in");
                       loading(false);
                   }
                });
    }

    private Boolean invalidDetails()
    {
        if(binding.inputEmail.getText().toString().isEmpty())
        {
            makeToast("Email is required");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches())
        {
            makeToast("Please enter valid Email");
            return false;
        }
        else if(binding.inputPassword.getText().toString().isEmpty())
        {
            makeToast("Password is required");
            return false;
        }
        else
        {
            return true;
        }
    }

    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.buttonSignIN.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.buttonSignIN.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

}