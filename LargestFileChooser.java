package net.g3ti.droidhopper.phoneagent.datafile.storage;

import java.util.List;

//////////////////////////////////////////////////////////////////////
/// \class         LargestFileChooser
/// \brief         File chooser that chooses the largest file.
/// \author      Ammar Alrashed
/// \date        06/08/2012
//////////////////////////////////////////////////////////////////////
public class LargestFileChooser implements IFileChooser
{

    //////////////////////////////////////////////////////////////////////
    /// \fn         chooseDataFile(List<DataFile> dataFiles)
    /// \brief         Chooses the largest file according to the file size. 
    /// \param[in]    dataFiles - the data files to choose from.
    /// \return        DataFile - The largest data file.
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
        boolean isLargestSoFar = false;
        for(DataFile df : dataFiles)
        {
            isLargestSoFar = df.length() > result.length();
            if(isLargestSoFar)
            {
                result = df;
            }
        }
        return result;
    }
}
