//=============================================================================
//
// Updater.cpp
// 
// Created by Max McGuire (max@unknownworlds.com)
// Copyright 2008-2009, Unknown Worlds Entertainment
//
//=============================================================================

#ifndef UPDATER_H
#define UPDATER_H

#ifdef UPDATELIBRARY_EXPORTS
    #define UPDATER_EXPORT  __declspec(dllexport)
#else
    #define UPDATER_EXPORT  __declspec(dllimport)
#endif

/**
 * Updater interface.
 */
class Updater
{

public:

    /// Callback function invoked when the information on the update is ready.
    typedef void (*Callback)(Updater* updater, void* param);

    /**
     * Constructor.
     */
    UPDATER_EXPORT Updater();

    /**
     * Destructor.
     */
    UPDATER_EXPORT ~Updater();

    /**
     * Retrieves update information from the URL and blocks until the data is available.
     */
    UPDATER_EXPORT bool CheckForUpdates(const char* url, unsigned int buildNumber);

    /**
     * Retrieves update information from the URL and invokes the callback function when it's done.
     */
    UPDATER_EXPORT bool CheckForUpdates(const char* url, unsigned int buildNumber, Callback callback, void* param);

    /**
     * Retrieves update information from the URL and displays a dialog box until the data is available.
     */
    UPDATER_EXPORT bool CheckForUpdates(const char* url, unsigned int buildNumber, void* parentWindow);

    /**
     * Returns the number of updates available. This should only be called after CheckForUpdates.
     * If CheckForUpdates is called in asynchronous mode, you must wait until its complete before
     * calling this function.
     */
    UPDATER_EXPORT unsigned int GetNumUpdates() const;

    /**
     * Returns a description of the build that's available. This generally
     * includes change lists and whatever other information is provided.
     */
    UPDATER_EXPORT const char* GetDescription() const;

    /**
     * Presents the user with a dialog asking if they would like to
     * update to the newest version.
     */
    UPDATER_EXPORT bool ShowUpdateNotice(void* parentWindow);

    /**
     * Downloads the latest version to a temporary location. This must be done
     * before the update is installed. The downloading is done asychronously,
     * so this function returns immediately. When the download is complete the
     * callback function is called.
     */
    UPDATER_EXPORT bool DownloadUpdate(Callback callback, void* param);

    /**
     * Downloads the latest version to a temporary location. A diablog box is shown
     * with the progress. This function blocks, but pumps the Windows message loop.
     */
    UPDATER_EXPORT bool DownloadUpdate(void* parentWindow);

    /**
     * Cancels the downloading of an update.
     */
    UPDATER_EXPORT void CancelDownload();

    /**
     * Installs the update. Note the update must be downloaded before
     * it's installed. If a password is supplied, it's used when
     * unzipping the update.
     */
    UPDATER_EXPORT bool InstallUpdate(const char* password = 0);

    /**
     * Returns the total size (in bytes) of the patch file(s) for the update.
     * This function will return 0 until a call to CheckForUpdates successfully
     * completes. If the server incorrectly reports the size of the patch file,
     * this number will be adjusted during download of the patches to the best
     * known size.
     */
    UPDATER_EXPORT size_t GetPatchFileSize() const;

    /**
     * Returns the number of bytes of the patch that have been downloaded. The
     * progress of the update download can be computed as:
     * GetDownloadedSize() / GetPatchFileSize()
     */
    UPDATER_EXPORT size_t GetDownloadedSize() const;

private:

    class UpdaterImpl*    m_impl;

};

#endif