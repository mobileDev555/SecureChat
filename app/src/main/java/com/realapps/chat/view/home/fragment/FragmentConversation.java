package com.realapps.chat.view.home.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.realapps.chat.R;
import com.realapps.chat.view.home.activity.HomeActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Prashant Sharma on 3/15/2018.
 * Core techies
 * prashant@coretechies.org
 */

public class FragmentConversation extends Fragment  {

    Unbinder unbinder;
    @BindView(R.id.navigation)
    TabLayout navigation;
    private Context mContext;
    private Activity mActivity;
    public static int tabposition = 0;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);
        mContext = getContext();
        mActivity = getActivity();
        unbinder = ButterKnife.bind(this, view);
        updateScreen(new FragmentChats(),false);
        initview();
        return view;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.chat_menu, menu);

        SearchManager searchManager = (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();

        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(tabposition==0){
                    if(FragmentChats.searchResultResponse!=null)
                        FragmentChats.searchResultResponse.onQueryTextSubmit(query);
                }else{
                    if(FragmentCall.searchResultResponse!=null)
                        FragmentCall.searchResultResponse.onQueryTextSubmit(query);
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if(tabposition==0){
                    if(FragmentChats.searchResultResponse!=null)
                        FragmentChats.searchResultResponse.onQueryTextChange(query);
                }else{
                    if(FragmentCall.searchResultResponse!=null)
                        FragmentCall.searchResultResponse.onQueryTextChange(query);
                }

                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    private void initview() {
        navigation.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabposition = tab.getPosition();
                switch (tab.getPosition()) {
                    case 0:
                        if(HomeActivity.CALL_DELETE_MODE)
                            FragmentCall.deactivateDeleteModeLister.onDeActive();
                        ((HomeActivity) getActivity()).fabButtonVisible();
                        updateScreen(new FragmentChats(),false);

                        break;
                    case 1:
                        if(HomeActivity.CHAT_DELETE_MODE)
                            FragmentChats.deactivateDeleteModeLister.onDeActive();
                        ((HomeActivity) getActivity()).fabButtonGone();
                        updateScreen(new FragmentCall(),false);
                        break;


                }

            }


            @Override
            public void onTabUnselected(TabLayout.Tab tab) {


            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    public void updateScreen(Fragment fragment, boolean animStatus) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        String backStateName = fragment.getClass().getName();
//        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);
//
//        if (!fragmentPopped) {
//            //fragment not in back stack, create it.
//
//
//            if (animStatus)
//                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

            ft.replace(R.id.fragment, fragment);
            ft.addToBackStack(backStateName);
            ft.commit();

    }





}
