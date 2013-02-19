package net.g3ti.droidhopper.phoneagent.datafile.storage;

import java.io.File;

import android.os.Environment;

//////////////////////////////////////////////////////////////////////
/// \class         StorageInformation
/// \brief         Represents the storage information of the device.
/// \author      Ammar Alrashed
/// \date        06/08/2012
//////////////////////////////////////////////////////////////////////
public class StorageInformation
{
    private long totalSpace; ///< The total space in bytes.
    private long freeSpace; ///< The free space in bytes.
    private long incompleteFilesSpace; ///< The incomplete files space.

    //////////////////////////////////////////////////////////////////////
    /// \fn         getDeviceStorageInformation()
    /// \brief         Gets the current device storage information.
    /// \return        StorageInformation - The storage information of the current
    ///             device.
    /// \author        Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    public static StorageInformation getDeviceStorageInformation()
    {
        File sdCard = Environment.getExternalStorageDirectory();
        StorageInformation storageInfo = new StorageInformation();
        storageInfo.setTotalSpace(sdCard.getTotalSpace());
        storageInfo.setFreeSpace(sdCard.getFreeSpace());
        storageInfo.setIncompleteFilesSpace(DataFileRepository.getIncompleteDataFilesSize());
        return storageInfo;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         getTotalSpace()
    /// \brief         Gets the total space instance variable.
    /// \return        long - The total space.
    /// \author        Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    public long getTotalSpace()
    {
        return totalSpace;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         setTotalSpace(long totalSpace)
    /// \brief         Sets the total space instance variable.
    /// \param[in]     totalSpace - The new value for total space.
    /// \author        Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    public void setTotalSpace(long totalSpace)
    {
        this.totalSpace = totalSpace;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         getFreeSpace()
    /// \brief         Gets the free space instance variable.
    /// \return        long - The free space.
    /// \author        Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    public long getFreeSpace()
    {
        return freeSpace;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         setFreeSpace(long freeSpace)
    /// \brief         Sets the free space instance variable.
    /// \param[in]     freeSpace - The new value for free space.
    /// \author        Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    public void setFreeSpace(long freeSpace)
    {
        this.freeSpace = freeSpace;
    }
    
    //////////////////////////////////////////////////////////////////////
    /// \fn         getIncompleteFilesSpace()
    /// \brief         Gets the incomplete file space instance variable.
    /// \return        long - The free space.
    /// \author        Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    public long getIncompleteFilesSpace()
    {
        return incompleteFilesSpace;
    }

    //////////////////////////////////////////////////////////////////////
    /// \fn         setIncompleteFilesSpace()
    /// \brief         Sets the incomplete files space instance variable.
    /// \param[in]     incompleteFilesSpace - The new value for incomplete file space.
    /// \author        Ammar Alrashed
    /// \date       06/08/2012
    //////////////////////////////////////////////////////////////////////
    public void setIncompleteFilesSpace(long incompleteFilesSpace)
    {
        this.incompleteFilesSpace = incompleteFilesSpace;
    }
}
