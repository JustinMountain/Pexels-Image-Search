/*
 * Author: Justin Mountain
 * Declaration: This is my own original work and is free from Plagiarism.
 */
package justin.pexelsimagesearch;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * This abstract class represents the Database of PexelsSave objects that will be stored
 */
@Database(entities = {PexelsSave.class}, version = 2)
public abstract class PexelsSaveDatabase extends RoomDatabase {

    // DAO Object for accessing the database
    public abstract PexelsSaveDAO pexelDAO();
}
