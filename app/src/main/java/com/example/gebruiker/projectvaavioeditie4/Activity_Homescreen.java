package com.example.gebruiker.projectvaavioeditie4;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Activity_Homescreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    //Declaring all the drawer, button, textView, String and Firebase variables.
    private DrawerLayout drawer;
    private Button mZoeken;
    private ImageView mIVNavHeader;
    private TextView mTV1NavHeader, mTV2NavHeader;
    private StorageReference mStorage;
    private DatabaseReference myRef;
    private FirebaseDatabase mDatabase;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private String UserID;
    private AutoCompleteTextView mFunctie, mLocatie;

    public static ArrayList<String> Functies = new ArrayList<String>();
    public static ArrayList<String> Locaties = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);

        // Taking the toolbar and set it as the actionbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Declaring the drawer and navigationview. Also making the current page selected in the drawer menu
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Making the drawer menu 'hamburger' and adding it to the actionbar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Setting the title that is showing on the toolbar
        setTitle("Vaavio");

        // Setting the nav_header of the drawer menu, using the layout created.
        View hView = navigationView.inflateHeaderView(R.layout.nav_header);

        // Setting up the firebase connection, getting the current user, if present
        mDatabase = FirebaseDatabase.getInstance();
        myRef = mDatabase.getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null)
        {
            // When a user is logged in, the user ID gets put in a string
            UserID = user.getUid();

            String userID = UserID.toString();

            // Declaring the image view of the nav header
            mIVNavHeader = hView.findViewById(R.id.ImageViewNavHeader);

            // Getting the profile image of the user, if it has uploaded one. When successful, the image view in het nav header gets replaced
            // By the profile picture of the user. Picasso is used fot this processed. The image get loaded from the uri, fit in the image view
            // and cropped centerly into the view.
            mStorage.child("Profile photos/" + userID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
            {
                @Override
                public void onSuccess(Uri uri)
                {
                    Picasso.get().load(uri).fit().centerCrop().into(mIVNavHeader);
                }
            });

            // Declaring the TextView's of the nav header of the drawer menu
            mTV1NavHeader = hView.findViewById(R.id.TextViewNaamNavHeader);
            mTV2NavHeader = hView.findViewById(R.id.TextViewEmailNavHeader);

            // addValueEventListener to change the default text view to the name and email adres of the user, if it has provided these in the
            // profile information section of the account. A dataSnapshot is used to get the name, which get put in a string, after which the
            // string gets put in het Text View. The email is provided by registration, so this can be gotten with the mAuth state. Where it
            // gets the current user, and puts the current user email in a string.
            myRef.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    mUser = mAuth.getCurrentUser();
                    String email = mUser.getEmail();
                    String naam = dataSnapshot.child("Users").child(UserID).child("Persoonlijke informatie").child("Naam").getValue(String.class);

                    mTV1NavHeader.setText(naam);
                    mTV2NavHeader.setText(email);
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }

        // Making the nav_home button in the drawer menu selected on start up
        navigationView.setCheckedItem(R.id.nav_home);

        // Declaring the variables to there corresponding objects
        mFunctie = findViewById(R.id.AcEditTextFunctie);
        mLocatie = findViewById(R.id.AcEditTextLocatie);

        // mFunctie and mLocatie are AutoCompleteTextView's, therefore they need strings to show when you type in the EditText. These strings are stored in the
        // Database. This way we only show suggestions of functies and locaties that are actually used in vacatures. To add these items to a Array that can be used
        // for the AutoCompleteTextView, we first make a addListenerForSingleValueEvent.
        myRef.child("Functies").addValueEventListener(new ValueEventListener()
        {
            // To add the strings, a for loop is executed, because its unknown how many entries there are of functies and locaties in the database, we first
            // get the amount of children from the dataSnapshot. And then for every child a string gets added to the Functies Array. Before we do this, we first
            // remove all the items in the ArrayList, to make sure there aren't any duplicates
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Functies.removeAll(Functies);
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Functies.add(snapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        // To add the array to the AutoCompleteTextView, an ArrayAdapter is made. This adapter consists of 3 items, the context (this), the layout used to display
        // the suggestions when typing something, and the suggestions itself which were stored in the ArrayList, in this case Functies. When the adapter is created,
        // It gets added to the AutoCompleteTextView. Also, the Threshold is changed, which is the amount of characters needed before the AutoCompleteTextView shows
        // a suggestion, the standard of this Threshold is 2, in our case it's 1.
        ArrayAdapter<String> functies = new ArrayAdapter<String>(this, R.layout.autocomplete_item, Functies);
        mFunctie.setAdapter(functies);
        mFunctie.setThreshold(1);

        myRef.child("Locaties").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Locaties.removeAll(Locaties);
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Locaties.add(snapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        ArrayAdapter<String> locaties = new ArrayAdapter<String>(this, R.layout.autocomplete_item, Locaties);
        mLocatie.setAdapter(locaties);
        mLocatie.setThreshold(1);

        // Setti

        // Setting the on click event for the zoeken button. Which redirects the user to the activity with the vacatures.
        mZoeken = findViewById(R.id.ZoekenBtn);
        mZoeken.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onClickZoeken();
            }
        });
    }

    // The onClickZoeken function.
    private void onClickZoeken()
    {
        // First, a connection is made with the database
        mDatabase = FirebaseDatabase.getInstance();
        myRef = mDatabase.getReference();

        // Then, a series of if statements get checked. In this activity, the user can fill in 2 Edit Texts, Functie and Locatie.
        // If the users fills in either both, none or 1 of those EditTexts, the data filled in those fields needs to be passed on to
        // the Vacature activity. Therefore, we do the following.
        if (mFunctie.getText().toString().isEmpty() && mLocatie.getText().toString().isEmpty())
        {
            // The first statement is executed when both fields are empty. Nothing needs to be passed on, so a intent will start
            // redirecting the user to the vacature screen.
            String vacatures = "Vacatures";
            Intent zoeken = new Intent(Activity_Homescreen.this, Activity_Vacatures.class);
            zoeken.putExtra("Vacatures", vacatures);
            startActivity(zoeken);
        }
        if (!mFunctie.getText().toString().isEmpty() && mLocatie.getText().toString().isEmpty())
        {
            // The second statement will execute when something is filled in into the Functie edit text. The text filled in by the user will
            // be converted into a string, and put into a string variable. Then a intent will start, but with this intent data gets send
            // with the putExtra. The string just created will also passed along.
            String functie = mFunctie.getText().toString();
            Intent zoeken = new Intent(Activity_Homescreen.this, Activity_Vacatures.class);
            zoeken.putExtra("functie", functie);
            startActivity(zoeken);
        }
        if (mFunctie.getText().toString().isEmpty() && !mLocatie.getText().toString().isEmpty())
        {
            // The third statement will execute when something is filled in into the Locatie edit text. The text filled in by the user will
            // be converted into a string, and put into a string variable. Then a intent will start, but with this intent data gets send
            // with the putExtra. The string just created will also passed along.
            String locatie = mLocatie.getText().toString();
            Intent zoeken = new Intent(Activity_Homescreen.this, Activity_Vacatures.class);
            zoeken.putExtra("locatie", locatie);
            startActivity(zoeken);
        }
        if (!mFunctie.getText().toString().isEmpty() && !mLocatie.getText().toString().isEmpty())
        {
            // The forth statement will execute when something is filled in into both the Functie and Locatie edit text. The text filled in by the user will
            // be converted into strings, and put into a string variable. Then the string will be combined into one string. Then a intent will start,
            // but with this intent data gets send with the putExtra. The string just created will also passed along.
            String locatie = mLocatie.getText().toString();
            String functie = mFunctie.getText().toString();
            String functie_locatie = functie + "_" + locatie;
            Intent zoeken = new Intent(Activity_Homescreen.this, Activity_Vacatures.class);
            zoeken.putExtra("functie_locatie", functie_locatie);
            startActivity(zoeken);
        }
    }

    // The cases for the items in the Navigation drawer. When clicking on an item in the menu, the method corresponding with
    // The clicked item will be executed.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_home:
                // Intent to switch form the current activity to the intended activity, in this case the Startscherm
                // When executing this event, it will also clear the activity stack so that there ar no activity's
                // Stacking up. When the intended activity is started, the activity which will be left will also be closed.
                Intent navhome = new Intent(this, Activity_Homescreen.class);
                navhome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(navhome);
                finish();
                break;
            case R.id.nav_av:
                // Intent that redirects the user to the Vaavio website outside the app
                Intent av = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.vaavio.nl/terms-and-conditions/"));
                startActivity(av);
                break;
                //Intent that switches the user to his/her profile page from the current activity.
            case R.id.nav_profile:
                Intent navprofile = new Intent(this, Activity_Login.class);
                navprofile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(navprofile);
                finish();
                break;
                //Intent that redirects the user to the "about us" page of Vaavio.
            case R.id.nav_over_ons:
                Intent overons = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.vaavio.nl/over-ons/"));
                startActivity(overons);
                break;
                //Intent that redirects the user to the "contact" page of Vaavio.
            case R.id.nav_contact:
                Intent contact = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.vaavio.nl/contact/"));
                startActivity(contact);
                break;
                //Intent that shows a toast "Settings" when clicked.
            case R.id.nav_settings:
                Toast.makeText(Activity_Homescreen.this, "Settings", Toast.LENGTH_SHORT).show();
                break;
        }
        // After an item is clicked in the menu, the drawer will close itself so you can see the activity/fragment
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed()
    {
        // If the drawer is open, the back button will instead of going back to the previous page, close the drawer.
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        // if the drawer isn't open, the back button will operate just as normal.
        {
            super.onBackPressed();
        }
    }
}
