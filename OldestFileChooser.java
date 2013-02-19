package net.g3ti.droidhopper.phoneagent.datafile.storage;

import java.util.List;

//////////////////////////////////////////////////////////////////////
/// \class         OldestFileChooser
/// \brief         File chooser that chooses the oldest file.
/// \author      Ammar Alrashed
/// \date        06/08/2012
//////////////////////////////////////////////////////////////////////
public class OldestFileChooser implements IFileChooser
{

    //////////////////////////////////////////////////////////////////////
    /// \fn         chooseDataFile(List<DataFile> dataFiles)
    /// \brief         Chooses the oldest file according to the file meta data. 
    /// \param[in]    dataFiles - the data files to choose from.
    /// \return        DataFile - The oldest data file.
    /// \author        Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    @Override
    public DataFile chooseDataFile(List<DataFile> dataFiles)
    {
        boolean isEmpty = dataFiles.isEmpty();
        if(isEmpty)
        {
            return null;
        }
        
        // Assume that the first data file is the largest.
        DataFile result = dataFiles.get(0);
        
        // Iterate over the files and get the largest file.
        boolean isOldestSoFar = false;
        for(DataFile df : dataFiles)
        {
            isOldestSoFar = df.getCreationTimestamp() < result.getCreationTimestamp();
            if(isOldestSoFar)
            {
                result = df;
            }
        }
        return result;
    }
}
