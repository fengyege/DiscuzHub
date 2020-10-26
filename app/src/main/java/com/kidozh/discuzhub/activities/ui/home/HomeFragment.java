package com.kidozh.discuzhub.activities.ui.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.LoginActivity;
import com.kidozh.discuzhub.adapter.ForumCategoryAdapter;
import com.kidozh.discuzhub.databinding.ActivityBbsForumIndexBinding;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.interact.BaseStatusInteract;
import com.kidozh.discuzhub.results.BBSIndexResult;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.URLUtils;

import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();

    private HomeViewModel homeViewModel;


    ForumCategoryAdapter adapter;
    bbsInformation bbsInfo;
    forumUserBriefInfo userBriefInfo;

    ActivityBbsForumIndexBinding activityBbsForumIndexBinding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        activityBbsForumIndexBinding = ActivityBbsForumIndexBinding.inflate(getLayoutInflater());
        View root = activityBbsForumIndexBinding.getRoot();
        getIntentInfo();

        configurePortalRecyclerview();
        bindLiveDataFromViewModel();
        //getPortalCategoryInfo();
        configureRefreshBtn();
        configureSwipeRefreshLayout();

        return root;
    }

    public HomeFragment(){

    }

    public static HomeFragment newInstance(bbsInformation bbsInformation, forumUserBriefInfo userBriefInfo){
        HomeFragment homeFragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInformation);
        args.putSerializable(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
        homeFragment.setArguments(args);
        return homeFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bbsInfo = (bbsInformation) getArguments().getSerializable(bbsConstUtils.PASS_BBS_ENTITY_KEY);
            userBriefInfo = (forumUserBriefInfo)  getArguments().getSerializable(bbsConstUtils.PASS_BBS_USER_KEY);

        }
    }

    private void getIntentInfo(){

        if(bbsInfo != null){
            homeViewModel.setBBSInfo(bbsInfo,userBriefInfo);
            return;
        }
        Intent intent = getActivity().getIntent();
        bbsInfo = (bbsInformation) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        userBriefInfo = (forumUserBriefInfo) intent.getSerializableExtra(bbsConstUtils.PASS_BBS_USER_KEY);
        if(bbsInfo == null){
            getActivity().finish();
        }
        else {
            Log.d(TAG,"get bbs name "+bbsInfo.site_name);
            URLUtils.setBBS(bbsInfo);
            homeViewModel.setBBSInfo(bbsInfo,userBriefInfo);
            //bbsURLUtils.setBaseUrl(bbsInfo.base_url);
        }


    }

    private void configureSwipeRefreshLayout(){
        activityBbsForumIndexBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                homeViewModel.loadForumCategoryInfo();
            }
        });
    }

    private void bindLiveDataFromViewModel(){
        homeViewModel.getForumCategoryInfo().observe(getViewLifecycleOwner(), new Observer<List<BBSIndexResult.ForumCategory>>() {
            @Override
            public void onChanged(List<BBSIndexResult.ForumCategory> forumCategories) {
                if(homeViewModel.bbsIndexResultMutableLiveData.getValue() !=null &&
                        homeViewModel.bbsIndexResultMutableLiveData.getValue().forumVariables !=null){
                    List<ForumInfo> allForumInfo = homeViewModel.bbsIndexResultMutableLiveData.getValue().forumVariables.forumInfoList;
                    adapter.setForumCategoryList(forumCategories,allForumInfo);
                }

            }
        });

        homeViewModel.errorMessageMutableLiveData.observe(getViewLifecycleOwner(),errorMessage -> {
            if(errorMessage!=null){
                activityBbsForumIndexBinding.errorView.setVisibility(View.VISIBLE);
                activityBbsForumIndexBinding.errorIcon.setImageResource(R.drawable.ic_error_outline_24px);
                activityBbsForumIndexBinding.errorValue.setText(errorMessage.key);
                activityBbsForumIndexBinding.errorContent.setText(errorMessage.content);
            }
        });

        homeViewModel.userBriefInfoMutableLiveData.observe(getViewLifecycleOwner(), new Observer<forumUserBriefInfo>() {
            @Override
            public void onChanged(forumUserBriefInfo userBriefInfo) {
                Log.d(TAG,"changed user to "+userBriefInfo);
                if(userBriefInfo == null && userBriefInfo!=null){
                    // raise dialog
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
                    //MaterialAlertDialogBuilder builder =  new AlertDialog.Builder(getActivity());
                    builder.setMessage(getString(R.string.user_login_expired,userBriefInfo.username))
                            .setPositiveButton(getString(R.string.user_relogin, userBriefInfo.username), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                                    intent.putExtra(bbsConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                                    intent.putExtra(bbsConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                                    startActivity(intent);
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.show();
                }
            }
        });
        homeViewModel.isLoading.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    activityBbsForumIndexBinding.swipeRefreshLayout.setRefreshing(true);
                }
                else {
                    activityBbsForumIndexBinding.swipeRefreshLayout.setRefreshing(false);
                }

            }
        });
        homeViewModel.bbsIndexResultMutableLiveData.observe(getViewLifecycleOwner(),bbsIndexResult -> {
            if(getContext() instanceof BaseStatusInteract){
                ((BaseStatusInteract) getContext()).setBaseResult(bbsIndexResult,
                        bbsIndexResult!=null?bbsIndexResult.forumVariables:null);
            }
        });
    }


    private void configurePortalRecyclerview(){
        activityBbsForumIndexBinding.bbsPortalRecyclerview.setHasFixedSize(true);
        activityBbsForumIndexBinding.bbsPortalRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ForumCategoryAdapter(bbsInfo,userBriefInfo);
        activityBbsForumIndexBinding.bbsPortalRecyclerview.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        activityBbsForumIndexBinding.bbsPortalRecyclerview.setAdapter(adapter);
    }

    private void configureRefreshBtn(){
        activityBbsForumIndexBinding.bbsPortalRefreshPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.loadForumCategoryInfo();
            }
        });
    }
}