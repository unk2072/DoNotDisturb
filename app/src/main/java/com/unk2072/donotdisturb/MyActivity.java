package com.unk2072.donotdisturb;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

public class MyActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MyActivity";
    private String[] mListText = new String[3];
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        initListView();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return false;
    }

    private boolean initListView() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean run_flag = pref.getBoolean(Const.RUN_FLAG, false);

        mListText[0] = run_flag ? getString(R.string.list1_1) : getString(R.string.list1_0);
        mListText[1] = getString(R.string.list2_0, pref.getInt(Const.ON_HOUR, 22), pref.getInt(Const.ON_MINUTE, 0));
        mListText[2] = getString(R.string.list3_0, pref.getInt(Const.OFF_HOUR, 6), pref.getInt(Const.OFF_MINUTE, 0));

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mListText);
        ListView listView = (ListView) findViewById(R.id.listView);
        TextView textView = new TextView(this);
        textView.setText(R.string.list0_0);
        listView.addHeaderView(textView, null, false);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(this);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final String DIALOG = "dialog";
        switch (i) {
            case 0:
                break;
            case 1:
                doToggleService();
                break;
            case 2:
                new SettingDialog1().show(getSupportFragmentManager(), DIALOG);
                break;
            case 3:
                new SettingDialog2().show(getSupportFragmentManager(), DIALOG);
                break;
        }
    }

    private boolean doToggleService() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = pref.edit();

        boolean run_flag = !pref.getBoolean(Const.RUN_FLAG, false);
        edit.putBoolean(Const.RUN_FLAG, run_flag);
        edit.apply();
        mListText[0] = run_flag ? getString(R.string.list1_1) : getString(R.string.list1_0);
        mAdapter.notifyDataSetChanged();

        Intent i = new Intent(this, MyService.class);
        i.putExtra(Const.RUN_MODE, Const.MODE_START);
        startService(i);
        return true;
    }

    public static class SettingDialog1 extends DialogFragment implements DialogInterface.OnClickListener {
        private TimePicker mTimePicker;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.time_title);
            mTimePicker = new TimePicker(getActivity());
            if (savedInstanceState != null){
                mTimePicker.setCurrentHour(savedInstanceState.getInt(Const.ON_HOUR));
                mTimePicker.setCurrentMinute(savedInstanceState.getInt(Const.ON_MINUTE));
            } else {
                mTimePicker.setCurrentHour(pref.getInt(Const.ON_HOUR, 22));
                mTimePicker.setCurrentMinute(pref.getInt(Const.ON_MINUTE, 0));
            }
            mTimePicker.setIs24HourView(DateFormat.is24HourFormat(getActivity()));
            mTimePicker.setSaveFromParentEnabled(false);
            mTimePicker.setSaveEnabled(true);
            builder.setView(mTimePicker);
            builder.setPositiveButton(R.string.time_done, this);
            return builder.create();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            if (mTimePicker != null){
                outState.putInt(Const.ON_HOUR, mTimePicker.getCurrentHour());
                outState.putInt(Const.ON_MINUTE, mTimePicker.getCurrentMinute());
            }
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int hour = mTimePicker.getCurrentHour();
            int minute = mTimePicker.getCurrentMinute();
            SharedPreferences.Editor edit = pref.edit();
            edit.putInt(Const.ON_HOUR, hour);
            edit.putInt(Const.ON_MINUTE, minute);
            edit.apply();

            MyActivity my = (MyActivity)getActivity();
            my.mListText[1] = getString(R.string.list2_0, hour, minute);
            my.mAdapter.notifyDataSetChanged();
            my.refreshSetting();
        }
    }

    public static class SettingDialog2 extends DialogFragment implements DialogInterface.OnClickListener {
        private TimePicker mTimePicker;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.time_title);
            mTimePicker = new TimePicker(getActivity());
            if (savedInstanceState != null){
                mTimePicker.setCurrentHour(savedInstanceState.getInt(Const.OFF_HOUR));
                mTimePicker.setCurrentMinute(savedInstanceState.getInt(Const.OFF_MINUTE));
            } else {
                mTimePicker.setCurrentHour(pref.getInt(Const.OFF_HOUR, 6));
                mTimePicker.setCurrentMinute(pref.getInt(Const.OFF_MINUTE, 0));
            }
            mTimePicker.setIs24HourView(DateFormat.is24HourFormat(getActivity()));
            mTimePicker.setSaveFromParentEnabled(false);
            mTimePicker.setSaveEnabled(true);
            builder.setView(mTimePicker);
            builder.setPositiveButton(R.string.time_done, this);
            return builder.create();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            if (mTimePicker != null){
                outState.putInt(Const.OFF_HOUR, mTimePicker.getCurrentHour());
                outState.putInt(Const.OFF_MINUTE, mTimePicker.getCurrentMinute());
            }
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int hour = mTimePicker.getCurrentHour();
            int minute = mTimePicker.getCurrentMinute();
            SharedPreferences.Editor edit = pref.edit();
            edit.putInt(Const.OFF_HOUR, hour);
            edit.putInt(Const.OFF_MINUTE, minute);
            edit.apply();

            MyActivity my = (MyActivity)getActivity();
            my.mListText[2] = getString(R.string.list3_0, hour, minute);
            my.mAdapter.notifyDataSetChanged();
            my.refreshSetting();
        }
    }

    private boolean refreshSetting() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean run_flag = pref.getBoolean(Const.RUN_FLAG, false);

        if (run_flag) {
            Intent i = new Intent(this, MyService.class);
            i.putExtra(Const.RUN_MODE, Const.MODE_START);
            startService(i);
        }
        return true;
    }
}
