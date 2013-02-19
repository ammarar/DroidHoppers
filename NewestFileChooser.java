package net.g3ti.droidhopper.phoneagent.datafile.storage;

import java.util.List;

//////////////////////////////////////////////////////////////////////
/// \class         NewestFileChooser
/// \brief         File chooser that chooses the newest file.
/// \author      Ammar Alrashed
/// \date        06/08/2012
//////////////////////////////////////////////////////////////////////
public class NewestFileChooser implements IFileChooser
{

    //////////////////////////////////////////////////////////////////////
    /// \fn         chooseDataFile(List<DataFile> dataFiles)
    /// \brief         Chooses the newest file according to the file meta data. 
    /// \param[in]    dataFiles - the data files to choose from.
    /// \return        DataFile - The newest data file.
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
        boolean isNewestSoFar = false;
        for(DataFile df : dataFiles)
        {
            isNewestSoFar = df.getCreationTimestamp() > result.getCreationTimestamp();
            if(isNewestSoFar)
            {
                result = df;
            }
        }
        return result;
    }

}
