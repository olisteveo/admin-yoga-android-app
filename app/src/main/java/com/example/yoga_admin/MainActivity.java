// MainActivity.java
package com.example.yoga_admin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todolist.OliDB.Models.Task;
import com.example.todolist.OliDB.TasksTable;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> taskList;
    private ArrayAdapter<String> adapter;
    private static final int ADD_TASK_REQUEST = 1; // Request code for AddTaskActivity
    private static final int EDIT_TASK_REQUEST = 2; // Request code for EditTaskActivity
    private EditText editText;
    private TasksTable tasksDB; // Preloaded tasks from the database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialise task list and adapter with custom layout
        taskList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.custom_task_list_item, R.id.textViewTaskName, taskList);
        // Set up the ListView
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);

        // Find the EditText
        editText = findViewById(R.id.editText);

        // Set click listener for list items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editOrDeleteTask(position);
            }
        });

        // Add bottom border to EditText
        addBottomBorder();

        // Initialise tasks database
        tasksDB = TasksTable.getInstance();
        Log.d("MyApp", "Activity onCreate()");

        // If any tasks are loaded from the database, add them to the list
        if (!tasksDB.loaded().isEmpty()) {
            for (Task task : tasksDB.loaded()) {
                Log.d("Tasks", task.getTaskName());
                taskList.add(tasksDB.loaded().indexOf(task), task.getTaskName());
            }
        }
    }

    // Method to add task
    public void addTask(View view) {
        String task = editText.getText().toString().trim();

        if (!task.isEmpty()) {
            // Add task to the beginning of the list
            taskList.add(0, task);
            adapter.notifyDataSetChanged();
            editText.getText().clear();
            TasksTable.getInstance().insertTask(task, "", 0); // Insert task into the database
        } else {
            Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to handle editing or deleting a task
    private void editOrDeleteTask(final int position) {
        // Placeholder method for editing or deleting a task
        final String task = taskList.get(position);
        // For simplicity, currently showing a toast message with task details
        Toast.makeText(this, "Selected task: " + task + "\nPosition: " + position, Toast.LENGTH_SHORT).show();
    }

    // Method to delete a task
    public void deleteTask(View view) {
        View listItem = (View) view.getParent(); // Find the parent view of the delete button
        final int position = ((ListView) findViewById(R.id.listView)).getPositionForView(listItem); // Find the index of the list item
        showDeleteConfirmationDialog(position); // Show the delete confirmation dialog
    }

    // Method to show delete confirmation dialog
    private void showDeleteConfirmationDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Context context = this;
        builder.setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If user confirms deletion, delete the task
                        if (TasksTable.getInstance().deleteByPosition(position)) {
                            String taskName = taskList.get(position).toString();
                            taskList.remove(position);
                            adapter.notifyDataSetChanged();
                            StringBuilder msg = new StringBuilder("Deleted ");
                            msg.append(taskName);
                            Toast.makeText(context, msg.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If user cancels deletion, do nothing
                    }
                })
                .create()
                .show();
    }

    // Method to navigate to AddTaskActivity
    public void navigateToAddTask(View view) {
        Intent intent = new Intent(this, AddTaskActivity.class);
        startActivityForResult(intent, ADD_TASK_REQUEST); // Start AddTaskActivity with request code
    }

    // Method to navigate to EditTaskActivity
    public void navigateToEditTask(View view) {
        Intent intent = new Intent(this, EditTaskActivity.class);
        startActivity(intent);
    }

    // Handle the result from AddTaskActivity and EditTaskActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == ADD_TASK_REQUEST) {
                // Extract the task details from the intent
                String taskName = data.getStringExtra("taskName");
                String taskDescription = data.getStringExtra("taskDescription");

                // Construct the task string
                String task = taskName + ": " + taskDescription;

                // Add the task to the top of the list
                TasksTable.getInstance().insertTask(taskName, taskDescription, 0); // Insert task into the database
                taskList.add(0, task);
                adapter.notifyDataSetChanged();
            } else if (requestCode == EDIT_TASK_REQUEST) {
                // Handle the result from EditTaskActivity if needed
            }
        }
    }

    // Method to add bottom border to EditText
    private void addBottomBorder() {
        // Set bottom border color and height
        Drawable drawable = getResources().getDrawable(R.drawable.edittext_bottom_border);
        drawable.setColorFilter(Color.parseColor("#FF4081"), PorterDuff.Mode.SRC_ATOP);
        editText.setBackground(drawable);
    }
}
