/*
 * Author: Justin Mountain
 * Declaration: This is my own original work and is free from Plagiarism.
 */
package justin.pexelsimagesearch;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * This class represents one Pexels query result to save in the database, containing all relevant responses (does not save the image)
 */
@Entity
public class PexelsSave {
    @PrimaryKey (autoGenerate = true)
    @ColumnInfo(name="id")
    int id;

    @ColumnInfo(name="photoID")
    int photoID;

    @ColumnInfo(name="pexelsURL")
    String pexelsURL;

    @ColumnInfo(name="thumbnailPhoto")
    String thumbnailPhoto;

    @ColumnInfo(name="fullsizePhoto")
    String fullsizePhoto;

    @ColumnInfo(name="photographer")
    String photographer;

    @ColumnInfo(name="photoHeight")
    String photoHeight;

    @ColumnInfo(name="photoWidth")
    String photoWidth;

    /**
     * Overloaded constructor which stores all relevant query responses
     * @param photoID The photo ID, provided by Pexels
     * @param pexelsURL The URL to Pexels page for this photo
     * @param thumbnailPhoto The url for the thumbnail image
     * @param fullsizePhoto The url for the fullsize image
     * @param photographer The photographer's name
     * @param photoHeight The photo's height in pixels
     * @param photoWidth The photo's width in pixels
     */
    PexelsSave(int photoID, String pexelsURL, String thumbnailPhoto, String fullsizePhoto, String photographer, String photoHeight, String photoWidth) {
        this.photoID = photoID;
        this.pexelsURL = pexelsURL;
        this.thumbnailPhoto = thumbnailPhoto;
        this.fullsizePhoto = fullsizePhoto;
        this.photographer = photographer;
        this.photoHeight = photoHeight;
        this.photoWidth = photoWidth;
    }

    /**
     * Returns the photo's id used in the database
     * @return the photo's id used in the database
     */
    public int getID() { return id; };

    /**
     * Returns the photo's id used in the database
     * @return the photo's id used in the database
     */
    public int getPhotoID() { return photoID; };

    /**
     * Returns the photo's URL
     * @return the photo's URL
     */
    public String getPexelsURL() { return pexelsURL; };

    /**
     * Returns the photo's URL
     * @return the photo's URL
     */
    public String getThumbnailURL() { return thumbnailPhoto; };

    /**
     * Returns the photo's URL
     * @return the photo's URL
     */
    public String getFullsizePhoto() { return fullsizePhoto; };

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
