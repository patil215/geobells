package com.patil.geobells.lite.views;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.patil.geobells.lite.R;
import com.patil.geobells.lite.asynctask.DownloadImageTask;
import com.patil.geobells.lite.data.Place;
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
    ImageView mapImage;
    String title;
    String location;
    String date;
    boolean completed;
    int positionInList;
    Reminder reminder;
    ArrayList<String> additionalInfo; // Days to trigger on, settings to change, etc
    Context context;

    public ReminderCard(Context context, int innerLayout, Reminder reminder, int positionInList) {
        super(context, innerLayout);
        this.context = context;
        getTextFromReminder(reminder);
        this.reminder = reminder;
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
        mapImage = (ImageView) view.findViewById(R.id.view_map);
        titleBox.setText(title);
        locationBox.setText(location);
        if(completed) {
            dateBox.setText("Completed " + date);
        } else {
            dateBox.setText("Created " + date);
        }
        colorStripe = (ImageView) view.findViewById(R.id.view_colorbar);
        String color = Constants.COLORS[positionInList % Constants.COLORS.length];
        ArrayList<LatLng> positions = new ArrayList<LatLng>();
        if(reminder.type == Constants.TYPE_FIXED) {
            positions.add(new LatLng(reminder.latitude, reminder.longitude));
            String url = GeobellsUtils.constructMapImageURL(positions, Color.parseColor(color));
            new DownloadImageTask(context, mapImage).execute(url);
        } else {
            for(Place place : reminder.places) {
                positions.add(new LatLng(place.latitude, place.longitude));
            }
            String url = GeobellsUtils.constructMapImageURL(positions, Color.parseColor(color));
            new DownloadImageTask(context, mapImage).execute(url);
        }
        colorStripe.setBackgroundColor(Color.parseColor(color));
    }
}
