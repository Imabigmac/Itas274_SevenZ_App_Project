/**
 * @project "sevenZ"
 * @Author Mac Conway
 * @Date April 2022
 */

package com.mac.sevenz;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mac.sevenz.databinding.ActivityMainBinding;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * * @method MainActivity
 * * loads main activity views.
 * * provides a listenser for when screen pages change.
 * starts firebase connection
 * starts location GPS location manager
 */
public class MainActivity extends AppCompatActivity implements LocationListener {

    private ActivityMainBinding binding;
    public static String TAG = "itas123";
    public static String DEBUG_TAG = "itas123";
    private FirebaseFirestore db;
    private final boolean isLoginIn = false;
    public String nameTextS;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    //TextView txtLat;
    TextView locationName;
    String userLocation;
    String lat;
    double theLong;
    double theLat;
    String provider;
    String userDocID;
    protected String latitude, longitude;
    protected boolean gps_enabled, network_enabled;
    private static final int PERMISSIONS_REQUEST_CODE = 99;
    String itsAMatch; //stores match value that is to be added to the another user who is matched.
    String token;
    String chatUserID;

    ArrayList<String> matchList = new ArrayList<String>();
    ArrayList<String> chatList = new ArrayList<String>();
    Map<String, Object> doc = new HashMap<>();
    String[] traitsArray = null;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                Log.d("itas123", "Destination changed: " + destination);

                if (destination.getId() == R.id.navigation_dashboard) {
                    showFirestoreDashboard();
                }
            }
        });

        //begins conenction to firebase
        db = FirebaseFirestore.getInstance();

        //outputs current gps location datas in main page
        //txtLat = findViewById(R.id.gpsview);

        Log.d("itas123", "Setting up location manager");

        //request the location of the device and requires this to be updated if the location changes in a interval of 10 MS and distance of a meter
        requestLocationPerm();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 1, this);

    }


    /**
     * @method requestLocationPerm
     * Prompts user when app loads for first time to approve access to location services (gps)
     */
    private void requestLocationPerm() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(this, perms, PERMISSIONS_REQUEST_CODE);
    }

    /**
     * @method showFirestoreDashboard
     * Method takes user firestore DB data and displays it from the "dashbaord/account" view.
     */
    public void showFirestoreDashboard() {

        Log.d(TAG, "this is: " + nameTextS);

        // connections to database collection "users" where the username is equal to that is entered
        db.collection("users")
                .whereEqualTo("name", nameTextS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot snapshot = task.getResult();
                            StringBuffer usernameOut = new StringBuffer();
                            StringBuffer ageOut = new StringBuffer();
                            StringBuffer fullNameOut = new StringBuffer();
                            StringBuffer userTraitOut = new StringBuffer();

                            //takes the current document ID and adds it to a string buffer
                            //takes the UI text field for username to a string buffer
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Map<String, Object> theData = document.getData();
                                String returnTheUsername = (String) theData.get("name");
                                usernameOut.append("Username: " + returnTheUsername + "\n");
                                TextView showUser = findViewById(R.id.usernameView);
                                showUser.setText(usernameOut.toString());

                                //takes the UI text field for user age to a string buffer
                                String returnUserAge = (String) theData.get("age");
                                ageOut.append("you are : " + returnUserAge + " years old" + "\n");
                                TextView showUAge = findViewById(R.id.ageView);
                                showUAge.setText(ageOut.toString());

                                //takes the UI text field for full name to a string buffer
                                String returnUserFullName = (String) theData.get("userFullName");
                                fullNameOut.append("Welcome back : " + returnUserFullName + "\n");
                                TextView showUFName = findViewById(R.id.displayName);
                                showUFName.setText(fullNameOut.toString());

                                //user traits from the input field are stored as a string
                                String returnUserTraits = (String) theData.get("UserTraits");
                                //the string is split, delinetated buy a space
                                //then added to a new array
                                // all traits are then stored as individual strings in a global string arraylist and displays
                                traitsArray = returnUserTraits.split(" ");
                                for (int i = 0; i < traitsArray.length; i++) {
                                    traitsArray[i] = traitsArray[i].trim();
                                }
                                userTraitOut.append(Arrays.toString(traitsArray));
                                TextView showUTraits = findViewById(R.id.traitViewer);
                                showUTraits.setText(userTraitOut.toString());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    /**
     * @param location
     * @throws IOException
     * @method on locationchanged
     * pulls location data from MainActivty method when location is changed.
     * sets location corrdinates to global variables.
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d("itas123", "Location changed...");

        //sets the Lat and Long as global vairables
        //txtLat.setText("Lat:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
        theLat = location.getLatitude();
        theLong = location.getLongitude();

        Log.d("itas123", "Lat:" + location.getLatitude() + ", Longitude:" + location.getLongitude());

        //if no location data the method will throw exception.
        try {
            whereAmI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("itas123", "Location provider disabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }

    /**
     * @throws IOException
     * @method whereAmI
     * loads google "geocoder" library to decode LAt and Long abd output a city and country name
     * Location city name is saved globally per user
     */
    public void whereAmI() throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(theLat, theLong, 1);
        String cityName = addresses.get(0).getAddressLine(0);
        //String stateName = addresses.get(0).getAddressLine(1);
        String countryName = addresses.get(0).getAddressLine(2);
        String theCity = addresses.get(0).getLocality();

        //global city location stored as global string
        userLocation = theCity;

        //testing logs
        Log.d("itas123", "the location is: " + cityName);
        Log.d("itas123", "the location is: " + countryName);
        Log.d("itas123", "the location is: " + theLat);
        Log.d("itas123", "the location is: " + theLong);
    }

    /**
     * @param view
     * @method addUserFirestore
     * Adds all edit text fields from home page and ads them to the fire store database
     */
    public void addUserFirestore(View view) {
        //takes username field data and converts data to a string
        EditText nameText = findViewById(R.id.editTextUsername);
        nameTextS = nameText.getText().toString();
        //takes password field data and converts data to a string (plaintext)
        String passwordTextS;
        EditText editText = findViewById(R.id.editTextPassword);
        passwordTextS = editText.getText().toString();
        //takes user age field data and converts data to a string
        String userAge;
        EditText addAge = findViewById(R.id.enterAge);
        userAge = addAge.getText().toString();
        //takes users full field data and converts data to a string - one string first and last
        String userFullName;
        EditText addFullName = findViewById(R.id.fullName);
        userFullName = addFullName.getText().toString();

        //for the traits I think it will need be one string, then use a split method
        //to make individual
        String tempUserTraits;
        EditText addUserTraits = findViewById(R.id.enterTraits);
        tempUserTraits = addUserTraits.getText().toString();


        //Puts all strings created above into a hashmap
        doc.put("name", nameTextS);
        doc.put("password", passwordTextS);
        doc.put("age", userAge);
        doc.put("userFullName", userFullName);
        doc.put("UserTraits", tempUserTraits);
        doc.put("location", userLocation);
        doc.put("UserIdentifier", userDocID);
        //adds current time
        Long time = System.currentTimeMillis();
        doc.put("time", time);

        //opens a firestore database connection to the users collection
        db.collection("users")
                .add(doc)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    //if the adding process is succesful, pull the document ID created and store that as global vairbale for later use
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(DEBUG_TAG, "Document Snapshot added with ID: " + documentReference.getId());
                        userDocID = documentReference.getId(); //get the current user database ID
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(DEBUG_TAG, "Error adding new comment: ", e);
                    }
                });
    }

    /**
     * @param view
     * @method match
     * access the database and update teh current user field with a UserID absed on the docuementID
     * creates a matches array to store all matches
     */
    public void match(View view) {
        //get the documentID of the current user, as this needs to be updated to include the documentID as the user Identifier code.
        //user identifier is used in DB as a userID that can be search for in the DB.

        //updates document in DB
        DocumentReference updateUser = db.collection("users").document(userDocID);
        updateUser
                .update("userIdentifier", userDocID)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });

        //creates a array to the user matches
        String[] matches = new String[0];
        StringBuffer userMatch = new StringBuffer();
        userMatch.append(Arrays.toString(matches));

        //calls the locationMatch method
        locationMatch(userDocID);
    }

    /**
     * @param userID
     * @method locationMatch for the userID used in the match method.
     * Takes string of traits ands mathces one vlaue
     * accesses the database and comapares location of the current user (last user to make an account during session).\
     * also compares and matches traits fo user
     */
    public void locationMatch(String userID) {

        Log.d("itas123", "the suer document id is: " + userDocID);

        //takes string of traits and displays them on the dashbaord/account page
        //converts string into a single trait to be searched for - this will be the first word in the strait string
        String singleTrait = null;
        TextView showUTraits = findViewById(R.id.traitViewer);
        String userTraits = showUTraits.getText().toString();
        String[] traitsArray2 = userTraits.split(" ");
        singleTrait = traitsArray2[0]
                .replace("[", "")  //remove the right bracket
                .replace("]", "")  //remove the left bracket
                .trim();
        Log.d(TAG, "SEARCH FOR : " + userTraits + " single trait " + singleTrait);

        //searches the DB collection users for other users with the same location as current user and only their first user trait.
        //returns all users who match that criteria
        db.collection("users")
                .whereEqualTo("location", userLocation)
                .whereEqualTo("UserTraits", singleTrait)

                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "matcher" + document.getId() + " => " + document.getData());
                                Map<String, Object> theData = document.getData();
                                String userTraits = (String) theData.get("UserTraits");
                                Log.d(TAG, "userTraits: " + userTraits);
                                String[] traitsArray2 = userTraits.split(" ");
                                for (int i = 0; i < traitsArray2.length; i++) {
                                    traitsArray2[i] = traitsArray2[i].trim();
                                }
                                //adss a users ID  who has their first trait matched and compared to a matchlist
                                matchList.add(document.getId());
                                Log.d(TAG, "all matched users: " + matchList.toString());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    /**
     * method chatMatch
     * Goes throught the matchlist arraylist
     * goes through a an array if the size is grtr than or equal to 7 others
     * this is the chat roullete so it needs a pool of 7 users.
     * <p>
     * Adds Chat token to the 2 matched users DB docuement
     *
     * @param view
     */
    public void chatMatch(View view) {
        //if the matchlist is over 7 people, it randomly chooses one of the 7 people in the list from random array list positions
        //user is added ot the chat list
        if (matchList.size() >= 7) {
            int count = 0;
            for (int i = 0; i < 6; i++) {
                int index = (int) (Math.random() * matchList.size());
                chatList.add(matchList.get(index));
                Log.d(TAG, "Count is: " + count + "all chat users: " + chatList.toString());
                if (count == 6)
                    break;
                count++;
            }
        } else {
            //use all matches available if there aren't 7 people
            // this could just be the code used below where it just chosses a random chat user from the user available
            Log.d(TAG, "ah ok ");
            for (int i = 0; i < matchList.size(); i++) {
                int index = (int) (Math.random() * matchList.size());
                chatList.add(matchList.get(i));
                Log.d(TAG, "all chat users: " + chatList.toString());
            }
        }
        Log.d(TAG, "matchlist size " + matchList.size());
        //from the chatlist a random person is chosen, that userID is then called the "itsAMatch" user string.
        int random = new Random().nextInt(matchList.size()) + 0; //returns a random value between 0 and 7
        itsAMatch = chatList.get(random);
        Log.d(TAG, "what user was choosen to chat " + itsAMatch);
        //token is a assembled string of "chatuserID -(should be random but currently its fixed) and the USERID's of both matched users.
        token = chatUserID + itsAMatch;

        //needs a blank token value created to be later updated
        //the current user has its DB document updated to reflect that they were matched with a new user.
        //uses the "token" field and ads to current user.
        DocumentReference updateToken = db.collection("users").document(userDocID);
        updateToken
                .update("chatToken", token)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
        Log.d(TAG, "Editing other users match token to: " + itsAMatch + "the token value is: " + token);

        //Updates the other not current matched user and adds the same chat "token" to their database document.
        DocumentReference updateOtherToken = db.collection("users").document(itsAMatch);
        updateToken
                .update("chatToken", token)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    /**
     * @param view
     * @method addChatMessage
     * @Notes Method enables the chat system for two users to send messages
     */
    public void addChatMessage(View view) {
        //sets the message name with the chatID
        String messageName = nameTextS;
        //gets message text from the the text entry field and converts to string
        EditText chatMessage = (EditText) findViewById(R.id.chatText);
        String commentText = chatMessage.getText().toString();

        //creates a new hashmap for chats
        Map<String, Object> chats = new HashMap<>();
        chats.put("messageName", messageName);
        chats.put("message", commentText);
        Long time = System.currentTimeMillis();
        chats.put("time", time);

        //adds the message to the "token" message stream
        db.collection(String.valueOf(token))
                .add(chats)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(DEBUG_TAG, "Document Snapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(DEBUG_TAG, "Error adding new message: ", e);
                    }
                });
        //cals chat method
        chat();
    }

    /**
     * @method chat
     * @notes Displays all fields (in time order) of a chat document (of chat token).
     * each document is a new "chat stream" and is identified by the chat token.
     * Inside the document there are fields, each field is a new message these messages are outputed in order of time created in database.
     */
    public void chat() {
        db.collection(String.valueOf(token)).orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    //retrives all fields (messages)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            StringBuffer messages = new StringBuffer();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //pulls message content from database and puts it into a hashmap.
                                Map<String, Object> data = document.getData();
                                String name = (String) data.get("messageName");
                                String comment = (String) data.get("message");
                                SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss");
                                String commentTime = date.format(new Date());
                                // message data as a ID, name of the tokenID, comment text and the time.
                                messages.append("\t- " + name + ": " + commentTime + ": '" + comment + "'" + "\n");
                            }
                            //display message string into chatview on the notfications/chat layout page.
                            TextView messageView = (TextView) findViewById(R.id.chatView);
                            messageView.setText(messages.toString());

                        } else {
                            Log.w(DEBUG_TAG, "Error getting documents from Firestore: ", task.getException());
                        }
                    }
                });
    }
}
