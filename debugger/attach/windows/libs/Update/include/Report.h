//=============================================================================
//
// Report.h
// 
// Created by Max McGuire (max@unknownworlds.com)
// Copyright 2009, Unknown Worlds Entertainment
//
//=============================================================================

#ifndef UPDATER_REPORT_H
#define UPDATER_REPORT_H

#include "Updater.h"

/**
 * Class used to build and submit a report.
 */
class Report
{

public:

    /// Callback function invoked when the a report has been sent.
    typedef void (*Callback)(void* param, const char* response);

    /**
     * Constructor.
     */
    UPDATER_EXPORT Report();

    /**
     * Destructor.
     */
    UPDATER_EXPORT virtual ~Report();

    /**
     * Attaches a file on disk to the report. A name is specified and is passed
     * to the web server. If autoDelete is true, the file will automatically be
     * deleted from disk when the report is destroyed.
     */
    UPDATER_EXPORT bool AttachFile(const char* name, const char* fileName, bool autoDelete = false);

    /**
     * Attaches a mini dump to the report. This can only be used if the application
     * has crashed.
     */
    UPDATER_EXPORT bool AttachMiniDump();

    /**
     * Adds a string field to the report. Fields are submit in the post request.
     */
    UPDATER_EXPORT void SetField(const char* name, const char* value);

    /**
     * Shows details about the report to the user. The user has the ability to
     * remove files from the report as well as add notes (which will be
     * automatically attached as "notes.txt").
     */
    UPDATER_EXPORT bool Preview(void* parentWindow = 0);

    /**
     * Submits the report as as post operation to a web server. Returns false if there was
     * an error submitting the report. This function returns immediately, and when the
     * report is finished being sent, the callback is invoked with the specified parameter.
     * Since the function returns immediately, a return value of true does not indicate
     * that the report was sent successfully.
     */
    UPDATER_EXPORT bool Submit(const char* httpAddress, Callback callback, void* param);

    /**
     * Submits the report as as post operation to a web server. Returns false if there
     * was an error submitting the report. A dialog box is displayed showing the progress
     * of the submit operation. The function returns once the report has been submit or
     * the operation cancelled. The parentWindow may be NULL.
     */
    UPDATER_EXPORT bool Submit(const char* httpAddress, void* parentWindow);

private:

    class ReportImpl*   m_impl;

};

#endif