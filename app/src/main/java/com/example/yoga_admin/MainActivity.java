package com.example.yoga_admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yoga_admin.OliDB.Models.Workshop;
import com.example.yoga_admin.OliDB.WorkshopsTable;
import com.example.yoga_admin.adapters.WorkshopAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Workshop> workshopList;
    private WorkshopAdapter adapter;
    private static final int ADD_WORKSHOP_REQUEST = 1;
    private static final int EDIT_WORKSHOP_REQUEST = 2;
    private WorkshopsTable workshopsDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        workshopList = new ArrayList<>();
        adapter = new WorkshopAdapter(this, workshopList);

        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);

        workshopsDB = WorkshopsTable.initFor(getApplication(), "workshops_db", 1);
        workshopsDB.load();
        workshopList.addAll(workshopsDB.loaded());
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editOrDeleteWorkshop(position);
            }
        });
    }

    private void editOrDeleteWorkshop(final int position) {
        final Workshop workshop = workshopList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this workshop?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (workshopsDB.deleteByPosition(position)) {
                            workshopList.remove(position);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(MainActivity.this, "Deleted " + workshop.getWorkshopName(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                })
                .create()
                .show();
    }

    public void navigateToAddWorkshop(View view) {
        Intent intent = new Intent(this, AddWorkshopActivity.class);
        startActivityForResult(intent, ADD_WORKSHOP_REQUEST);
    }

    public void navigateToEditWorkshop(View view) {
        Intent intent = new Intent(this, EditWorkshopActivity.class);
        startActivity(intent);
    }

    public void deleteWorkshop(View view) {
        // Retrieve the position of the workshop item associated with the delete button
        int position = (int) view.getTag();

        // Call the method to edit or delete the workshop
        editOrDeleteWorkshop(position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == ADD_WORKSHOP_REQUEST) {
            String workshopName = data.getStringExtra("workshopName");
            String workshopDescription = data.getStringExtra("workshopDescription");
            String date = data.getStringExtra("date");
            String startTime = data.getStringExtra("startTime");
            String endTime = data.getStringExtra("endTime");
            int capacity = data.getIntExtra("capacity", 0);
            float price = data.getFloatExtra("price", 0.0f);
            String workshopType = data.getStringExtra("workshopType");

            Workshop workshop = null;
            long insertedId = workshopsDB.insertWorkshop(workshop);

            if (insertedId != -1) {
                workshop = new Workshop();
                workshop.setId((int) insertedId);
                workshop.setWorkshopName(workshopName);
                workshop.setWorkshopDescription(workshopDescription);
                workshop.setDate(date);
                workshop.setStartTime(startTime);
                workshop.setEndTime(endTime);
                workshop.setCapacity(capacity);
                workshop.setPrice(price);
                workshop.setWorkshopType(workshopType);

                workshopList.add(workshop);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
