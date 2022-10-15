package com.rcappstudio.indoorfarming.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.rcappstudio.indoorfarming.R;


public class PlantConfirmationDialog extends AppCompatDialogFragment {
    private EditText editTextPlantName;

    private PlantConfirmationDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.requireActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog_confirmation, null);

        builder.setView(view)
                .setTitle("Confirmation")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String plantName = editTextPlantName.getText().toString();

                        listener.getPlantsName(plantName);
                    }
                });

        editTextPlantName = view.findViewById(R.id.edit_mac_address);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (PlantConfirmationDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ExampleDialogListener");
        }
    }

    public interface PlantConfirmationDialogListener {
        void getPlantsName(String plantsName);
    }
}
