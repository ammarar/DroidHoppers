/// \package net.g3ti.droidhopper.phoneagent.datafile.storage
/// \brief Contains classes that are responsible with data file and storage.
package net.g3ti.droidhopper.phoneagent.datafile.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

//////////////////////////////////////////////////////////////////////
/// \class       DataFile
/// \brief       Represents the data file that is transferred across phones.
/// \author      Ammar Alrashed
/// \date        06/08/2012
//////////////////////////////////////////////////////////////////////
public class DataFile extends File
{
    private static final long serialVersionUID = -6040309355979197230L; ///< A number used to verify serialization from senders and receivers.
    static final String INCOMPLETE_FILE_APPENDIX = "dhincomplete"; ///< The appendix used for signaling a file that is incomplete (partially transferred).
    private static final String LOG_TAG = DataFile.class.getSimpleName(); ///< Tag for logging.
    private static final String ORIGIN_UID = "OriginUID"; ///< Key of the origin UID in the metadata.
    private static final String CREATION_TIMESTAMP = "CreationTimestamp"; ///< Key of the creation timestamp in the metadata.
    private static final String FILE_NAME = "FileName"; ///< Key of the file name in the metadata.

    //////////////////////////////////////////////////////////////////////
    /// \fn         DataFile(String path)
    /// \brief         Initialize the object using the super class constructor.
    /// \param[in]     path - The path of the data file.
    /// \author      Ammar Alrashed
    /// \date        06/08/2012
    //////////////////////////////////////////////////////////////////////
    public DataFile(String path)
    {
        super(path);
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         DataFile(File dir, String name)
    /// \brief         Initialize the object using the super class constructor.
    /// \param[in]     dir - The path of the data file.
    /// \param[in]     name - The data file name.
    /// \author      Ammar Alrashed
    /// \date        06/08/2012
    //////////////////////////////////////////////////////////////////////
    public DataFile(File dir, String name)
    {
        super(dir, name);
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         isComplete()
    /// \brief         Test the file extension name and checking whether it
    ///             is complete or not.
    /// \return        boolean - TRUE if the data file is complete.
    /// \author        Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    public boolean isComplete()
    {
        // Test the end of the file.
        boolean isComplete = !this.getName().endsWith(INCOMPLETE_FILE_APPENDIX);
        Log.d(LOG_TAG, "Checking file " + this.getName() + " (complete: " + isComplete + ")");
        return isComplete;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         getRemoteIncompleteDataFileName()
    /// \brief         Get the current data file name in the remote machine.
    /// \return        String - the name of the data file name.
    /// \author        Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    public String getRemoteIncompleteDataFileName()
    {
        StringBuilder remoteIncompleteDataFileName = new StringBuilder(this.getName());
        
        // Append incomplete file suffix.
        remoteIncompleteDataFileName.append('.');
        remoteIncompleteDataFileName.append(DataFile.INCOMPLETE_FILE_APPENDIX);
        Log.d(LOG_TAG, "Remote incomplete data file name: " + remoteIncompleteDataFileName.toString());
        return remoteIncompleteDataFileName.toString();
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         retrieve(String fileId)
    /// \brief      Gets a file that is identified by its file ID.
    ///             The file can be either a complete or an incomplete file.
    /// \param[in]  fileId - The ID of the file that should be returned. This ID is 
    ///             the beginning of the file name, not including the incomplete file suffix.
    ///             with the extension of the file.
    /// \return     DataFile - The data file that is retrieved, or null if 
    ///             it does not exist.
    /// \author     Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    public static DataFile retrieve(String fileId)
    {
        List<DataFile> dataFiles = DataFileRepository.getDataFiles();
        DataFile retrievedFile = null;
        
        // Iterate over the files to try to find if a datafile with that ID is actually there.
        for(DataFile dataFile : dataFiles)
        {
            Log.d(LOG_TAG, "ID of datafile to retrieve: " + fileId);            
            Log.d(LOG_TAG, "Name of datafile being checked: " + dataFile.getName());

            // Compare the ID we are looking for with the beginning of the file, as the filename may also contain the "incomplete" suffix.
            boolean compareDataFileNames = dataFile.getName().startsWith(fileId);            
            if(compareDataFileNames)
            {
                Log.d(LOG_TAG, "Datafile found inside the repository.");
                retrievedFile = dataFile;
                break;
            }
        }
        
        return retrievedFile;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         getMetadata(String key)
    /// \brief         Get the metadata information of the data file.
    /// \param[in]     key - They key for the metadata.
    /// \return     String - The metadata information.
    /// \author      Ammar Alrashed
    /// \date        07/28/2012
    //////////////////////////////////////////////////////////////////////
    private String getMetadata(String key)
    {
        String result = null;
        ZipFile zipFile = null;
        try
        {
            zipFile = new ZipFile(this);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry entry = null;
            
            // Iterate over the entries and once the meta data file is found, break.
            while (entries.hasMoreElements())
            {
                entry = entries.nextElement();
                boolean isMetadataFile = entry.getName().endsWith(".json");
                if (isMetadataFile)
                {
                    break;
                }
            }
            
            boolean entryIsNull = entry == null;
            if (entryIsNull)
            {
                Log.e(LOG_TAG,"A metadata file was not found in the application.");
                return null;
            }
            
            // Get a String representation of the content of the metadata.
            InputStream stream = zipFile.getInputStream(entry);
            
            // Break down the stream into tokens delimited by start of string.
            Scanner scanner = new Scanner(stream, "UTF-8");
            String json = scanner.useDelimiter("\\A").next();
            
            // Create the JSON object with the file content.
            JSONObject jsonObject = new JSONObject(json);
            
            // Get the result needed, and then close all streams.
            result = jsonObject.getString(key);
            stream.close();
            scanner.close();
            zipFile.close();
        }
        catch(ZipException zipException)
        {
            Log.e(LOG_TAG,"Error occured while opening the Zip file.", zipException);
            return null;
        }
        catch(IOException ioException)
        {
            Log.e(LOG_TAG,"Error occured while opening the Zip file.", ioException);
            return null;
        }
        catch(JSONException jsonException)
        {
            Log.e(LOG_TAG, "Error occured while reading the metadata file.", jsonException);
            return null;
        }
        
        return result;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         getCreationTimestamp()
    /// \brief         Get the creation timestamp of the data file from metadata.
    /// \return     long - The creation timestamp.
    /// \author      Ammar Alrashed
    /// \date        07/28/2012
    //////////////////////////////////////////////////////////////////////
    public long getCreationTimestamp()
    {
        return Long.parseLong(getMetadata(CREATION_TIMESTAMP));
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         getOriginUID()
    /// \brief         Get the origin UID of the data file from metadata.
    /// \return     String - The origin UID.
    /// \author      Ammar Alrashed
    /// \date        07/28/2012
    //////////////////////////////////////////////////////////////////////
    public String getOriginUID()
    {
        return getMetadata(ORIGIN_UID);
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         getOriginUID()
    /// \brief         Get the original file name of the data file from metadata.
    /// \return     String - The original file name.
    /// \author      Ammar Alrashed
    /// \date        07/28/2012
    //////////////////////////////////////////////////////////////////////
    public String getOriginalFileName()
    {
        return getMetadata(FILE_NAME);
    }
}