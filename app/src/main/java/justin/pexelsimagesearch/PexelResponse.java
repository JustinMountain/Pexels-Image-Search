/*
 * Author: Justin Mountain
 * Declaration: This is my own original work and is free from Plagiarism.
 */
package justin.pexelsimagesearch;

import android.graphics.Bitmap;

/**
 * This class represents one Pexels query result, containing all relevant responses
 */
public class PexelResponse {
    protected Bitmap image;
    protected Bitmap fullsizeImage;
    protected int id;
    protected String pexelsURL;
    protected String thumbnailPhoto;
    protected String fullsizePhoto;
    protected String photographer;
    protected String photoHeight;
    protected String photoWidth;

    /**
     * Overloaded constructor which stores all relevant query responses
     * @param image the Bitmap image of its thumbnail
     * @param id The photo ID, provided by Pexels
     * @param pexelsURL The URL to Pexels page for this photo
     * @param thumbnailPhoto The url for the thumbnail image
     * @param fullsizePhoto The url for the fullsize image
     * @param photographer The photographer's name
     * @param photoHeight The photo's height in pixels
     * @param photoWidth The photo's width in pixels
     */
    PexelResponse(Bitmap image, int id, String pexelsURL, String thumbnailPhoto, String fullsizePhoto, String photographer, String photoHeight, String photoWidth) {
        this.image = image;
        this.id = id;
        this.pexelsURL = pexelsURL;
        this.thumbnailPhoto = thumbnailPhoto;
        this.fullsizePhoto = fullsizePhoto;
        this.photographer = photographer;
        this.photoHeight = photoHeight;
        this.photoWidth = photoWidth;
    }

    /**
     * Returns a Bitmap of the image
     * @return a Bitmap of the image
     */
    public Bitmap getImage() { return image; };

    /**
     * Returns the photo's id
     * @return the photo's id
     */
    public int getID() { return id; };

    /**
     * Returns the photo's URL
     * @return the photo's URL
     */
    public String getPexelsURL() { return pexelsURL; };

    /**
     * Returns the URL to the thumbnail image
     * @return the URL to the thumbnail image
     */
    public String getThumbnailURL() { return thumbnailPhoto; };

    /**
     * Returns the URL to the fullsize image
     * @return the URL to the fullsize image
     */
    public Bitmap getFullsizeImage() { return fullsizeImage; };

    /**
     * Returns the name of the photographer
     * @return the name of the photographer
     */
    public String getPhotographer() { return photographer; };

    /**
     * Returns the photo's height in pixels
     * @return the photo's height in pixels
     */
    public String getPhotoHeight() { return photoHeight; };

    /**
     * Returns the photo's width in pixels
     * @return the photo's width in pixels
     */
    public String getPhotoWidth() { return photoWidth; };
}
