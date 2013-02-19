using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DroidhoppersLibrary.Util;
using DroidhoppersLibrary.Util.Configuration;
using System.IO;
using System.IO.Compression;
using DroidhoppersLibrary.Datafile.Transfer.Control.Message;
using Newtonsoft.Json;
using Ionic.Zip;
using System.Threading;

namespace DroidhoppersLibrary.Datafile.Storage
{
    /// <summary>
    /// Packages/Unpackages the data files with their meta-data.
    /// </summary>
    /// \author Ammar Alrashed
    /// \date   07/23/2012
    public class DataFilePackager
    {
        #region Constants

        /// <summary>Temporary name of the zip file created.</summary>
        private const string temporaryZipName = "zipped.zip";

        /// <summary>Name of a temporary folder where a file is packaged. Subfodlers are created inside here for each file being packaged.</summary>
        private const string temporaryPackagingFolderName = "data";

        /// <summary>Number of milliseconds to wait for directory with same name created.</summary>
        private const int directoryCollisionWaitingTime = 5 * 1000;

        /// <summary>Number of attempts to create a directory.</summary>
        private const int maxNumberOfAttempts = 3;

        /// <summary>A field that represents the beginning time for the unix timestamp. Used to determine the timestamp.</summary>
        private static readonly DateTime Jan1st1970 = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);

        /// <summary>Tag to be used when logging, indicating the class name.</summary>
        private static readonly string logTag = typeof(DataFile).Name;

        #endregion

        #region Methods
        /// <summary>Packages the data file with its metadata. It also copies the file.</summary>
        /// <param name="dataFilefullPath">The full path of the data file to be packaged.</param>
        /// <returns>FileInfo - The file information of the packaged data file.</returns>
        /// \author  Ammar Alrashed
        /// \date    07/24/2012
        public static FileInfo PackageDataFile(string dataFilefullPath)
        {
            string dataFileName = Path.GetFileName(dataFilefullPath);

            // Create the directory defined above to package the file inside it.
            DirectoryInfo packagingDirectoryInfo = CreatePackagingFolder(dataFileName);

            FileInfo packagedFileInfo = null;
            try
            {
                // Copy the file to be package to the folder where we'll package it.
                string copiedFileFullPath = Path.Combine(packagingDirectoryInfo.FullName, dataFileName);
                File.Copy(dataFilefullPath, copiedFileFullPath);

                // Create the metadata file inside this folder.
                CreateMetadataFile(dataFileName, packagingDirectoryInfo.FullName);

                // Compress the complete directory.
                string packagedFilePath = Path.Combine(packagingDirectoryInfo.FullName, temporaryZipName);
                Logger.LogDebug(logTag, "Packaging files in " + packagedFilePath);
                using (ZipFile zip = new ZipFile())
                {
                    zip.AddDirectory(packagingDirectoryInfo.FullName);
                    zip.ParallelDeflateThreshold = -1;
                    zip.Save(packagedFilePath);
                }

                // We want to calculate the file's hash, and rename it to that same hash to identify it.
                string fileHash = string.Empty;
                using (FileStream openFileStream = File.Open(packagedFilePath, FileMode.Open))
                {
                    fileHash = HashCalculator.CalculateHashOfFile(openFileStream);
                }

                // The final packaged file with the hash as its name will be moved to the base packaging directory.
                string agentRootDirectory = AppDomain.CurrentDomain.BaseDirectory;
                string finalPackagedFilePath = Path.Combine(agentRootDirectory, temporaryPackagingFolderName, fileHash);
                File.Move(packagedFilePath, finalPackagedFilePath);
                packagedFileInfo = new FileInfo(finalPackagedFilePath);
                Logger.LogDebug(logTag, "Packaged file copied and renamed to " + packagedFileInfo.FullName);
            }
            finally
            {
                // Delete the temporary folder we used to put all the files to compress.
                bool recursiveDelete = true;
                Directory.Delete(packagingDirectoryInfo.FullName, recursiveDelete);
            }

            // Return the information of the newly created, packaged file.
            return packagedFileInfo;
        }

        /// <summary>
        /// Creates the folder which will contain all the data about the data file to be packaged.
        /// </summary>
        /// <param name="dataFileName">The name of the data file, which will be the name of the subfolder to create.</param>
        /// <returns>A DirectoryInfo structure with information about the created directory, if it was successful. An exception is thrown otherwise.</returns>
        /// \author  Sebastian Echeverria
        /// \date    10/24/2012
        private static DirectoryInfo CreatePackagingFolder(string dataFileName)
        {
            // Get the full path for the folder where we will be packaging the file.
            string agentRootDirectory = AppDomain.CurrentDomain.BaseDirectory;
            string filePackagingDirectoryName = dataFileName;
            string packagingDirectoryPath = Path.Combine(agentRootDirectory, temporaryPackagingFolderName, filePackagingDirectoryName);

            // This loop is to make sure that if a different thread has a directory with the same name, 
            // then this thread is wait until the other thread finishes.
            bool directoryNotCreated = true;
            DirectoryInfo directoryInfo = null;
            int attemptsToCreateDirectory = 0;
            while (directoryNotCreated)
            {
                bool directoryExists = Directory.Exists(packagingDirectoryPath);
                if (directoryExists)
                {
                    // Check if we have retried too many times; if so, just give up.
                    bool attemptsMaxIsReached = attemptsToCreateDirectory == maxNumberOfAttempts;
                    if (attemptsMaxIsReached)
                    {
                        throw new IOException("Max attempts to create the directory to package a file have been reached (directory: " + packagingDirectoryPath + ")");
                    }

                    // Wait for a while and retry.
                    Thread.Sleep(directoryCollisionWaitingTime);
                    attemptsToCreateDirectory++;
                    continue;
                }

                // Create the folder and get its information.
                directoryInfo = Directory.CreateDirectory(packagingDirectoryPath);
                directoryNotCreated = false;   
            }

            return directoryInfo;
        }

        /// <summary>
        /// Creates a file with metadata about the data file we are packaging.
        /// </summary>
        /// <param name="dataFileName">The data file we are packaging, where most of the metadata will come from.</param>
        /// <param name="packagingFolder">The folder where we will create the metadata file.</param>
        /// \author  Sebastian Echeverria
        /// \date    10/24/2012
        private static void CreateMetadataFile(string dataFileName, string packagingFolder)
        {
            // Get the UID of the device and the current timestamp as we'll need them as metadata.
            string uid = ConfigurationSettings.GetStringSetting(ConfigurationIdentifier.UniqueIdentifier);
            long timestamp = GetCurrentUnixTimestamp();

            // Put together all the metadata we need.
            DataFileMetadata metadata = new DataFileMetadata();
            metadata.FileName = dataFileName;
            metadata.CreationTimestamp = timestamp;
            metadata.OriginUID = uid;

            // The actual metadata file will have the same name as the data file, but with a JSON extension.
            string metadataFileName = Path.Combine(packagingFolder, dataFileName + ".json");

            // Create the metadata file by serializing through Json the metadata information.
            using (StreamWriter filestream = File.CreateText(metadataFileName))
            {
                JsonSerializerSettings settings = new JsonSerializerSettings();
                JsonSerializer serializer = JsonSerializer.Create(settings);
                serializer.Serialize(filestream, metadata);
            }
        }

        /// <summary>Unpackages the data file and deletes its metadata attached.</summary>
        /// <param name="fullPath">The full path of the data file to be packaged.</param>
        /// <returns>FileInfo - The file information of the unpackaged file.</returns>
        /// \author  Ammar Alrashed
        /// \date    07/24/2012
        public static FileInfo UnpackageDataFile(string fullPath)
        {
            // Cache the current directory in a field to prevent moments in which the "current directory" is not actually the app's folder
            // (something that for unknown reasons actually happens).
            string agentRootDirectory = AppDomain.CurrentDomain.BaseDirectory;
            string currentDirctory = Path.Combine(agentRootDirectory, temporaryPackagingFolderName);
            FileInfo dataFileInfo = null;

            // Extract Metadata
            using (ZipFile zip = ZipFile.Read(fullPath))
            {
                foreach (ZipEntry entry in zip)
                {
                    // If it is the metadata file, then extract it to the metadata folder. Else extract it to the current directory.
                    if (entry.FileName.EndsWith(".json"))
                    {
                        // If the file exists, then ignore this file. Ignoring the new file because it is assumed that already existing file is the same as this file.
                        // Also, the metadata files does not contain any significant information that is needed when the file reaches the destination.
                        entry.Extract(ConfigurationSettings.GetStringSetting(ConfigurationIdentifier.MetadataFolder), ExtractExistingFileAction.DoNotOverwrite);
                    }
                    else
                    {
                        // A file can fail when extracted if a file with the same name is extracted simultaneously. This is because, it will have the same directory used for extraction.
                        bool extractSuccessful = false;
                        int attemptsToExtractFile = 0;

                        // If the extract is successful, then the process will proceed. However, if the extraction fails, then wait a specified amount of time.
                        // then try again afterwards. These trials should not exceed the maximum number of trials.
                        while (!extractSuccessful)
                        {
                            try
                            {
                                entry.Extract(currentDirctory, ExtractExistingFileAction.Throw);
                                extractSuccessful = true;
                            }
                            catch (ZipException)
                            {
                                Logger.LogError(logTag, "A file with the same name exists. Waiting for it to finish processing.");
                                bool attemptsMaxIsReached = attemptsToExtractFile == maxNumberOfAttempts;
                                if (attemptsMaxIsReached)
                                {
                                    throw new IOException("Attempts to extract file is reached its maximum.");
                                }

                                attemptsToExtractFile++;
                                Thread.Sleep(directoryCollisionWaitingTime);
                            }
                        }

                        dataFileInfo = new FileInfo(Path.Combine(currentDirctory, entry.FileName));
                    }
                }
            }

            return dataFileInfo;
        }

        /// <summary>Gets the Unix timestamp.</summary>
        /// <returns>long - The current unix timestamp.</returns>
        /// \author  Ammar Alrashed
        /// \date    07/30/2012
        private static long GetCurrentUnixTimestamp()
        {
            return (long)((DateTime.UtcNow - Jan1st1970).TotalMilliseconds);
        }
        #endregion
    }
}
