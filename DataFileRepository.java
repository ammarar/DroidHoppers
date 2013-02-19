package net.g3ti.droidhopper.phoneagent.datafile.storage;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.g3ti.droidhopper.phoneagent.util.FileManager;
import net.g3ti.droidhopper.phoneagent.util.InvalidConfigurationException;
import net.g3ti.droidhopper.phoneagent.util.RemovableStorage;

import android.content.Context;
import android.util.Log;

//////////////////////////////////////////////////////////////////////
/// \class       DataFileRepository
/// \brief       Represents the collection of data files and all the
///              functions related to them.
/// \author      Ammar Alrashed
/// \date        06/08/2012
//////////////////////////////////////////////////////////////////////
public class DataFileRepository
{
    private static final String DATA_FILE_DIRECTORY = "data";  ///< The data file directory name.
    private static final String LOG_TAG = DataFileRepository.class.getSimpleName(); ///< Tag for logging.
    private static final long BUFFER_SPACE = 100L*1024L*1024L; ///< Represents the buffer space of 100 MB.    
    
    private static Context context; ///< The app's running context.
    
    //////////////////////////////////////////////////////////////////
    /// \fn         setContext(Context context)
    /// \brief      Sets the app's context.
    /// \param[in]  context - the app's current running context. 
    /// \author     Sebastian Echeverria
    /// \date       07/05/2012
    //////////////////////////////////////////////////////////////////
    public static void setContext(Context context)
    {
        DataFileRepository.context = context;
        FileManager.setContext(context);
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         hasFilesToSend()
    /// \brief         Checks if there are complete files in the data files directory.
    /// \return        boolean - TRUE if there is at least one file, FALSE otherwise.
    /// \author        Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    public static boolean hasFilesToSend()
    {
        Log.d(LOG_TAG, "Checking if there are files to send from folder " + DataFileRepository.getDataFileDirectory());
        List<DataFile> dataFiles = DataFileRepository.getDataFiles();
        boolean hasFiles = false;
        
        // Iterate over the data file names.
        for(DataFile dataFile : dataFiles)
        {
            boolean isFileComplete = dataFile.isComplete();
            if(isFileComplete)
            {
                Log.i(LOG_TAG, "There is a file to send: " + dataFile);
                hasFiles = true;
                break;
            }// end if
        }// end for
        return hasFiles;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         deleteIncompleteFilesForSpace(long fileSize)
    /// \brief      Tries to delete incomplete files to make space for a new
    ///             incoming file.
    /// \param[in]  fileId - the id of the file to receive.    
    /// \param[in]  fileSize - the size of the file to receive in bytes.
    /// \return     boolean - TRUE if it deleted files successfully.
    ///             FALSE if it either could not delete the files or there
    ///             is not enough space.
    /// \author     Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    public static boolean deleteIncompleteFilesForSpace(String fileId, long fileSize) throws IOException
    {
        // Sort the data file from the least modified to the most modified files.
        List<DataFile> incompleteDataFiles = DataFileRepository.getIncompleteDataFiles();
        Collections.sort(incompleteDataFiles, new LastModifiedComparator());
        
        // Delete incomplete files in order until there is space available.
        Log.d(LOG_TAG, "Deleting incomplete files to make space.");
        for(DataFile incompleteDataFile : incompleteDataFiles)
        {
            // Check if the file we are going to delete is the same file we are trying to receive.
            String fileName = incompleteDataFile.getName();            
            boolean isSameAsFileToBeReceived = fileName.startsWith(fileId);
            if(isSameAsFileToBeReceived)
            {
                // Ignore this one, as we don't want to delete the partial file we are about to continue receiving.
                continue;
            }
            
            // Try to delete the first incomplete file.
            boolean successfullyDeleted = incompleteDataFile.delete();
            if(!successfullyDeleted)
            {
                // The file could not be deleted, then throw an exception.
                String errorMessage = "File: " + incompleteDataFile.getName() + " could not be deleted.";
                Log.w(LOG_TAG, errorMessage);
                throw new IOException(errorMessage);
            }
            
            // Check if we have enough space now.
            Log.d(LOG_TAG, "Deleted file " + incompleteDataFile.getName());            
            boolean enoughSpaceAvailable = hasEnoughSpaceAvailable(fileSize);
            if(enoughSpaceAvailable)
            {
                // If the space is successfully created, then the operation is successful.
                Log.d(LOG_TAG, "We have enough space now.");
                return true;
            }
        }
        
     // If after deleting all incomplete files we still don't have enough space, return false.
        return false;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         selectNextFileForTransfer(long maxFileSize)
    /// \brief         Responsible for selecting the file to be transfered.
    /// \param[in]    maxFileSize - The maximum file size that the files should 
    ///             not exceed. This is measured in bytes.
    /// \return        DataFile - The data file that best meets the criteria, NULL
    ///             if there is no file that meets the criteria.
    /// \author        Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    public static DataFile selectNextFileForTransfer(long maxFileSize) throws InvalidConfigurationException
    {
        DataFile selectedFile = null;
        
        List<DataFile> dataFiles = DataFileRepository.getDataFiles();
        Log.i(LOG_TAG, "Number of files found: " + dataFiles.size());
        
        // If there are files then continue, otherwise return null.
        boolean mayContainCandidateFile = dataFiles.size() != 0;
        if(!mayContainCandidateFile)
        {
            return null;
        }
        
        // Construct a list to hold completed and small data files.
        List<DataFile> candidateDataFiles = getCompleteDataFilesLessThan(maxFileSize);
        
        // If there are no files, return NULL.
        Log.i(LOG_TAG, "Number of candidate files: " + candidateDataFiles.size());
        boolean hasCandidateFiles = candidateDataFiles.size() != 0;
        if(!hasCandidateFiles)
        {
            return null;
        }
        
        // Choose a file according to the configured behavior.
        IFileChooser fileChooser = FileChooserFactory.createFileChooser(context);
        Log.d(LOG_TAG, "File chooser: " + fileChooser.getClass().getSimpleName());
        selectedFile = fileChooser.chooseDataFile(candidateDataFiles);
        boolean selectedFileNotNull = selectedFile != null;
        if(selectedFileNotNull)
        {
            Log.i(LOG_TAG, "Chosen file: " + selectedFile.getName());
        }
        return selectedFile;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         getDataFileDirectory()
    /// \brief      Gives the data file directory.
    /// \return     File - The directory in an object representation,
    ///             or null if the directory is not available.
    /// \author     Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    public static File getDataFileDirectory()
    {
        return FileManager.getFileDirectory(DATA_FILE_DIRECTORY);
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         getDataFiles()
    /// \brief         Gets all the data files.
    /// \return        List<DataFile> - The list of files, an empty list if 
    ///             there are no files or we don't have access to them.
    /// \author        Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    public static List<DataFile> getDataFiles()
    {
        List<DataFile> dataFiles = new LinkedList<DataFile>();
        
        // Get the folder where the files are stored.
        File dataFileDirectory = DataFileRepository.getDataFileDirectory();
        
        // Only get list of files if we actually got a directory.
        boolean isDirectoryAvailable = (dataFileDirectory != null);
        if(!isDirectoryAvailable)
        {
            // Return an empty list, as we don't have access to the folder.
            return dataFiles;
        }        
        
        // Iterate over all the file names and create a DataFile object to store them.
        String[] fileNames = dataFileDirectory.list();
        boolean atLeastOneFile = (fileNames !=null &&  fileNames.length > 0);
        if(atLeastOneFile)
        {
            for(String fileName: fileNames)
            {
                dataFiles.add(new DataFile(dataFileDirectory, fileName));
            }
        }
        
        return dataFiles;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         getDataFilesSize()
    /// \brief         Gets total size of the all data files.
    /// \return        long - The total size of all ata files. 
    /// \author        Ammar Alrashed
    /// \date       06/21/2012
    //////////////////////////////////////////////////////////////////////
    public static long getDataFilesSize()
    {
        List<DataFile> dataFiles = getDataFiles();
        long dataFilesSize = 0;
        for(DataFile dataFile : dataFiles)
        {
            dataFilesSize += dataFile.length();
        }
        return dataFilesSize;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         getIncompleteDataFiles()
    /// \brief         Gets all the incomplete data files.
    /// \return        List<DataFile> - The list of incomplete data files. 
    /// \author        Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    static List<DataFile> getIncompleteDataFiles()
    {
        List<DataFile> dataFiles = DataFileRepository.getDataFiles();
        List<DataFile> incompleteDataFiles = new LinkedList<DataFile>();
        
        // Filter out the files that are complete and leave the rest.
        for(DataFile dataFile : dataFiles)
        {
            boolean incompleteDataFile = !dataFile.isComplete();
            if(incompleteDataFile)
            {
                incompleteDataFiles.add(dataFile);
            }
        }
        return incompleteDataFiles;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         getCompleteDataFiles()
    /// \brief         Gets all the complete data files.
    /// \return        List<DataFile> - The list of complete data files. 
    /// \author        Ammar Alrashed
    /// \date       07/28/2012
    //////////////////////////////////////////////////////////////////////
    public static List<DataFile> getCompleteDataFiles()
    {
        List<DataFile> dataFiles = DataFileRepository.getDataFiles();
        List<DataFile> completeDataFiles = new LinkedList<DataFile>();
        
        // Filter out the files that are complete and leave the rest.
        for(DataFile dataFile : dataFiles)
        {
            boolean completeDataFile = dataFile.isComplete();
            if(completeDataFile)
            {
                completeDataFiles.add(dataFile);
            }
        }
        return completeDataFiles;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         getCompleteDataFilesLessThan(long maxFileSize)
    /// \brief         Gets data files that are complete and bounded by the input 
    ///             max file size.
    /// \param[in]    maxFileSize - The maximum file size that the files should 
    ///             not exceed. This is measured in bytes.
    /// \return        List<DataFile> - A list of complete and with sizes less 
    ///             than max file size.
    /// \author        Ammar Alrashed
    /// \date       06/21/2012
    //////////////////////////////////////////////////////////////////////
    public static List<DataFile> getCompleteDataFilesLessThan(long maxFileSize)
    {
        List<DataFile> dataFiles = new LinkedList<DataFile>();
        
        boolean isCandidateFile = false;
        boolean fileSmallerThanFreeSpace = false;
        boolean fileIsComplete = false;
        
        // Filter out files larger than max size, and incomplete files.
        for(DataFile dataFile : getDataFiles())
        {
            Log.d(LOG_TAG, "File name: " + dataFile.getName());
            fileIsComplete = dataFile.isComplete();
            Log.d(LOG_TAG, "File complete: " + fileIsComplete);
            fileSmallerThanFreeSpace = dataFile.length() < maxFileSize;
            Log.d(LOG_TAG, "File length: " + dataFile.length());
            Log.d(LOG_TAG, "File smaller than free space: " + fileSmallerThanFreeSpace);
            
            isCandidateFile = fileSmallerThanFreeSpace && fileIsComplete;
            Log.d(LOG_TAG, "File is a candidate: " + isCandidateFile);
            if(isCandidateFile)
            {
                dataFiles.add(dataFile);
            }
        }
        return dataFiles;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         getIncompleteDataFilesSize()
    /// \brief         Gets total size of the incomplete data files.
    /// \return        long - The total size of all incomplete data files. 
    /// \author        Ammar Alrashed
    /// \date       06/21/2012
    //////////////////////////////////////////////////////////////////////
    public static long getIncompleteDataFilesSize()
    {
        List<DataFile> incompleteDataFiles = getIncompleteDataFiles();
        long incompleteDataFilesSize = 0;
        for(DataFile dataFile : incompleteDataFiles)
        {
            incompleteDataFilesSize += dataFile.length();
        }
        return incompleteDataFilesSize;
    }

    //////////////////////////////////////////////////////////////////
    /// \fn         isAbleToReceiveFiles()     
    /// \brief      Checks if the repository can store new files.   
    /// \return     True if it is, false if not.
    /// \author     Sebastian Echeverria
    /// \date       07/02/2012
    //////////////////////////////////////////////////////////////////    
    public static boolean isAbleToReceiveFiles()
    {
        // If we have the flag to allow non removable storage, this method can just return true, as that was the only thing we were checking.
        boolean allowNonRemovableStorage = FileManager.isNonRemovableStorageAllowed();
        if(allowNonRemovableStorage)
        {
            return true;
        }
        
        // Only check if we have access to the removable storage.
        boolean isRemovableStorageAvailable = RemovableStorage.isRemovableStorageAvailable();
        if(!isRemovableStorageAvailable)
        {
            Log.w(LOG_TAG, "Removable storage is not currently available.");   
        }
        
        return isRemovableStorageAvailable;
    }
    
    //////////////////////////////////////////////////////////////////
    /// \fn         hasEnoughSpaceAvailable(long targetSize)
    /// \brief      Checks if there is a certain amount of space available 
    ///             to store files.
    /// \param[in]  targetSize - we want to check if we have this amount 
    ///             of space available, at least.
    /// \return     True if there is more (or equal) than "targetSize" space 
    ///             available, false if there is less.
    /// \author     Sebastian Echeverria
    /// \date       07/09/2012
    //////////////////////////////////////////////////////////////////
    public static boolean hasEnoughSpaceAvailable(long targetSize)
    {
        // Read the storage statistics of the device.
        StorageInformation storageInformation = StorageInformation.getDeviceStorageInformation();
        long freeSpaceLeftInBytes = storageInformation.getFreeSpace();
        
        Log.d(LOG_TAG,"FREE SPACE: " + freeSpaceLeftInBytes);
        Log.d(LOG_TAG,"BUFFER_SPACE: " + BUFFER_SPACE);
        Log.d(LOG_TAG,"TARGET SIZE: " + targetSize);        
        
        // Compare the storage statistics to check if the file can be accommodated.
        boolean enoughFreeSpace = (freeSpaceLeftInBytes - BUFFER_SPACE >= targetSize);
        return enoughFreeSpace;
    }      
}
