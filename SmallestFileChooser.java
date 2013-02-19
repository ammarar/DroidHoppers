package net.g3ti.droidhopper.phoneagent.datafile.storage;

import java.util.List;

//////////////////////////////////////////////////////////////////////
/// \class         SmallestFileChooser
/// \brief         File chooser that chooses the smallest file.
/// \author      Ammar Alrashed
/// \date        06/08/2012
//////////////////////////////////////////////////////////////////////
public class SmallestFileChooser implements IFileChooser
{

    //////////////////////////////////////////////////////////////////////
    /// \fn         chooseDataFile(List<DataFile> dataFiles)
    /// \brief         Chooses the smallest file according to the file size. 
    /// \param[in]    dataFiles - the data files to choose from.
    /// \return        DataFile - The smallest data file.
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
        
        // Assume that the first data file is the smallest.
        DataFile result = dataFiles.get(0);
        
        // Iterate over the files and get the smallest file.
        boolean isSmallestSoFar = false;
        for(DataFile df : dataFiles)
        {
            isSmallestSoFar = df.length() < result.length();
            if(isSmallestSoFar)
            {
                result = df;
            }
        }
        return result;
    }

}
