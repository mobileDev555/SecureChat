package com.realapps.chat.interfaces;

/**
 * Created by Prashant Sharma on 3/16/2018.
 * Core techies
 * prashant@coretechies.org
 */

public interface AddVaultItemResponse {
    void onImageAddResponse(String path);

    void onAddPersonalNote(String name, String Path);

    void onChanged();
}
