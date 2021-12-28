package com.rs.smartpoultryfarm.fragment;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.adapter.ContactAdapter;
import com.rs.smartpoultryfarm.controller.AppController;
import com.rs.smartpoultryfarm.model.Contact;
import com.rs.smartpoultryfarm.remote.PermissionManager;
import com.rs.smartpoultryfarm.util.AppExtensions;
import com.rs.smartpoultryfarm.util.Constants;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("ClickableViewAccessibility")
public class ContactsFragment extends DialogFragment {

    private static final String TAG = ContactsFragment.class.getSimpleName();
    private AppCompatImageView  backBtn;
    private RecyclerView        rcvContacts;
    private ContactAdapter      contactAdapter;

    public static ContactsFragment show(){
        ContactsFragment fragment = new ContactsFragment();
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
        return inflater.inflate(R.layout.fragment_layout_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        init();
    }

    private void idSetup(View view) {
        backBtn = view.findViewById(R.id.backBtn);
        rcvContacts = view.findViewById(R.id.rcvContacts);
        contactAdapter = new ContactAdapter();
    }

    private void init(){
        backBtn.setOnClickListener(view -> dismiss());

        rcvContacts.setHasFixedSize(true);
        rcvContacts.setAdapter(contactAdapter);

        if (!new PermissionManager(PermissionManager.Permission.CONTACT, true, response -> setAllContacts()).isGranted()) return;
        setAllContacts();
    }

    private void setAllContacts(){
        try {
            List<Contact> contacts = new ArrayList<>();
            Cursor phones = AppController.getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
            assert phones != null;
            while (phones.moveToNext())
            {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contacts.add(new Contact(name, phoneNumber));
            }
            phones.close();
            contactAdapter.setContacts(contacts);
        }
        catch (Exception ex){
            ex.printStackTrace();
            Log.e(Constants.TAG, ex.getMessage()+"");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
