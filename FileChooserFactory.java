package net.g3ti.droidhopper.phoneagent.datafile.storage;

import android.content.Context;
import android.util.Log;
import net.g3ti.droidhopper.phoneagent.datafile.configuration.UploadPriorityType;
import net.g3ti.droidhopper.phoneagent.util.ConfigurationIdentifier;
import net.g3ti.droidhopper.phoneagent.util.ConfigurationSettings;
import net.g3ti.droidhopper.phoneagent.util.InvalidConfigurationException;

//////////////////////////////////////////////////////////////////////
/// \class         FileChooserFactory
/// \brief         Factory class for specifying which file chooser to choose.
/// \author      Ammar Alrashed
/// \date        06/08/2012
//////////////////////////////////////////////////////////////////////
public class FileChooserFactory
{
    private static final String LOG_TAG = FileChooserFactory.class.getSimpleName(); ///< Tag for logging.
    private static final UploadPriorityType DEFAULT_UPLOAD_PRIORITY_TYPE = UploadPriorityType.SMALLEST_FIRST; ///< Default upload priority type if the configuration setting is null.
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         createFileChooser(Context context)
    /// \brief         Creates the file chooser according to configuration.
    /// \param[in]     context - The context of the application.
    /// \return        boolean - The file chooser defined in the configuration.
    /// \author      Ammar Alrashed
    /// \date        06/08/2012
    //////////////////////////////////////////////////////////////////////
    public static IFileChooser createFileChooser(Context context) throws InvalidConfigurationException
    {
        // GET THE CONFIGURATION SETTING FOR UPLOAD PRIORITY.
        // Create the configuration setting object.
        ConfigurationSettings settings = new ConfigurationSettings(context);
        String uploadPrioritySetting = null;
        try
        {
            // Get the configuration setting.
            uploadPrioritySetting = settings.getStringSetting(ConfigurationIdentifier.UPLOAD_PRIORITY);
            Log.d(LOG_TAG,"Given upload priority setting is " + uploadPrioritySetting);
        }
        catch (InvalidConfigurationException e)
        {
            Log.e(LOG_TAG, "Error while getting the cofiguration for upload priority", e);
            throw e;
        }
        
        // Get the upload priority enum.
        UploadPriorityType uploadPriority = null;
        try
        {
            // If the current configuration setting for upload priority is null, 
            // set the default type.
            if (uploadPrioritySetting == null)
            {
                settings.setStringSetting(ConfigurationIdentifier.UPLOAD_PRIORITY, DEFAULT_UPLOAD_PRIORITY_TYPE.toString());
                uploadPriority = UploadPriorityType.valueOf(DEFAULT_UPLOAD_PRIORITY_TYPE.toString());               
            }
            else
            {
                uploadPriority = UploadPriorityType.valueOf(uploadPrioritySetting);
            }
        }
        catch(IllegalArgumentException exception)
        {
            Log.d(LOG_TAG,"The given Upload Priority value is unexpected", exception);
            throw new InvalidConfigurationException("Invalid configuration value", exception);
        }
      
        Log.d(LOG_TAG,"Given upload priority is " + uploadPriority);
        
        IFileChooser chooser = null;
        
        // Choose one of the file choosers.
        switch(uploadPriority)
        {
            case LARGEST_FIRST:
                chooser = new LargestFileChooser();
                break;
            case NEWEST_FIRST:
                chooser = new NewestFileChooser();
                break;
            case OLDEST_FIRST:
                chooser = new OldestFileChooser();
                break;
            case SMALLEST_FIRST:
                chooser = new SmallestFileChooser();
                break;
            default:
                chooser = null;
                Log.e(LOG_TAG, "Choosing upload priority failed, the given upload priority is " + uploadPrioritySetting);
                throw new InvalidConfigurationException("Unexpected upload priority value");
        }
        return chooser;
    }    
}
