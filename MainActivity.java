package com.quirkboard.ime;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.provider.Settings;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout list;
    SharedPreferences prefs;

    int dp(float x){ return (int)(x*getResources().getDisplayMetrics().density + .5f); }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences("quirk",0);
        build();
    }

    TextView title(String s){
        TextView t=new TextView(this); t.setText(s); t.setTextColor(Color.WHITE);
        t.setTextSize(24); t.setPadding(dp(18),dp(25),dp(18),dp(10)); return t;
    }

    public void build(){
        ScrollView scroll=new ScrollView(this);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(9,9,9)); scroll.addView(root);

        root.addView(title("QUIRKBOARD"));

        TextView info=new TextView(this);
        info.setText("Configure replacements. Example: e → & and s → £\\n\\nThe keyboard autocorrects the ORIGINAL spelling, then applies your replacements.\\nExample: type h&llo → corrected internally to hello → sent as h&llo.");
        info.setTextColor(Color.LTGRAY); info.setTextSize(15); info.setPadding(dp(18),0,dp(18),dp(15)); root.addView(info);

        Button enable=new Button(this); enable.setText("ENABLE / CHOOSE QUIRKBOARD KEYBOARD");
        enable.setOnClickListener(v -> {
            try { startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)); }
            catch(Exception e){ ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker(); }
        });
        root.addView(enable);

        list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        root.addView(list);

        Button add=new Button(this); add.setText("+ ADD NEW SWAP");
        add.setOnClickListener(v -> addRow("", ""));
        root.addView(add);

        setContentView(scroll);
        loadRows();
    }

    void loadRows(){
        list.removeAllViews();
        int n=prefs.getInt("count",2);
        for(int i=0;i<n;i++) addRow(prefs.getString("from"+i, i==0?"e":"s"), prefs.getString("to"+i, i==0?"&":"£"));
    }

    void addRow(String f,String t){
        LinearLayout row=new LinearLayout(this); row.setPadding(dp(14),dp(6),dp(14),dp(6));
        EditText from=new EditText(this); from.setHint("from"); from.setText(f); from.setTextColor(Color.WHITE); from.setHintTextColor(Color.GRAY);
        EditText to=new EditText(this); to.setHint("replacement"); to.setText(t); to.setTextColor(Color.WHITE); to.setHintTextColor(Color.GRAY);
        row.addView(from,new LinearLayout.LayoutParams(0,dp(55),1));
        row.addView(to,new LinearLayout.LayoutParams(0,dp(55),1));
        list.addView(row);
    }

    @Override protected void onPause(){ super.onPause(); saveRows(); }

    void saveRows(){
        if(list==null)return;
        SharedPreferences.Editor e=prefs.edit();
        int count=list.getChildCount(); e.putInt("count",count);
        for(int i=0;i<count;i++){
            View v=list.getChildAt(i);
            if(!(v instanceof LinearLayout))continue;
            LinearLayout r=(LinearLayout)v;
            EditText f=(EditText)r.getChildAt(0), t=(EditText)r.getChildAt(1);
            e.putString("from"+i,f.getText().toString());
            e.putString("to"+i,t.getText().toString());
        }
        e.apply();
    }
}
