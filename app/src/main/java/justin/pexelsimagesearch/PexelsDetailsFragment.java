/*
 * Author: Justin Mountain
 * Declaration: This is my own original work and is free from Plagiarism.
 */
package justin.pexelsimagesearch;

import android.content.Context;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import justin.pexelsimagesearch.databinding.PexelsFragmentBinding;

/**
 * This class determines the contents of the fragment which loads when selecting an item in the RecyclerView
 */
public class PexelsDetailsFragment extends Fragment {

    private PexelResponse selected;
    private Context context;
    private PexelsSaveDAO pexelsDAO;
    private PexelsSaveDatabase db;
    WebView webView;

    /**
     * Constructor which requires a PexelResponse object for use in determining the fragment's contents
     * @param obj
     */
    public PexelsDetailsFragment(Context context, PexelResponse obj) {
        this.context = context;
        this.selected = obj;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Instantiate the Activity and variables
        super.onCreateView(inflater, container, savedInstanceState);
        PexelsFragmentBinding binding = PexelsFragmentBinding.inflate(inflater);
        db = Room.databaseBuilder(context, PexelsSaveDatabase.class, "pexels-database").build();
        pexelsDAO = db.pexelDAO();

        // Determine if the selected image is a favorite
        // Database actions should be done on a background thread
        Executor readThread = Executors.newSingleThreadExecutor();
        readThread.execute(() -> {

            // Create a list of all saved favorite images
            List<PexelsSave> favorites = pexelsDAO.getAllResponses();

            // Loop over all saved items, if the item in the fragment is a favorite then display a broken heart
            for (PexelsSave fav : favorites) {
                if (selected.id == fav.photoID) {
                    swapHeartToBroken(binding);
                    break;
                }
            }
        });

        // Declare the contents of the fragment from the passed-in PexelResponse object
        binding.pexelsImage.setImageBitmap( selected.fullsizeImage );
        binding.pexelsCreditDisplay.setText( selected.photographer );
        binding.pexelsURLDisplay.setText( selected.pexelsURL );
        binding.pexelsHeightDisplay.setText( selected.photoHeight );
        binding.pexelsWidthDisplay.setText( selected.photoWidth );

        // Determine the action taken when selecting the displayed URL
        binding.pexelsURLDisplay.setOnClickListener( click -> {

            // Load the PexelsURL into the WeebView and make it visible
            webView = (WebView) binding.pexelsWebView;
            webView.getSettings().setJavaScriptEnabled(false);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.setVisibility(View.VISIBLE);
            webView.setWebViewClient( new SSLTolerentWebViewClient() );
            webView.loadUrl(selected.pexelsURL.replace("https", "http")); // its working
        });

        // Button to add to favorites
        binding.pexelsHeart.setOnClickListener( click -> {

            // Create an alert asking if user wants to add the image to their favorites
            AlertDialog.Builder builder = new AlertDialog.Builder( PexelsDetailsFragment.this.getContext() );
            builder.setTitle(getActivity().getString(R.string.pexels_alert_title))
                    .setMessage(getActivity().getString(R.string.pexels_alert_add))
                    .setNegativeButton( getActivity().getString(R.string.pexels_alert_no), (dialog, cl) -> { }) // User selects No
                    .setPositiveButton( getActivity().getString(R.string.pexels_alert_yes), (dialog, cl) -> { // User selects Yes

                        // Database actions should be done on a background thread
                        readThread.execute(() -> {
                            boolean isUnique = true;

                            // Ensure the photo isn't already in the favorites DB
                            List<PexelsSave> favorites = pexelsDAO.getAllResponses();
                            for (PexelsSave fav : favorites) {
                                if (selected.id == fav.photoID) {
                                    isUnique = false;
                                    break;
                                }
                            }

                            // If the photo isn't in the DB...
                            if (isUnique) {
                                // Create a PexelsSave object to insert into the database
                                PexelsSave saveData = new PexelsSave(selected.id, selected.pexelsURL, selected.thumbnailPhoto, selected.fullsizePhoto, selected.photographer, selected.photoHeight, selected.photoWidth);

                                // Database actions should be done on a background thread
                                Executor insertThread = Executors.newSingleThreadExecutor();
                                insertThread.execute(() -> {
                                    // Insert the image into the database
                                    pexelsDAO.insertResponse(saveData);
                                });

                                // Display the unfavorite broken heart and notify the photo has been added to the DB
                                swapHeartToBroken(binding);
                                Snackbar.make(container, getActivity().getString(R.string.pexels_snackbar_added), Snackbar.LENGTH_LONG)
                                        .show();
                            } else {
                                // Tell the user if the photo was already in their favorites
                                Snackbar.make(container, getActivity().getString(R.string.pexels_snackbar_not_unique), Snackbar.LENGTH_LONG)
                                        .show();
                            }
                        });
                    })
                    .create().show();
        });

        // Button to remove favorites
        binding.pexelsBrokenHeart.setOnClickListener( click -> {

            // Create an alert asking if user wants to remove the photo from their favorites
            AlertDialog.Builder builder = new AlertDialog.Builder( PexelsDetailsFragment.this.getContext() );
            builder.setTitle(getActivity().getString(R.string.pexels_alert_title))
                    .setMessage(getActivity().getString(R.string.pexels_alert_remove))
                    .setNegativeButton( getActivity().getString(R.string.pexels_alert_no), (dialog, cl) -> { }) // User selects No
                    .setPositiveButton( getActivity().getString(R.string.pexels_alert_yes), (dialog, cl) -> { // User selects Yes

                        // Database actions should be done on a background thread
                        Executor removeThread = Executors.newSingleThreadExecutor();
                        removeThread.execute(() -> {

                            List<PexelsSave> favorites = pexelsDAO.getAllResponses();
                            for (PexelsSave fav : favorites) {
                                if (selected.id == fav.photoID) {

                                    // If the photo is in the DB, remove it
                                    pexelsDAO.deleteResponse(fav);

                                    // Attempt to delete a stored thumbnail image for this photo
                                    String pexelsImagePathname = context.getFilesDir() + "/pexels-" + selected.id + ".png";
                                    File thumbnailFile = new File(pexelsImagePathname);
                                    if (thumbnailFile.exists()) {
                                        thumbnailFile.delete();
                                    }

                                    // Attempt to delete a stored fullsize image for this photo
                                    String pexelsFullsizeImagePathname = context.getFilesDir() + "/pexels-fullsize-" + selected.id + ".png";
                                    File fullsizeFile = new File(pexelsFullsizeImagePathname);
                                    if (fullsizeFile.exists()) {
                                        fullsizeFile.delete();
                                    }

                                    // Don't need to continue looking if we got this far
                                    break;
                                }
                            }

                            // Display the favorite full heart and notify the photo has been removed from the DB
                            swapHeartToFull(binding);
                            Snackbar.make(container, getActivity().getString(R.string.pexels_snackbar_removed), Snackbar.LENGTH_LONG)
                                    .show();
                        });
                    })
                    .create().show();
        });

        return binding.getRoot();
    }

    /**
     * Swaps a full heart image for a broken heart
     * @param binding Fragment binding to use to invert visible icon
     */
    private void swapHeartToBroken(PexelsFragmentBinding binding) {
        getActivity().runOnUiThread( (  )  -> {
            binding.pexelsHeart.setVisibility(View.INVISIBLE);
            binding.pexelsBrokenHeart.setVisibility(View.VISIBLE);
        });
    }

    /**
     * Swaps a broken heart image for a full heart
     * @param binding Fragment binding to use to invert visible icon
     */
    private void swapHeartToFull(PexelsFragmentBinding binding) {
        getActivity().runOnUiThread( (  )  -> {
            binding.pexelsHeart.setVisibility(View.VISIBLE);
            binding.pexelsBrokenHeart.setVisibility(View.INVISIBLE);

        });
    }

    /**
     * This class is used to ignore SSL Certificate errors caused when trying to display a URL in the WebView
     */
    private class SSLTolerentWebViewClient extends WebViewClient {

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // Ignore SSL certificate errors
        }
    }
}
