package com.patil.geobells.lite.views;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.patil.geobells.lite.R;
import com.patil.geobells.lite.data.DynamicReminder;
import com.patil.geobells.lite.data.FixedReminder;
import com.patil.geobells.lite.data.Reminder;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;

public class ReminderCard extends Card {
    TextView titleBox;
    TextView locationBox;
    TextView dateBox;
    String title;
    String location;
    String date;
    ArrayList<String> additionalInfo; // Days to trigger on, settings to change, etc

    public ReminderCard(Context context, int innerLayout, Reminder reminder) {
        super(context, innerLayout);
        getTextFromReminder(reminder);
    }

    public void getTextFromReminder(Reminder reminder) {
        title = reminder.title;
        date = String.valueOf(reminder.timeCreated); // TODO make relative time

        if(reminder instanceof FixedReminder) {
            FixedReminder castedReminder = (FixedReminder) reminder;
            location = castedReminder.address;
            Log.d("GeobellsCards", "creating fixed reminder");
        } else if(reminder instanceof DynamicReminder) {
            DynamicReminder castedReminder = (DynamicReminder) reminder;
            location = ((DynamicReminder) castedReminder).business;
            Log.d("GeobellsCards", "creating dynamic reminder");
        }
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        titleBox = (TextView) view.findViewById(R.id.text_title);
        locationBox = (TextView) view.findViewById(R.id.text_location);
        dateBox = (TextView) view.findViewById(R.id.text_date);
        titleBox.setText(title);
        locationBox.setText(location);
        dateBox.setText(date);
    }
}
