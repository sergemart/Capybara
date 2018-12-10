package com.github.sergemart.mobile.capybara.workflow.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.R;
import com.github.sergemart.mobile.capybara.data.datastore.ResStore;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;


public class UpgradeBackendRetryDialogFragment extends DialogFragment {

    private Throwable mCause;


    // --------------------------- Getters/ setters

    void setCause(Throwable cause) {
        mCause = cause;
    }


    // --------------------------- Override dialog fragment lifecycle event handlers

    /**
     * View-unrelated startup actions
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    /**
     * The dialog factory
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog alertDialog = new AlertDialog.Builder(Objects.requireNonNull( super.getActivity() ))
            .setTitle(ResStore.get().getUpgradeBackendRetryDialogTitleR(mCause))
            .setMessage(ResStore.get().getUpgradeBackendRetryDialogMessageR(mCause))
            .setIcon(ResStore.get().getUpgradeBackendRetryDialogIconR(mCause))
            .setPositiveButton(R.string.action_retry, (dialog, button) ->
                Objects.requireNonNull(super.getParentFragment()).onActivityResult(                 // use Fragment#onActivityResult() as a callback
                    Constants.REQUEST_CODE_DIALOG_FRAGMENT,
                    Activity.RESULT_OK,
                    super.getActivity().getIntent()
                )
            )
            .setNegativeButton(R.string.action_thanks_no, (dialog, button) -> dialog.cancel())
            .create()
        ;
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);                                                           // a kind of modal (not really)
        return alertDialog;
    }


    /**
     * On cancel
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        Objects.requireNonNull(super.getParentFragment()).onActivityResult(                         // use Fragment#onActivityResult() as a callback
            Constants.REQUEST_CODE_DIALOG_FRAGMENT,
            Activity.RESULT_CANCELED,
            Objects.requireNonNull(super.getActivity()).getIntent()
        );
    }


    /**
     * Fix a compat lib bug causing the dialog dismiss on rotate
     */
    @Override
    public void onDestroyView() {
        if (super.getDialog() != null && super.getRetainInstance()) super.getDialog().setDismissMessage(null);
        super.onDestroyView();
    }


    // --------------------------- Static encapsulation-leveraging methods

    /**
     * The dialog fragment factory
     */
    public static UpgradeBackendRetryDialogFragment newInstance(Throwable cause) {
        UpgradeBackendRetryDialogFragment instance = new UpgradeBackendRetryDialogFragment();
        instance.setCause(cause);
        return instance;
    }

}
