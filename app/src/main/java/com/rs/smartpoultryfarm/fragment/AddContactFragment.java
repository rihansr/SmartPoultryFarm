package com.rs.smartpoultryfarm.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.controller.AppController;
import com.rs.smartpoultryfarm.model.Contact;
import com.rs.smartpoultryfarm.util.AppExtensions;
import com.rs.smartpoultryfarm.util.Constants;
import com.rs.smartpoultryfarm.util.SharedPreference;

import java.util.Objects;

@SuppressLint("ClickableViewAccessibility")
public class AddContactFragment extends DialogFragment {

    private static final String TAG = AddContactFragment.class.getSimpleName();
    private AppCompatImageView  backBtn;
    private TextInputEditText   nameInput;
    private TextInputEditText   numberInput;
    private AppCompatButton     addContact;
    private SharedPreference    sp;

    public static AddContactFragment show(Contact contact){
        AddContactFragment fragment = new AddContactFragment();
        if(contact != null){
            Bundle args = new Bundle();
            args.putSerializable(Constants.CONTACT_BUNDLE_KEY, contact);
            fragment.setArguments(args);
        }
        fragment.show(((AppCompatActivity) AppController.getActivity()).getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        AppExtensions.halfScreenDialog(getDialog());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_add_contact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        init();
    }

    private void idSetup(View view) {
        backBtn = view.findViewById(R.id.backBtn);
        nameInput = view.findViewById(R.id.nameInput);
        numberInput = view.findViewById(R.id.numberInput);
        addContact = view.findViewById(R.id.addBtn);
        sp = new SharedPreference();
    }

    private void init(){
        if (getArguments() != null) {
            Contact contact = (Contact) getArguments().getSerializable(Constants.CONTACT_BUNDLE_KEY);
            nameInput.setText(contact != null && contact.getName() != null? contact.getName() : "");
            numberInput.setText(contact != null && contact.getNumber() != null ? contact.getNumber() : "");
            getArguments().remove(Constants.CONTACT_BUNDLE_KEY);
        }

        addContact.setOnClickListener(view -> addContact());

        backBtn.setOnClickListener(view -> dismiss());
    }

    private void addContact(){
        String phone = Objects.requireNonNull(numberInput.getText()).toString().trim();
        if (TextUtils.isEmpty(phone)) {
            numberInput.setError(getString(R.string.phone_Error));
            AppExtensions.requestFocus(numberInput);
            return;
        }
        AppExtensions.hideKeyboardInDialog();

        String name = Objects.requireNonNull(nameInput.getText()).toString().trim();
        sp.storeEmergencyContact(new Contact(name, phone));
        dismiss();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
