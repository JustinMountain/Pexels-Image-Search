/*
 * Author: Justin Mountain
 * Declaration: This is my own original work and is free from Plagiarism.
 */
package justin.pexelsimagesearch;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * This interface acts as the DAO object for the Pexels favorites database
 */
@Dao
public interface PexelsSaveDAO {

    /**
     * Insert a PexelsSave object into the database
     * @param response The PexelsSave object to be stored in the database
     */
    @Insert
    public void insertResponse(PexelsSave response);

    /**
     * Queries the Pexels favortites database for all entries
     * @return a List<PexelsSave> containing all saved photos
     */
    @Query("SELECT * FROM PexelsSave")
    public List<PexelsSave> getAllResponses();

    /**
     * Delete a PexelsSave object from the database
     * @param response The PexelsSave object to be removed from the database
     */
    @Delete
    public void deleteResponse(PexelsSave response);
}
