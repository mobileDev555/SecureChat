package com.realapps.chat.interfaces;

/**
 * Created by Prashant Sharma on 3/16/2018.
 * Core techies
 * prashant@coretechies.org
 */

public interface AttachmentDialogResponse {
    void onImageSelect();

    void onContactSelect();

    void onPersonalNoteSelect();

    void onCameraResponse();

    void sharemsgfromvault();

    void onClose();

}
