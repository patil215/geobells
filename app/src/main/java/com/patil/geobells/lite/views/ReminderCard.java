package com.patil.geobells.lite.views;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.patil.geobells.lite.R;
import com.patil.geobells.lite.data.Reminder;
import com.patil.geobells.lite.utils.Constants;
import com.patil.geobells.lite.utils.GeobellsUtils;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;

public class ReminderCard extends Card {
    TextView titleBox;
    TextView locationBox;
    TextView dateBox;
    ImageView colorStripe;
    String title;
    String location;
    String date;
    boolean completed;
    int positionInList;
    ArrayList<String> additionalInfo; // Days to trigger on, settings to change, etc

    public ReminderCard(Context context, int innerLayout, Reminder reminder, int positionInList) {
        super(context, innerLayout);
        getTextFromReminder(reminder);
        this.positionInList = positionInList;
    }

    public void getTextFromReminder(Reminder reminder) {
        title = reminder.title;
        completed = reminder.completed;
        if(completed) {
            date = GeobellsUtils.getRelativeTime(reminder.timeCompleted);
        } else {
            date = GeobellsUtils.getRelativeTime(reminder.timeCreated); // TODO make relative time
        }
        if(reminder.type == Constants.TYPE_FIXED) {
            location = reminder.address;
        } else if(reminder.type == Constants.TYPE_DYNAMIC) {
            location = reminder.business;
        }
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        titleBox = (TextView) view.findViewById(R.id.text_title);
        locationBox = (TextView) view.findViewById(R.id.text_location);
        dateBox = (TextView) view.findViewById(R.id.text_date);
        titleBox.setText(title);
        locationBox.setText("At " + location);
        if(completed) {
            dateBox.setText("Completed " + date);
        } else {
            dateBox.setText("Created " + date);
        }
        colorStripe = (ImageView) view.findViewById(R.id.view_colorbar);
        String color = Constants.COLORS[positionInList % Constants.COLORS.length];
        colorStripe.setBackgroundColor(Color.parseColor(color));
    }
}