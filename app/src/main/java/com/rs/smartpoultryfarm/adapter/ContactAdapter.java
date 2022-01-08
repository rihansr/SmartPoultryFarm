package com.rs.smartpoultryfarm.adapter;

import static com.rs.smartpoultryfarm.util.AppExtensions.string;
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.model.Contact;
import com.rs.smartpoultryfarm.util.AppExtensions;
import com.rs.smartpoultryfarm.util.SharedPreference;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("NotifyDataSetChanged")
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<Contact> contacts = new ArrayList<>();
    private final SharedPreference sp;

    public ContactAdapter() {
        sp = new SharedPreference();
    }

    public void setContacts(List<Contact> contacts) {
        if(!AppExtensions.isNullOrEmpty(contacts)) this.contacts = contacts;
        else this.contacts = new ArrayList<>();
        notifyDataSetChanged();
    }

    private Contact contact(int position){
        return contacts.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.sample_contact, parent, false);
        return new ViewHolder(view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatTextView contactName;
        private final AppCompatTextView contactNumber;
        private final AppCompatTextView addContact;
        private final AppCompatTextView removeContact;

        private ViewHolder(View v) {
            super(v);
            contactName = v.findViewById(R.id.name);
            contactNumber = v.findViewById(R.id.number);
            addContact = v.findViewById(R.id.addContact);
            removeContact = v.findViewById(R.id.removeContact);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Contact emergencyContact = sp.getEmergencyContact();
        if(emergencyContact != null && emergencyContact.getNumber() != null && contact(position) != null && contact(position).getNumber() != null){
            String emergencyNumber = emergencyContact.getNumber();
            String currentNumber = contact(position).getNumber();
            if(emergencyNumber.equals(currentNumber)
                    || ("0" + emergencyNumber).equals(currentNumber)
                    || ("0" + currentNumber).equals(emergencyNumber)
                    || ("880" + emergencyNumber).equals(currentNumber)
                    || ("880" + currentNumber).equals(emergencyNumber)
                    || ("+880" + emergencyNumber).equals(currentNumber)
                    || ("+880" + currentNumber).equals(emergencyNumber)){

                holder.addContact.setVisibility(View.GONE);
                holder.removeContact.setVisibility(View.VISIBLE);
            }
            else {
                holder.addContact.setVisibility(View.VISIBLE);
                holder.removeContact.setVisibility(View.GONE);
            }
        }
        else {
            holder.addContact.setVisibility(View.VISIBLE);
            holder.removeContact.setVisibility(View.GONE);
        }

        holder.contactName.setText(contact(position).getName() == null ? string(R.string.unknown) : contact(position).getName());
        holder.contactNumber.setText(contact(position).getNumber() == null ? string(R.string.numberNotFound) : contact(position).getNumber());

        holder.addContact.setOnClickListener(view -> {
            sp.storeEmergencyContact(contact(position));
            notifyDataSetChanged();
        });

        holder.removeContact.setOnClickListener(view -> {
            sp.storeEmergencyContact(null);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }
}
