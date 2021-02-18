/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.realapps.chat.ui.pjsip.recorder.impl;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import org.pjsip.pjsua.pj_str_t;
import org.pjsip.pjsua.pjsua;

import com.realapps.chat.ui.api.SipCallSession;
import com.realapps.chat.ui.api.SipManager;

import com.realapps.chat.ui.pjsip.recorder.IRecorderHandler;
import com.realapps.chat.ui.service.SipService.SameThreadException;
import com.realapps.chat.ui.ui.view.java.model.Settings;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class SimpleWavRecorderHandler implements IRecorderHandler {
    final int way;
    final SipCallSession callInfo;
    private final int recorderId;
    private final String recordingPath;
    String username;
    
    public SimpleWavRecorderHandler(Context ctx,SipCallSession callInfo, File recordFolder, int way)
            throws SameThreadException, IOException {
        this.way = way;
        this.callInfo = callInfo;

        //File targetFolder = getDateWiseFolder(recordFolder);
        File targetFile = getRecordFile(ctx,recordFolder, callInfo.getRemoteContact(), way);
        if (targetFile == null) {
            throw new IOException("No target file possible");
        }
        recordingPath = targetFile.getAbsolutePath();
        pj_str_t file = pjsua.pj_str_copy(recordingPath);
        int[] rcId = new int[1];
        int status = pjsua.recorder_create(file, 0, (byte[]) null, 0, 0, rcId);
        if (status == pjsua.PJ_SUCCESS) {
            recorderId = rcId[0];
        } else {
            throw new IOException("Pjsip not able to write the file");
        }
    }
    
    /**
     * Get the file to record to for a given remote contact. This will
     * implicitly get the current date in file name.
     * 
     * @param remoteContact The remote contact name
     * @return The file to store conversation
     */
    private File getRecordFile(Context ctx,File dir, String remoteContact, int way) {
        if (dir != null) {
            // The file name is only to have an unique identifier.
            // It should never be used to store datas as may change.
            // The app using the recording files should rely on the broadcast
            // and on callInfo instead that is reliable.

        	username = PreferenceManager.getDefaultSharedPreferences(ctx)
					.getString(Settings.PREFSIP_USERNAME + "",
							Settings.DEFAULTSIP_USERNAME);

            String datePart = (String) DateFormat.format("yyyyMMdd_kkmmss", new Date());
            
            String remotePart = sanitizeForFile(remoteContact);

            //String fileName = hourPart + "_" + username + "_" + remotePart;
            String fileName = datePart+ "_" + remotePart ;
            if (way != (SipManager.BITMASK_ALL)) {
                fileName += ((way & SipManager.BITMASK_IN) == 0) ? "_out" : "_in";
            }
            
            File file = new File(dir.getAbsoluteFile() + File.separator
                    + fileName + ".wav");
            return file;
        }
        return null;
    }
    
    /**Add by himadri
     * Get the date wise folder to record file . This will
     * implicitly get the current date in file name.
     * 
     * @return The folder to store file
     */
	private File getDateWiseFolder(File dir) {
		if (dir != null) {
			String datePart = (String) DateFormat.format("dd-MM-yyyy",new Date());

			File dateDir = new File(dir.getAbsoluteFile() + File.separator+ datePart);
			if (!dateDir.exists()) {
				dateDir.mkdirs();
			}
			
			return dateDir;
		}
		
		return null;
	}

    private String sanitizeForFile(String remoteContact) {
        String fileName = remoteContact;
        String[] firstPart = null;
        String[] secondPart = null;
        //fileName = fileName.replaceAll("[\\.\\\\<>:; \"\'\\*]", "_");
        if(fileName.contains("wifi%23")){
            firstPart = fileName.split("@");
            String[] firstStr = firstPart[0].split(":");
            if(firstStr[1].contains("wifi%23")){
                secondPart = firstStr[1].split("wifi%23");
            }else{
                secondPart = firstStr;
            }

        }else {
            firstPart = fileName.split("@");
            secondPart = firstPart[0].split(":");
        }
        fileName = secondPart[1];
        return fileName;
    }

    @Override
    public void startRecording() {
        // TODO : treat connect errors ? is it useful? Should we fail gracefully
        int wavPort = pjsua.recorder_get_conf_port(recorderId);
        if ((way & SipManager.BITMASK_IN) == SipManager.BITMASK_IN) {
            int wavConfPort = callInfo.getConfPort();
            pjsua.conf_connect(wavConfPort, wavPort);
        }
        if ((way & SipManager.BITMASK_OUT) == SipManager.BITMASK_OUT) {
            pjsua.conf_connect(0, wavPort);
        }
    }

    @Override
    public void stopRecording() {
        pjsua.recorder_destroy(recorderId);
    }

    @Override
    public void fillBroadcastWithInfo(Intent it) {
        it.putExtra(SipManager.EXTRA_FILE_PATH, recordingPath);
        it.putExtra(SipManager.EXTRA_SIP_CALL_CALL_WAY, way);
    }

}
