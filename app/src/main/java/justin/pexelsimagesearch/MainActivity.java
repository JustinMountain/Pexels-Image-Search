/*
 * Author: Justin Mountain
 * Declaration: This is my own original work and is free from Plagiarism.
 */
package justin.pexelsimagesearch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import justin.pexelsimagesearch.databinding.ActivityMainBinding;

/**
 * This is the Activity for accessing Pexels
 * This class contains the interactive components for the Pexels Activity.
 * @author Justin Mountain
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {

    // Class variables
    protected SharedPreferences prefs;
    protected ActivityMainBinding binding;
    protected RecyclerView.Adapter<PexelsRow> adapter;
    protected PexelsViewModel pexelsModel;
    protected ArrayList<PexelResponse> allRows = new ArrayList<>();
    protected PexelsSaveDAO pexelsDAO;
    PexelsSaveDatabase db;

    // To use with Pexels API
    final String API_KEY = "563492ad6f917000010000019cd9260be5aa4f80b95db12f57533307";
    protected String queryURL;

    // To use with Volley
    protected RequestQueue queue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Instantiate the Activity and variables
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        queue = Volley.newRequestQueue(this);
        db = Room.databaseBuilder(this, PexelsSaveDatabase.class, "pexels-database").build();
        pexelsDAO = db.pexelDAO();
        setTitle(R.string.pexels_title);
        setContentView(binding.getRoot());
        setSupportActionBar(binding.pexelsToolbar);

        NavigationView navigationView = findViewById(R.id.pexels_navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        loadFavorites();

        // Displays the previous search term in EditText on startup
        prefs = getSharedPreferences("LastQuery", Context.MODE_PRIVATE);
        binding.pexelsSearchTerm.setText(prefs.getString("PreviousQuery", ""));

        // Declare RecyclerView as vertical linear layout
        binding.pexelsRecycler.setLayoutManager(new LinearLayoutManager(this));

        // Check MutableLiveData to maintain integrity with orientation changes
        pexelsModel = new ViewModelProvider(this).get(PexelsViewModel.class);
        allRows = pexelsModel.allRows.getValue();
        if(allRows == null) {
            pexelsModel.allRows.postValue( allRows = new ArrayList<>() );
        }

        // Behaviour for the Search button
        binding.pexelsSubmitSearch.setOnClickListener( click -> {
            // Grab the search term
            String searchTerm = binding.pexelsSearchTerm.getText().toString();

            // Create a search url
            queryURL = "https://api.pexels.com/v1/search?query=" + searchTerm;
            try { // Ensure url is in proper format
                URLEncoder.encode(queryURL, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Clear the RecyclerView, tell the user of the search through Toast, update SharedPreferences, and clear the search field
            clearPexelsRecycler();
            searchToast(searchTerm);
            updateLastQuery(searchTerm);
            clearPexelSearch();

            // Send the HTTP Request
            sendVolleyRequest(queryURL);
        });

        // Define the behaviour of the heart
        binding.pexelsViewFavs.setOnClickListener( click -> {
            // Clear the RecyclerView
            clearPexelsRecycler();
            loadFavorites();
        });

        // Define the behaviour of each new row in the RecyclerView
        binding.pexelsRecycler.setAdapter(adapter = new RecyclerView.Adapter<PexelsRow>() {
            // Defines the row
            @NonNull
            @Override                                                      // Layout type (no choice in this project)
            public PexelsRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View root = getLayoutInflater().inflate(R.layout.pexels_row, parent, false);
                return new PexelsRow( root );
            }

            // Used to set variables within the row
            @Override                                            // [Position] in RecyclerView / Array
            public void onBindViewHolder(@NonNull PexelsRow row, int position) {
                // Grab variables from Array
                String thisPhotographer = allRows.get(position).getPhotographer();
                Bitmap thisImage = allRows.get(position).image;

                // Set variables to the row
                row.photoCredit.setText(thisPhotographer);
                row.image.setImageBitmap(thisImage);
            }

            // Returns the number of rows in Array
            @Override
            public int getItemCount() {
                return allRows.size();
            }
        });

        // Define the behaviour for selecting a row
        pexelsModel.selectedRow.observe(this, (thisRow) -> {

            // Create the item's saved image pathname
            String pexelsImagePathname = getFilesDir() + "/pexels-fullsize-" + thisRow.id + ".png";
            File file = new File(pexelsImagePathname);

            // If the fullsize image exists, load it and create a fragment to display details
            if (file.exists()) {
                Bitmap image = BitmapFactory.decodeFile(pexelsImagePathname);
                thisRow.fullsizeImage = image;

                // Create a PhotoDetailsFragment to display image details
                PexelsDetailsFragment photoFragment = new PexelsDetailsFragment( this, thisRow );
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.pexelsDetailsFragment, photoFragment)
                        .addToBackStack("photo details")
                        .commit();
            } else {
                // If it doesn't exist, request the fullsize image in a separate thread
                Executor thread = Executors.newSingleThreadExecutor();
                thread.execute(() -> {
                    ImageRequest imgReq = new ImageRequest(thisRow.fullsizePhoto, new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            try {
                                Bitmap image = bitmap;
                                String photoID = "pexels-fullsize-" + thisRow.id + ".png";
                                image.compress(Bitmap.CompressFormat.PNG, 100, MainActivity.this.openFileOutput(photoID, Activity.MODE_PRIVATE));

                                // Place the received image into the PexelResponse object
                                thisRow.fullsizeImage = image;

                                // Run the changes to the UI here
                                runOnUiThread( (  )  -> {

                                    // Create a PhotoDetailsFragment to display image details
                                    PexelsDetailsFragment photoFragment = new PexelsDetailsFragment( getBaseContext(), thisRow );
                                    getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.pexelsDetailsFragment, photoFragment)
                                            .addToBackStack("photo details")
                                            .commit();
                                });
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 1024, 1024, ImageView.ScaleType.CENTER, null, (error ) -> { })
                            // Authorization header with API Key added to the getHeaders method for Pexels API access
                    {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Authorization", API_KEY);
                            return params;
                        }
                    };
                    // Add the created ImageRequest to the Queue
                    queue.add(imgReq);
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.pexels_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch( item.getItemId() )
        {
            // Create an alert describing the title, author, and version number
            // Create an alert describing how to use the app
            case R.id.pexels_menu_help:
                AlertDialog.Builder helpBuilder = new AlertDialog.Builder( this );
                helpBuilder.setTitle(this.getString(R.string.pexels_menu_help_title))
                        .setMessage(this.getString(R.string.pexels_menu_help_message))
                        .setPositiveButton( this.getString(R.string.pexels_menu_help_ok), (dialog, cl) -> {
                        })
                        .create().show();
                break;

            // Clicking on one of the links...
            case R.id.pexels_menu_movies:
            case R.id.pexels_menu_soccer:
            case R.id.pexels_menu_ticketmaster:
                // Create and show a toast
                Context context = getApplicationContext();
                String links = "Links to other projects don't work.";
                CharSequence text = links;
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                break;
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.pexels_drawer_help:
                AlertDialog.Builder helpBuilder = new AlertDialog.Builder( this );
                helpBuilder.setTitle(this.getString(R.string.pexels_menu_help_title))
                        .setMessage(this.getString(R.string.pexels_menu_help_message))
                        .setPositiveButton( this.getString(R.string.pexels_menu_help_ok), (dialog, cl) -> {
                        })
                        .create().show();
                break;
            case R.id.pexels_drawer_movies:
            case R.id.pexels_drawer_soccer:
            case R.id.pexels_drawer_ticketmaster:
                // Create and show a toast
                Context context = getApplicationContext();
                String links = "Links to other projects don't work.";
                CharSequence text = links;
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                break;
        }

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.pexels_drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

        return false;
    }

    /**
     * Updates SharedPreferences file to contain the last query
     * @param query String representing the last query
     */
    private void updateLastQuery(String query) {
        prefs = getSharedPreferences("LastQuery", Context.MODE_PRIVATE);

        // Creates the SharedPreferences editor on the SharedPreferences obj
        SharedPreferences.Editor editor = prefs.edit();

        // Creates (or updates) previous query to the input value
        editor.putString("PreviousQuery", query);

        // Apply all editor changes to SharedPreferences (all changes are saved at one time)
        editor.apply();
    }

    /**
     * Clears the EditText containing the search term
     */
    private void clearPexelSearch() {
        // Reset the search term
        binding.pexelsSearchTerm.setText("");
    }

    /**
     * Creates a Toast to notify the user their search has started
     * @param query A String containing the search term
     */
    private void searchToast(String query) {
        // Creates the variables to be used in the Toast
        Context context = getApplicationContext();

        String searching = getResources().getString(R.string.pexels_toast_search) + " ";
        CharSequence text = searching + query + "...";
        int duration = Toast.LENGTH_SHORT;

        // Create and show the Toast
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /**
     * Sends a JsonObjectRequest using Volley with an input string for the query's URL, extracts relevant information, and sends and ImageRequest
     * @param url A URL containing a query to be sent to the Pexels API
     */
    private void sendVolleyRequest(String url) {

        // HTTP Requests should be completed on a separate thread
        Executor thread = Executors.newSingleThreadExecutor();
        thread.execute(() -> {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    (response) -> {
                        try {
                            // Pull from the Response object to get the photos from the response
                            JSONArray photosArray = response.getJSONArray("photos");

                            // Loop over the array of photos
                            for (int i = 0; i < photosArray.length(); i++) {

                                // Extract one photo from the array
                                JSONObject thisPhoto = photosArray.getJSONObject(i);

                                // Grab each important element from the JSON object
                                int id = thisPhoto.getInt("id");
                                String photoHeight = thisPhoto.getString("height");
                                String photoWidth = thisPhoto.getString("width");
                                String photographer = thisPhoto.getString("photographer");
                                String pexelsURL = thisPhoto.getString("url");

                                // Image locations are stored one layer deeper in the 'src' key
                                JSONObject photoSrc = thisPhoto.getJSONObject("src");
                                String thumbnailPhoto = photoSrc.getString("large");
                                String fullsizePhoto = photoSrc.getString("original");

                                // Use extracted information to search for the image
                                findPexelImage(id, pexelsURL, thumbnailPhoto, fullsizePhoto, photographer, photoHeight, photoWidth);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    (error) -> { })
                    // Authorization header with API Key added to the getHeaders method for Pexels API access
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", API_KEY);
                    return params;
                }
            };
            // Add the created JsonObjectRequest to the Queue
            queue.add(request);
        });
    }

    /**
     * Uses the information provided by the Pexels API to send a Volley request for the image and saves it as a Bitmap
     * @param id The photo ID, provided by Pexels
     * @param pexelsURL The URL to Pexels page for this photo
     * @param thumbnailPhoto The url for the thumbnail image
     * @param fullsizePhoto The url for the fullsize image
     * @param photographer The photographer's name
     * @param photoHeight The photo's height in pixels
     * @param photoWidth The photo's width in pixels
     */
    private void findPexelImage(int id, String pexelsURL, String thumbnailPhoto, String fullsizePhoto, String photographer, String photoHeight, String photoWidth) {

        // Create the item's saved image pathname
        String pexelsImagePathname = getFilesDir() + "/pexels-" + id + ".png";

        // If a file exists for this item, create a PexelResponse object using it, otherwise find the image
        File file = new File(pexelsImagePathname);
        if (file.exists()) {
            Bitmap image = BitmapFactory.decodeFile(pexelsImagePathname);
            createPexelResponse(image, id, pexelsURL, thumbnailPhoto, fullsizePhoto, photographer, photoHeight, photoWidth);
        } else {

            // Volley requests are sent on a separate thread
            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() -> {
                ImageRequest imgReq = new ImageRequest(thumbnailPhoto, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        try {

                            // Take the received image, save it as .png
                            Bitmap image = bitmap;
                            String photoID = "pexels-" + id + ".png";
                            image.compress(Bitmap.CompressFormat.PNG, 100, MainActivity.this.openFileOutput(photoID, Activity.MODE_PRIVATE));

                            // Run the changes to the UI on the main thread
                            runOnUiThread( (  )  -> {
                                // Create the PexelResponse object using the received image
                                createPexelResponse(image, id, pexelsURL, thumbnailPhoto, fullsizePhoto, photographer, photoHeight, photoWidth);
                            });
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }, 1024, 1024, ImageView.ScaleType.CENTER, null, (error ) -> { })
                        // Authorization header with API Key added to the getHeaders method for Pexels API access
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Authorization", API_KEY);
                        return params;
                    }
                };
                // Add the created ImageRequest to the Queue
                queue.add(imgReq);
            });
        }
    }

    /**
     * Loops over all items in the RecyclerView and removes them one by one, clearing any associated images from the cache
     */
    private void clearPexelsRecycler() {
        int length = allRows.size();

        // As long as there are items in the RecyclerView...
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                // Take the first item in the RecyclerView
                PexelResponse thisRow = allRows.get(0);

                // Create the item's saved image pathname
                String pexelsImagePathname = getFilesDir() + "/pexels-" + thisRow.id + ".png";

                // If a file exists for this item, delete it
                File file = new File(pexelsImagePathname);
                if (file.exists()) {
                    file.delete();
                }

                // Remove the item from the RecyclerView ArrayList
                allRows.remove(0);
                adapter.notifyItemRemoved(0);
            }
        }
    }

    private void loadFavorites() {
        // Database actions should be done on a background thread
        Executor readThread = Executors.newSingleThreadExecutor();
        readThread.execute(() -> {

            // Create a list of all saved favorite images
            List<PexelsSave> favorites = pexelsDAO.getAllResponses();

            // Loop over each favorite image to look for it's image
            for (PexelsSave fav : favorites) {
                runOnUiThread( (  )  -> {
                    findPexelImage(fav.photoID,
                            fav.pexelsURL,
                            fav.thumbnailPhoto,
                            fav.fullsizePhoto,
                            fav.photographer,
                            fav.photoHeight,
                            fav.photoWidth);
                });
            }
        });
    }

    /**
     * Creates a PexelResponse object, adds it to the ArrayList, and notifies that adapter of the change
     * @param image The Bitmap image to be displayed in the RecyclerView
     * @param id The photo ID, provided by Pexels
     * @param pexelsURL The URL to Pexels page for this photo
     * @param thumbnailPhoto The url for the thumbnail image
     * @param fullsizePhoto The url for the fullsize image
     * @param photographer The photographer's name
     * @param photoHeight The photo's height in pixels
     * @param photoWidth The photo's width in pixels
     */
    private void createPexelResponse(Bitmap image, int id, String pexelsURL, String thumbnailPhoto, String fullsizePhoto, String photographer, String photoHeight, String photoWidth) {

        // Creates the PexelResponse object
        PexelResponse thisRow = new PexelResponse(image, id, pexelsURL, thumbnailPhoto, fullsizePhoto, photographer, photoHeight, photoWidth);

        // Add to ArrayList and notify of change
        allRows.add(thisRow);
        adapter.notifyItemInserted(allRows.size()-1);
    }

    /**
     * Represents a single row to be added to the RecyclerView
     */
    class PexelsRow extends RecyclerView.ViewHolder {
        TextView photoCredit;
        ImageView image;
        ImageView heart;

        /**
         * Creates a row and sets variables to access each view
         * @param itemView
         */
        public PexelsRow(@NonNull View itemView) {
            super(itemView);

            // Set the Image and Photographer's name
            photoCredit = itemView.findViewById( R.id.pexels_photo_credit );
            image = itemView.findViewById( R.id.pexels_image );

            // Behavior for clicking on the row
            itemView.setOnClickListener( clk -> {
                int position = getAbsoluteAdapterPosition();
                PexelResponse selected = allRows.get(position);

                pexelsModel.selectedRow.postValue(selected);
            });
        }
    }
}
