/*
 * Author: Justin Mountain
 * Declaration: This is my own original work and is free from Plagiarism.
 */
package justin.pexelsimagesearch;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

/**
 * This class is used to store data between orientation changes
 */
public class PexelsViewModel extends ViewModel {
    public MutableLiveData<ArrayList<PexelResponse>> allRows = new MutableLiveData< >();
    public MutableLiveData<PexelResponse> selectedRow = new MutableLiveData< >();
}
