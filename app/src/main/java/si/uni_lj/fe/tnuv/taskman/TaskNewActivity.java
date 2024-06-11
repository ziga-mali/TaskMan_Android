package si.uni_lj.fe.tnuv.taskman;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TaskNewActivity extends AppCompatActivity {

    private String URL;
    private Calendar calendar;
    private String dateTime;
    private TextView dateTimeDisplay;
    private EditText taskNameInput;
    private EditText taskDescriptionInput;
    private String token;
    private String userID;
    private String projectID;
    private String projectIme;
    private String projectOpis;
    private String username;


    @SuppressLint("MissingInflatedId")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_new_activity);

        SharedPreferences prefs = TaskNewActivity.this.getSharedPreferences("TMan", Context.MODE_PRIVATE);
        userID = prefs.getString("userID", null);
        token = prefs.getString("token", null);

        Intent intent = getIntent();
        projectID = intent.getStringExtra("projectID");
        projectIme = intent.getStringExtra("projectIme");
        projectOpis = intent.getStringExtra("projectOpis");
        username = intent.getStringExtra("username");

        taskNameInput = findViewById(R.id.task_name_input);
        taskDescriptionInput = findViewById(R.id.task_description_input);
        Button taskFinishTimeButton = findViewById(R.id.task_finish_time_input);
        calendar = Calendar.getInstance();
        taskFinishTimeButton.setOnClickListener(v -> showDateTimePickerDialog());
        dateTimeDisplay = findViewById(R.id.task_finish_time_display);
    }

    private void showDateTimePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            calendar.set(selectedYear, selectedMonth, selectedDay);
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timeView, selectedHour, selectedMinute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);

                dateTime = DateFormat.format("yyyy-MM-dd HH:mm:ss", calendar).toString();

                dateTimeDisplay.setText(dateTime);
            }, hour, minute, true);

            timePickerDialog.show();
        }, year, month, day);

        datePickerDialog.show();
    }
    public void startAddTask(View v) {
        Log.d("TaskNew L90", dateTime);

        String taskName = taskNameInput.getText().toString();
        String taskDescription = taskDescriptionInput.getText().toString();

        if (taskName.isEmpty() || taskDescription.isEmpty() || dateTime.isEmpty()) {
            Toast.makeText(this, "Prosimo izpolnite vsa polja.", Toast.LENGTH_SHORT).show();
        }

        JSONObject requestInfo = new JSONObject();
        try {
            requestInfo.put("token", token);
            requestInfo.put("userID", userID);
            requestInfo.put("ime", taskName);
            requestInfo.put("opis", taskDescription);
            requestInfo.put("kon_cas", dateTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        URL = this.getString(R.string.URL_base_storitve) + this.getString(R.string.projectsAPI) + projectID + this.getString(R.string.tasksAPI);

        useAPI api = new useAPI("POST", URL, requestInfo, true);

        api.uporabi(output -> {
            String responseCode = output.getResponseCode();
            String taskURL = output.getResponseString();
            Log.d("ProjectNewActivity L91", responseCode);

            if (responseCode.equals("201")) {

                TaskNewActivity.this.runOnUiThread(() -> {
                    Toast.makeText(TaskNewActivity.this, "Naloga uspešno ustvarjena", Toast.LENGTH_SHORT).show();
                });

                Intent intent = new Intent(TaskNewActivity.this, ProjectActivity_v2.class);
                intent.putExtra("taskURL", taskURL);
                Log.d("TaskNewActivity L136", taskURL);
                intent.putExtra("startingActivity", "TaskNewActivity");
                intent.putExtra("username", username);
                intent.putExtra("projectIme", projectIme);
                intent.putExtra("projectOpis", projectOpis);
                intent.putExtra("projectID", projectID);
                startActivity(intent);
            } else {
                TaskNewActivity.this.runOnUiThread(() -> {
                    Toast.makeText(TaskNewActivity.this, "Ups, nekaj je šlo narobe \n Response code: " + responseCode, Toast.LENGTH_LONG).show();
                });
            }
        });

    }

    public void clearTask(View v) {
        taskNameInput.setText("");
        taskDescriptionInput.setText("");
        dateTimeDisplay.setText("");
    }
}
