package net.g3ti.droidhopper.phoneagent.datafile.storage;

import java.util.List;

//////////////////////////////////////////////////////////////////////
/// \class         IFileChooser
/// \brief         Represents the file chooser interface where all file
///             choosers should extend.
/// \author      Ammar Alrashed
/// \date        06/08/2012
//////////////////////////////////////////////////////////////////////
public interface IFileChooser
{
    //////////////////////////////////////////////////////////////////////
    /// \fn         chooseDataFile((List<DataFile> dataFiles))
    /// \brief         Choose a file between the list of file according to the
    ///             implementer criteria.
    /// \param[in]     dataFiles - The data files to choose from.
    /// \return        DataFile - The data file chosen.
    /// \author      Ammar Alrashed
    /// \date        06/08/2012
    //////////////////////////////////////////////////////////////////////
    DataFile chooseDataFile(List<DataFile> dataFiles);
}
