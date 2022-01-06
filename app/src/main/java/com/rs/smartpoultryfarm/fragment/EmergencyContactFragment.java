package com.rs.smartpoultryfarm.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.controller.AppController;
import com.rs.smartpoultryfarm.model.Contact;
import com.rs.smartpoultryfarm.util.AppExtensions;
import com.rs.smartpoultryfarm.util.SharedPreference;

@SuppressLint("ClickableViewAccessibility")
public class EmergencyContactFragment extends DialogFragment {

    private static final String TAG = EmergencyContactFragment.class.getSimpleName();
    private AppCompatImageView  backBtn;
    private AppCompatTextView   contactName;
    private AppCompatTextView   contactNumber;
    private AppCompatTextView   editContact;
    private AppCompatTextView   removeContact;
    private SharedPreference    sp;

    public static EmergencyContactFragment show(){
        EmergencyContactFragment fragment = new EmergencyContactFragment();
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
        return inflater.inflate(R.layout.fragment_layout_emergency_contact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        init();
    }

    private void idSetup(View view) {
        backBtn = view.findViewById(R.id.backBtn);
        contactName = view.findViewById(R.id.name);
        contactNumber = view.findViewById(R.id.number);
        editContact = view.findViewById(R.id.editContact);
        removeContact = view.findViewById(R.id.removeContact);
        sp = new SharedPreference();
    }

    private void init(){
        final Contact contact = sp.getEmergencyContact();
        if(contact == null){
            dismiss();

            AlertDialogFragment.show(null, R.string.noEmergencyContact, R.string.addNew, R.string.nav_contacts)
                    .setOnDialogListener(new AlertDialogFragment.OnDialogListener() {
                        @Override public void onLeftButtonClick() { AddContactFragment.show(null); }
                        @Override public void onRightButtonClick() { ContactsFragment.show(); }
                    });
            return;
        }

        contactName.setText(contact.getName() != null ? contact.getName() : AppExtensions.string(R.string.unknown));
        contactNumber.setText(contact.getNumber() != null ? contact.getNumber() : AppExtensions.string(R.string.numberNotFound));

        editContact.setOnClickListener(view -> {
            dismiss();
            AddContactFragment.show(contact);
        });

        removeContact.setOnClickListener(view -> {
            sp.storeEmergencyContact(null);
            dismiss();
        });

        backBtn.setOnClickListener(view -> dismiss());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
