package com.realapps.chat.view.home.fragment;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.interfaces.AddVaultItemResponse;
import com.realapps.chat.interfaces.ScreenNameChangeDialogResponse;
import com.realapps.chat.interfaces.VaultItemNameChangeDialogResponse;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.model.VaultEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.DateTimeUtils;
import com.realapps.chat.utils.DbConstants;
import com.realapps.chat.utils.KeyboardUtils;
import com.realapps.chat.view.custom.MyDividerItemDecoration;
import com.realapps.chat.view.dialoges.DialogChangeScreenName;
import com.realapps.chat.view.dialoges.DialogChangeVaultItemName;
import com.realapps.chat.view.home.activity.ForwardMessageActivity;
import com.realapps.chat.view.home.activity.HomeActivity;
import com.realapps.chat.view.home.activity.PersonalActivity;
import com.realapps.chat.view.home.activity.PhotoViewActivity;
import com.realapps.chat.view.home.activity.VaultMessageWindowActivity;
import com.realapps.chat.view.home.adapters.VaultAdapter;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Prashant Sharma on 3/15/2018.
 * Core techies
 * prashant@coretechies.org
 */

public class FragmentVault extends Fragment implements VaultAdapter.onItemClickListener, AddVaultItemResponse {

    private static final String TAG = FragmentVault.class.getSimpleName();
    public static AddVaultItemResponse addVaultItemResponse;
    @BindView(R.id.edt_password)
    EditText edtPassword;
    @BindView(R.id.btn_save)
    Button btnSave;
    @BindView(R.id.txt_big_title)
    TextView txtBigTitle;
    @BindView(R.id.txt_user_name)
    TextView txtUserName;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.recycler_vault)
    RecyclerView mRecyclerView;
    @BindView(R.id.lyr_password)
    RelativeLayout lyrPassword;
    Unbinder unbinder;
    VaultAdapter mAdapter;
    DbHelper db;
    @BindView(R.id.fab)
    ImageView fab;
    @BindView(R.id.lyr_main)
    LinearLayout lyrMain;
    @BindView(R.id.txt_wrong)
    TextView txtWrong;
    private Context mContext;
    private Activity mActivity;
    private ArrayList<VaultEntity> vaultList;
    int tmp_attempt = 0;
    private ProgressDialog mProgressDialoge;
    private Bitmap.Config mConfig = Bitmap.Config.ARGB_8888;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vault, container, false);
        mContext = getContext();
        mActivity = getActivity();
        unbinder = ButterKnife.bind(this, view);
        //edtPassword.requestFocusFromTouch(); open keyboard automaticlly

        //InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        addVaultItemResponse = this;
        db = new DbHelper(mContext);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTabLayout(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        setTabLayoutDefault();
        initViews();
        setNotesAdapter();

        return view;
    }

    private void setName() {
        int lastIndex;
        char lastIntial;
        //SetName
        if (User_settings.getScreenName(mContext).trim().contains(" ")) {
            lastIndex = User_settings.getScreenName(mContext).indexOf(" ");
            lastIntial = User_settings.getScreenName(mContext).charAt(lastIndex + 1);
            txtBigTitle.setText((User_settings.getScreenName(mContext).toUpperCase()).substring(0, 1) + (User_settings.getScreenName(mContext).toUpperCase()).substring(lastIndex + 1, lastIndex + 2));
        } else
            txtBigTitle.setText((User_settings.getScreenName(mContext).toUpperCase()).substring(0, 1));

        txtUserName.setText(User_settings.getScreenName(mContext));

    }

    private void initViews() {
        setName();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(mContext, DividerItemDecoration.VERTICAL, 0));
    }



    private void setNotesAdapter() {
        if (vaultList != null)
            vaultList.clear();
        vaultList = db.getVaultEntityList(AppConstants.ITEM_TYPE_NOTES);
        mAdapter = new VaultAdapter(mContext, vaultList, this, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        ((TextView) tabLayout.getTabAt(0).getCustomView().findViewById(R.id.txt_tab_detail)).setText(String.format("%d%s", vaultList.size(), getString(R.string.items)));


    }

    private void setPictureAdapter() {

        if (vaultList != null)
            vaultList.clear();
        vaultList = db.getVaultEntityList(AppConstants.ITEM_TYPE_PICTURE);

        mAdapter = new VaultAdapter(mContext, vaultList, this, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        ((TextView) tabLayout.getTabAt(1).getCustomView().findViewById(R.id.txt_tab_detail)).setText(vaultList.size() + getString(R.string.items));

    }

    private void setChatAdapter() {

        if (vaultList != null)
            vaultList.clear();
        vaultList = db.getVaultEntityList(AppConstants.ITEM_TYPE_CHATS);

        mAdapter = new VaultAdapter(mContext, vaultList, this, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        ((TextView) tabLayout.getTabAt(2).getCustomView().findViewById(R.id.txt_tab_detail)).setText(vaultList.size() + getString(R.string.items));
    }


    public void setTabLayoutDefault() {
        tabLayout.getTabAt(0).setCustomView(R.layout.tab_design);
        tabLayout.getTabAt(1).setCustomView(R.layout.tab_design);
        tabLayout.getTabAt(2).setCustomView(R.layout.tab_design);

        TextView txt_tab_title0 = tabLayout.getTabAt(0).getCustomView().findViewById(R.id.txt_tab_title);
        TextView txt_tab_title1 = tabLayout.getTabAt(1).getCustomView().findViewById(R.id.txt_tab_title);
        TextView txt_tab_title2 = tabLayout.getTabAt(2).getCustomView().findViewById(R.id.txt_tab_title);

        TextView txt_tab_detail0 = tabLayout.getTabAt(0).getCustomView().findViewById(R.id.txt_tab_detail);
        TextView txt_tab_detail1 = tabLayout.getTabAt(1).getCustomView().findViewById(R.id.txt_tab_detail);
        TextView txt_tab_detail2 = tabLayout.getTabAt(2).getCustomView().findViewById(R.id.txt_tab_detail);


        txt_tab_title0.setText(getString(R.string.notes));
        txt_tab_title1.setText(getString(R.string.pictures));
        txt_tab_title2.setText(getString(R.string.chats));

        txt_tab_detail0.setText(String.format("%d%s", db.getTotalVaultItem(AppConstants.ITEM_TYPE_NOTES), getString(R.string.items)));
        txt_tab_detail1.setText(String.format("%d%s", db.getTotalVaultItem(AppConstants.ITEM_TYPE_PICTURE), getString(R.string.items)));
        txt_tab_detail2.setText(String.format("%d%s", db.getTotalVaultItem(AppConstants.ITEM_TYPE_CHATS), getString(R.string.items)));


        txt_tab_title0.setBackgroundColor(getResources().getColor(R.color.tab_background_selected2));
        txt_tab_detail0.setBackgroundColor(getResources().getColor(R.color.tab_background_selected2));


        txt_tab_title1.setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
        txt_tab_detail1.setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
        tabLayout.getTabAt(1).getCustomView().findViewById(R.id.img_arrow).setVisibility(View.GONE);


        txt_tab_title2.setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
        txt_tab_detail2.setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
        tabLayout.getTabAt(2).getCustomView().findViewById(R.id.img_arrow).setVisibility(View.GONE);


    }


    public void setTabLayout(int tabPosition) {

        VaultAdapter.openLayoutPosition = -1;

        if (tabPosition == 0) {

            fab.setVisibility(View.VISIBLE);

            tabLayout.getTabAt(0).getCustomView().findViewById(R.id.txt_tab_title).setBackgroundColor(getResources().getColor(R.color.tab_background_selected2));
            tabLayout.getTabAt(0).getCustomView().findViewById(R.id.txt_tab_detail).setBackgroundColor(getResources().getColor(R.color.tab_background_selected2));
            tabLayout.getTabAt(0).getCustomView().findViewById(R.id.img_arrow).setVisibility(View.VISIBLE);

            tabLayout.getTabAt(1).getCustomView().findViewById(R.id.txt_tab_title).setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
            tabLayout.getTabAt(1).getCustomView().findViewById(R.id.txt_tab_detail).setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
            tabLayout.getTabAt(1).getCustomView().findViewById(R.id.img_arrow).setVisibility(View.GONE);


            tabLayout.getTabAt(2).getCustomView().findViewById(R.id.txt_tab_title).setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
            tabLayout.getTabAt(2).getCustomView().findViewById(R.id.txt_tab_detail).setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
            tabLayout.getTabAt(2).getCustomView().findViewById(R.id.img_arrow).setVisibility(View.GONE);

            setNotesAdapter();

        } else if (tabPosition == 1) {

            fab.setVisibility(View.VISIBLE);

            tabLayout.getTabAt(0).getCustomView().findViewById(R.id.txt_tab_title).setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
            tabLayout.getTabAt(0).getCustomView().findViewById(R.id.txt_tab_detail).setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
            tabLayout.getTabAt(0).getCustomView().findViewById(R.id.img_arrow).setVisibility(View.GONE);

            tabLayout.getTabAt(1).getCustomView().findViewById(R.id.txt_tab_title).setBackgroundColor(getResources().getColor(R.color.tab_background_selected2));
            tabLayout.getTabAt(1).getCustomView().findViewById(R.id.txt_tab_detail).setBackgroundColor(getResources().getColor(R.color.tab_background_selected2));
            tabLayout.getTabAt(1).getCustomView().findViewById(R.id.img_arrow).setVisibility(View.VISIBLE);


            tabLayout.getTabAt(2).getCustomView().findViewById(R.id.txt_tab_title).setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
            tabLayout.getTabAt(2).getCustomView().findViewById(R.id.txt_tab_detail).setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
            tabLayout.getTabAt(2).getCustomView().findViewById(R.id.img_arrow).setVisibility(View.GONE);

            setPictureAdapter();

        } else if (tabPosition == 2) {

            fab.setVisibility(View.GONE);

            tabLayout.getTabAt(0).getCustomView().findViewById(R.id.txt_tab_title).setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
            tabLayout.getTabAt(0).getCustomView().findViewById(R.id.txt_tab_detail).setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
            tabLayout.getTabAt(0).getCustomView().findViewById(R.id.img_arrow).setVisibility(View.GONE);

            tabLayout.getTabAt(1).getCustomView().findViewById(R.id.txt_tab_title).setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
            tabLayout.getTabAt(1).getCustomView().findViewById(R.id.txt_tab_detail).setBackgroundColor(getResources().getColor(R.color.tab_background_unselected));
            tabLayout.getTabAt(1).getCustomView().findViewById(R.id.img_arrow).setVisibility(View.GONE);


            tabLayout.getTabAt(2).getCustomView().findViewById(R.id.txt_tab_title).setBackgroundColor(getResources().getColor(R.color.tab_background_selected2));
            tabLayout.getTabAt(2).getCustomView().findViewById(R.id.txt_tab_detail).setBackgroundColor(getResources().getColor(R.color.tab_background_selected2));
            tabLayout.getTabAt(2).getCustomView().findViewById(R.id.img_arrow).setVisibility(View.VISIBLE);

            setChatAdapter();

        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.img_chat, R.id.img_support_chat, R.id.fab, R.id.txt_user_name,R.id.btn_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                if (validate() )

                {

                    lyrPassword.setVisibility(View.GONE);
                    KeyboardUtils.hideSoftInput(getActivity());
                }

                break;
            case R.id.img_chat:
                ((HomeActivity) Objects.requireNonNull(getActivity())).updateScreen(new FragmentChats(), getString(R.string.fragment_chat), false);
                break;
            case R.id.img_support_chat:
                break;
            case R.id.fab:
                if (tabLayout.getSelectedTabPosition() == 1)

                    ((HomeActivity) getActivity()).checkCameraPermission();
                else if (tabLayout.getSelectedTabPosition() == 0) {
                    Intent i = new Intent(mContext, PersonalActivity.class);
                    i.putExtra(AppConstants.Personal_note, AppConstants.Personal_edit);
                    getActivity().startActivityForResult(i, AppConstants.REQUEST_CODE_PERSONAL_NOTE);
                }
                break;
            case R.id.txt_user_name:
                changeName();
                break;
        }
    }

    private void changeName() {
        new DialogChangeScreenName(mContext, User_settings.getScreenName(mContext), new ScreenNameChangeDialogResponse() {
            @Override
            public void onChangeName(String name) {
                KeyboardUtils.hideSoftInput(mActivity);
                changeNameApi(name);
            }

            @Override
            public void onClose() {

            }
        }).show();
    }

    private void changeNameApi(String name) {
        mProgressDialoge = CommonUtils.showLoadingDialog(mContext);

        AndroidNetworking.post(ApiEndPoints.END_POINT_UPDATE_USER_DETAIL)
                .addBodyParameter("screen_name", name)
                .addBodyParameter("user_id", User_settings.getUserId(mContext))
                .addBodyParameter("device_id", User_settings.getFirebaseToken(mContext))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (mProgressDialoge.isShowing())
                            mProgressDialoge.dismiss();

                        try {
                            JSONObject rootObject = new JSONObject(response.toString());

                            if (rootObject.getString("status").equalsIgnoreCase("1")) {
                                User_settings.setScreenName(mContext, name);
                                //SetName
                                setName();
                                /*txtBigTitle.setText((User_settings.getScreenName(mContext).toUpperCase()).substring(0, 1));
                                txtUserName.setText(User_settings.getScreenName(mContext));*/
                                db.updateGroupMember(DbConstants.KEY_NAME, name, DbConstants.KEY_ECC_ID, User_settings.getECCID(mContext));


                                CommonUtils.showInfoMsg(mContext, "Screen name Changed Successfully");

                            } else {
                                CommonUtils.showInfoMsg(mContext, rootObject.getString("msg"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        if (mProgressDialoge.isShowing())
                            mProgressDialoge.dismiss();
                        System.out.println("API Error : " + error.getErrorDetail());
                        CommonUtils.showInfoMsg(mContext, "Please try again.");

                    }
                });
    }


    @Override
    public void onItemClick(VaultEntity vaultListEntity, int position, ImageView imageView) {

        if (vaultListEntity.getMimeType() == AppConstants.ITEM_TYPE_PICTURE) {
            Intent intent = new Intent(mContext, PhotoViewActivity.class);
            intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, new ChatListEntity());
            intent.putExtra(AppConstants.EXTRA_FROM_VAULT, true);
            intent.putExtra(AppConstants.EXTRA_IMAGE_PATH, vaultListEntity.getImage());
            intent.putExtra("file_name", vaultListEntity.getName());

            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(mActivity, imageView, "photo");
            startActivity(intent, options.toBundle());

            //  startActivity(intent);
        } else if (vaultListEntity.getMimeType() == AppConstants.ITEM_TYPE_NOTES) {
            Intent i = new Intent(mContext, PersonalActivity.class);
            i.putExtra(AppConstants.EXTRA_PERSONAL_NOTE_FILE_PATH, vaultListEntity.getNotes());
            i.putExtra("name", vaultListEntity.getName());
            i.putExtra(AppConstants.Personal_note, AppConstants.Personal_save);
            Objects.requireNonNull(getActivity()).startActivityForResult(i, AppConstants.REQUEST_CODE_PERSONAL_NOTE);
        } else {
            Intent intent = new Intent(mContext, VaultMessageWindowActivity.class);
            intent.putExtra(AppConstants.EXTRA_VAULT_LIST_ITEM, vaultListEntity);
            intent.putExtra(AppConstants.EXTRA_IS_SHARE, true);
            startActivity(intent);
        }

    }

    @Override
    public void onItemLongPress(VaultEntity vaultListEntity, int position) {

    }

    @Override
    public void onDelete(VaultEntity vaultEntity, int position) {
        VaultAdapter.MyViewHolder viewHolder = (VaultAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(position);
        viewHolder.mSwipeLayout.close();

        if (vaultEntity.getMimeType() == AppConstants.ITEM_TYPE_NOTES) {
            String filePath = vaultEntity.getNotes();
            new File(filePath).delete();
            db.deleteVaultItem(vaultEntity.getId());
            setNotesAdapter();
        } else if (vaultEntity.getMimeType() == AppConstants.ITEM_TYPE_PICTURE) {
            String filePath = vaultEntity.getImage();
            new File(filePath).delete();
            db.deleteVaultItem(vaultEntity.getId());
            setPictureAdapter();
        } else {
            db.deleteMessageListEntity(vaultEntity.getDbId());
            db.deleteVaultItem(vaultEntity.getId());
            setChatAdapter();
        }


    }

    @Override
    public void onRename(VaultEntity vaultEntity, int position) {

        VaultAdapter.MyViewHolder viewHolder = (VaultAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(position);
        viewHolder.mSwipeLayout.close();
        if (vaultEntity.getMimeType() == AppConstants.ITEM_TYPE_NOTES) {
            new DialogChangeVaultItemName(mContext, vaultEntity, new VaultItemNameChangeDialogResponse() {
                @Override
                public void onChangeName() {
                    setNotesAdapter();
                }

                @Override
                public void onClose() {
                }
            }).show();
        } else if (vaultEntity.getMimeType() == AppConstants.ITEM_TYPE_PICTURE) {
            new DialogChangeVaultItemName(mContext, vaultEntity, new VaultItemNameChangeDialogResponse() {
                @Override
                public void onChangeName() {
                    setPictureAdapter();
                }

                @Override
                public void onClose() {
                }
            }).show();
        }
    }

    @Override
    public void onShare(VaultEntity vaultEntity, int position) {

        VaultAdapter.MyViewHolder viewHolder = (VaultAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(position);
        viewHolder.mSwipeLayout.close();


        Intent i = new Intent(mContext, ForwardMessageActivity.class);
        i.putExtra(AppConstants.EXTRA_MESSAGE_LIST, getMessage(vaultEntity));
        i.putExtra(AppConstants.IS_ENCRYPTED, false);
        i.putExtra("name", vaultEntity.getName());
        startActivity(i);
    }


    private ArrayList<ChatMessageEntity> getMessage(VaultEntity vaultEntity) {
        ArrayList<ChatMessageEntity> chatMessageEntityArrayList = new ArrayList<>();

        if (vaultEntity.getMimeType() == AppConstants.ITEM_TYPE_NOTES) {
            ChatMessageEntity message = new ChatMessageEntity();
            message.setMessageMimeType(AppConstants.MIME_TYPE_NOTE);
            message.setFilePath(vaultEntity.getNotes());
            message.setFileName(vaultEntity.getName());
            chatMessageEntityArrayList.add(message);
            return chatMessageEntityArrayList;
        } else {
            ChatMessageEntity message = new ChatMessageEntity();
            message.setImagePath(vaultEntity.getImage());
            message.setMessageMimeType(AppConstants.MIME_TYPE_IMAGE);
            message.setFileName(vaultEntity.getName());
            chatMessageEntityArrayList.add(message);
            return chatMessageEntityArrayList;
        }
    }

    @Override
    public void onImageAddResponse(String path) {

        if (new File(path).length() / 1024 > 301)
            compressImage(path);
        else {
            VaultEntity vaultEntity = new VaultEntity();
            vaultEntity.setMimeType(AppConstants.ITEM_TYPE_PICTURE);
            vaultEntity.setName(CommonUtils.getImageName());
            vaultEntity.setImage(path);
            vaultEntity.setMessageID(String.valueOf(System.currentTimeMillis()));
            vaultEntity.setDateTimeStamp(DateTimeUtils.getCurrentDateTime());
            vaultEntity.setMessageID(String.valueOf(System.currentTimeMillis()));
            db.insertVaultItem(vaultEntity);
            vaultList.add(vaultEntity);
            VaultAdapter.checkLists = new boolean[vaultList.size()];
            mAdapter.notifyItemInserted(vaultList.size());

            //to set item count
            if (tabLayout != null)
                ((TextView) tabLayout.getTabAt(1).getCustomView().findViewById(R.id.txt_tab_detail)).setText(vaultList.size() + getString(R.string.items));
        }


    }

    @Override
    public void onAddPersonalNote(String name, String Path) {
        VaultEntity vaultEntity = new VaultEntity();
        vaultEntity.setMimeType(AppConstants.ITEM_TYPE_NOTES);
        vaultEntity.setName(name);
        vaultEntity.setMessageID(String.valueOf(System.currentTimeMillis()));
        vaultEntity.setNotes(Path);
        vaultEntity.setDateTimeStamp(DateTimeUtils.getCurrentDateTime());
        db.insertVaultItem(vaultEntity);
        setNotesAdapter();

        ((TextView) tabLayout.getTabAt(0).getCustomView().findViewById(R.id.txt_tab_detail)).setText(vaultList.size() + getString(R.string.items));


    }

    @Override
    public void onChanged() {
        if (getActivity() != null)
            getActivity().runOnUiThread(() -> setChatAdapter());
    }


    //  ******************************************COMPRESS*************************

    private void compressImage(String file) {
        try {
            final InputStream is = new FileInputStream(file);
            File outfile = new File(CommonUtils.getImageDirectory(mContext), "tempCom.jpg");
            if (outfile.exists())
                outfile.delete();
            FileOutputStream fos = new FileOutputStream(outfile);
            byte[] buffer = new byte[4096];
            int len = -1;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            is.close();

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = mConfig;
            Bitmap originBitmap = BitmapFactory.decodeFile(outfile.getAbsolutePath(), options);


            String logMessage = "origin file size:" + Formatter.formatFileSize(mContext, outfile.length())
                    + "\nwidth:" + originBitmap.getWidth() + ",height:" + originBitmap.getHeight() + ",config:" + originBitmap.getConfig();
            Log.e(TAG, "Original File  : " + logMessage);

            Tiny.FileCompressOptions compressOptions = new Tiny.FileCompressOptions();
            compressOptions.outfile = file;
            compressOptions.config = mConfig;
            Tiny.getInstance().source(outfile).asFile().withOptions(compressOptions).compress(new FileCallback() {
                @Override
                public void callback(boolean isSuccess, String outfile, Throwable t) {
                    if (!isSuccess) {
                        Log.e("zxy", "error: " + t.getMessage());

                        return;
                    }
                    File file = new File(outfile);
                    String logMessage = "compress file size:" + Formatter.formatFileSize(mContext, file.length())
                            + "\noutfile: " + outfile;
                    Log.e(TAG, "Compressed File  : " + logMessage);

                    VaultEntity vaultEntity = new VaultEntity();
                    vaultEntity.setMimeType(AppConstants.ITEM_TYPE_PICTURE);
                    vaultEntity.setName(CommonUtils.getImageName());
                    vaultEntity.setImage(outfile);
                    vaultEntity.setDateTimeStamp(DateTimeUtils.getCurrentDateTime());
                    vaultEntity.setMessageID(String.valueOf(System.currentTimeMillis()));
                    db.insertVaultItem(vaultEntity);/*
                    vaultList.add(vaultEntity);
                    VaultAdapter.checkLists = new boolean[vaultList.size()];
                    mAdapter.notifyItemInserted(vaultList.size());*/
                    setPictureAdapter();
                    //to set item count

                    if (tabLayout != null)
                        ((TextView) tabLayout.getTabAt(1).getCustomView().findViewById(R.id.txt_tab_detail)).setText(vaultList.size() + getString(R.string.items));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        lyrMain.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        //
        // lyrMain.setVisibility(View.GONE);
    }
    private boolean validate() {
        if (TextUtils.isEmpty(edtPassword.getText().toString().trim())) {
            Toast.makeText(getContext(), "Please enter password.", Toast.LENGTH_LONG).show();
            return false;
        } else if (!edtPassword.getText().toString().trim().equals(User_settings.getAppPassword(mContext))) {
            Toast.makeText(getContext(), "wrong password. Try again!", Toast.LENGTH_LONG).show();
            txtWrong.setVisibility(View.VISIBLE);
            return false;
        } else {
            return true;
        }
    }

}