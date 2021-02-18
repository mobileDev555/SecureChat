package com.realapps.chat.interfaces;

import com.realapps.chat.model.ContactEntity;

/**
 * Created by Prashant Sharma on 3/16/2018.
 * Core techies
 * prashant@coretechies.org
 */

public interface AddContactDialogResponse {
    void onAddContact(ContactEntity entity);

    void onClose();

}
