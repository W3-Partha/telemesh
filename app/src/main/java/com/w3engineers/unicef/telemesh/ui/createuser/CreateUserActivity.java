package com.w3engineers.unicef.telemesh.ui.createuser;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.w3engineers.unicef.telemesh.R;
import com.w3engineers.unicef.telemesh.data.helper.constants.Constants;
import com.w3engineers.unicef.telemesh.data.provider.ServiceLocator;
import com.w3engineers.unicef.telemesh.databinding.ActivityCreateUserBinding;
import com.w3engineers.unicef.telemesh.ui.chooseprofileimage.ProfileImageActivity;
import com.w3engineers.unicef.util.base.ui.BaseActivity;
import com.w3engineers.unicef.util.helper.CommonUtil;
import com.w3engineers.unicef.util.helper.uiutil.UIHelper;

import java.util.List;

/*
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * ============================================================================
 */

public class CreateUserActivity extends BaseActivity implements View.OnClickListener {

    private ActivityCreateUserBinding mBinding;
    public static int INITIAL_IMAGE_INDEX = -1;
    private CreateUserViewModel mViewModel;

    public static CreateUserActivity sInstance;
    private boolean isNeedToImportWallet;

    @NonNull
    public static String IMAGE_POSITION = "image_position";


    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_user;
    }

    @Override
    protected int statusBarColor() {
        return R.color.colorPrimaryDark;
    }

    @SuppressLint("CheckResult")
    @Override
    protected void startUI() {

        mBinding = (ActivityCreateUserBinding) getViewDataBinding();
        isNeedToImportWallet = getIntent().getBooleanExtra("import_wallet", false);

        setTitle(getString(R.string.create_user));
        mViewModel = getViewModel();

        setClickListener(mBinding.imageViewBack, mBinding.buttonSignup);

        UIHelper.hideKeyboardFrom(this, mBinding.editTextFirstName);
        UIHelper.hideKeyboardFrom(this, mBinding.editTextLastName);

        mViewModel.firstNameChangeLiveData.observe(this, this::nextButtonControl);
        mViewModel.lastNameChangeLiveData.observe(this, this::nextButtonControl);


        mViewModel.firstNameEditControl(mBinding.editTextFirstName);
        mViewModel.lastNameEditControl(mBinding.editTextLastName);

        sInstance = this;

        mBinding.editTextFirstName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                nextAction();
                return true;
            }
            return false;
        });
    }

    private void nextButtonControl(String nameText) {

        updateImageNameField();


        String firstName = mBinding.editTextFirstName.getText().toString();
        String lastName = mBinding.editTextLastName.getText().toString();

        if (!TextUtils.isEmpty(firstName)
                && !TextUtils.isEmpty(lastName)
                && firstName.length() >= Constants.DefaultValue.MINIMUM_TEXT_LIMIT
                && lastName.length() >= Constants.DefaultValue.MINIMUM_TEXT_LIMIT) {

            mBinding.buttonSignup.setBackgroundResource(R.drawable.ractangular_gradient);
            mBinding.buttonSignup.setTextColor(getResources().getColor(R.color.white));
            //mBinding.buttonSignup.setClickable(true);
        } else {

            mBinding.buttonSignup.setBackgroundResource(R.drawable.ractangular_white);
            mBinding.buttonSignup.setTextColor(getResources().getColor(R.color.new_user_button_color));
            //mBinding.buttonSignup.setClickable(false);
        }
    }

    private void updateImageNameField() {
        String firstName = mBinding.editTextFirstName.getText().toString();
        String lastName = mBinding.editTextLastName.getText().toString();

        String finalText = "";


        if (!TextUtils.isEmpty(firstName)) {

            finalText = String.valueOf(firstName.charAt(0));
        }
        if (!TextUtils.isEmpty(lastName)) {

            finalText += String.valueOf(lastName.charAt(0));
        }

        if (TextUtils.isEmpty(finalText)) {
            mBinding.textViewImageName.setVisibility(View.GONE);

            mBinding.imageProfileBackground.setVisibility(View.VISIBLE);
            mBinding.imageProfile.setVisibility(View.VISIBLE);
        } else {
            mBinding.textViewImageName.setVisibility(View.VISIBLE);

            mBinding.imageProfileBackground.setVisibility(View.INVISIBLE);
            mBinding.imageProfile.setVisibility(View.GONE);
        }

        mBinding.textViewImageName.setText(finalText);
    }

    private CreateUserViewModel getViewModel() {
        return ViewModelProviders.of(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) ServiceLocator.getInstance().getCreateUserViewModel(getApplication());
            }
        }).get(CreateUserViewModel.class);
    }

    protected void requestMultiplePermissions() {

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE/*,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION*/)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if (report.areAllPermissionsGranted()) {
                            goNext();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            CommonUtil.showPermissionPopUp(CreateUserActivity.this);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(
                            List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).withErrorListener(error -> requestMultiplePermissions()).onSameThread().check();
    }

    @Override
    public void onClick(@NonNull View view) {
        super.onClick(view);

        int id = view.getId();

        if (id == R.id.button_signup) {
            nextAction();
        } else if (id == R.id.image_profile) {
            Intent intent = new Intent(this, ProfileImageActivity.class);
            intent.putExtra(CreateUserActivity.IMAGE_POSITION, mViewModel.getImageIndex());
            startActivityForResult(intent, Constants.RequestCodes.PROFILE_IMAGE_REQUEST);
        } else if (id == R.id.image_view_back) {
            finish();
        }

    }

    private void nextAction() {

        saveData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && requestCode == Constants.RequestCodes.PROFILE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            mViewModel.setImageIndex(data.getIntExtra(IMAGE_POSITION, INITIAL_IMAGE_INDEX));

            int id = getResources().getIdentifier(Constants.drawables.AVATAR_IMAGE + mViewModel.getImageIndex(), Constants.drawables.AVATAR_DRAWABLE_DIRECTORY, getPackageName());
            mBinding.imageProfile.setImageResource(id);

            nextButtonControl(mBinding.editTextFirstName.getText().toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sInstance = null;
    }

    private void saveData() {

        if (CommonUtil.isValidName(mBinding.editTextFirstName.getText().toString(), this, true)
                && CommonUtil.isValidName(mBinding.editTextLastName.getText().toString(), this, false)) {
            requestMultiplePermissions();
        }
    }

    protected void goNext() {
        if (mViewModel.storeData(mBinding.editTextFirstName.getText() + "",
                mBinding.editTextLastName.getText() + "")) {



            mViewModel.launchWalletPage(isNeedToImportWallet);

            /*Intent intent = new Intent(CreateUserActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();*/
        }
    }
}
