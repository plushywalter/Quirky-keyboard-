package com.quirkboard.ime;

import android.inputmethodservice.InputMethodService;
import android.view.*;
import android.view.inputmethod.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.widget.*;
import android.content.*;
import java.util.*;

public class QuirkInputMethodService extends InputMethodService {
    LinearLayout keyboard;
    StringBuilder originalWord=new StringBuilder();
    boolean shift=false;
    SharedPreferences prefs;

    final String[][] ROWS={{"q","w","e","r","t","y","u","i","o","p"},
                           {"a","s","d","f","g","h","j","k","l"},
                           {"SHIFT","z","x","c","v","b","n","m","BACK"},
                           {"SPACE","ENTER"}};

    @Override public View onCreateInputView(){
        prefs=getSharedPreferences("quirk",0);
        buildKeyboard();
        return keyboard;
    }

    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}

    void buildKeyboard(){
        keyboard=new LinearLayout(this); keyboard.setOrientation(LinearLayout.VERTICAL);
        keyboard.setPadding(dp(4),dp(4),dp(4),dp(6)); keyboard.setBackgroundColor(Color.rgb(9,9,9));
        for(String[] row:ROWS){
            LinearLayout r=new LinearLayout(this); r.setGravity(Gravity.CENTER);
            for(String key:row){
                Button b=new Button(this); b.setText(displayKey(key)); b.setTextSize(15); b.setTextColor(Color.WHITE);
                b.setAllCaps(false); b.setPadding(0,0,0,0);
                GradientDrawable bg=new GradientDrawable(); bg.setColor(isSwapKey(key)?Color.rgb(255,45,8):Color.rgb(28,28,28)); bg.setStroke(dp(1),Color.WHITE); b.setBackground(bg);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(key.equals("SPACE")?58:55), weight(key));
                lp.setMargins(dp(2),dp(2),dp(2),dp(2)); r.addView(b,lp);
                b.setOnClickListener(v->press(key));
            }
            keyboard.addView(r);
        }
    }

    float weight(String k){
        if(k.equals("SPACE"))return 5f;
        if(k.equals("SHIFT")||k.equals("BACK")||k.equals("ENTER"))return 1.6f;
        return 1f;
    }

    boolean isSwapKey(String k){
        if(k.length()!=1)return false;
        for(Swap s:swaps())if(s.from.equalsIgnoreCase(k))return true;
        return false;
    }

    String displayKey(String k){
        if(k.equals("SPACE"))return "space";
        if(k.equals("BACK"))return "⌫";
        if(k.equals("SHIFT"))return "⇧";
        if(k.equals("ENTER"))return "↵";
        for(Swap s:swaps())if(s.from.equalsIgnoreCase(k))return k+" → "+s.to;
        return shift?k.toUpperCase():k;
    }

    void press(String key){
        InputConnection ic=getCurrentInputConnection(); if(ic==null)return;
        if(key.equals("SHIFT")){shift=!shift;buildKeyboard();return;}
        if(key.equals("BACK")){backspace(ic);return;}
        if(key.equals("SPACE")){finishWord(ic," ");return;}
        if(key.equals("ENTER")){finishWord(ic,"");ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER));ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ENTER));return;}
        String c=shift?key.toUpperCase():key;
        originalWord.append(c);
        ic.setComposingText(applySwaps(originalWord.toString()),1);
    }

    void backspace(InputConnection ic){
        if(originalWord.length()>0){
            originalWord.deleteCharAt(originalWord.length()-1);
            if(originalWord.length()==0)ic.finishComposingText();
            else ic.setComposingText(applySwaps(originalWord.toString()),1);
        } else ic.deleteSurroundingText(1,0);
    }

    void finishWord(InputConnection ic,String suffix){
        if(originalWord.length()==0){ if(!suffix.isEmpty())ic.commitText(suffix,1); return; }
        String corrected=autocorrect(originalWord.toString());
        ic.finishComposingText();
        ic.commitText(applySwaps(corrected)+suffix,1);
        originalWord.setLength(0);
        if(shift){shift=false;buildKeyboard();}
    }

    static class Swap {String from,to; Swap(String f,String t){from=f;to=t;}}

    List<Swap> swaps(){
        ArrayList<Swap> out=new ArrayList<>();
        int n=prefs==null?2:prefs.getInt("count",2);
        for(int i=0;i<n;i++){
            String f=prefs.getString("from"+i,i==0?"e":"s");
            String t=prefs.getString("to"+i,i==0?"&":"£");
            if(f!=null&&!f.isEmpty()&&t!=null&&!t.isEmpty())out.add(new Swap(f,t));
        }
        return out;
    }

    String applySwaps(String text){
        String out=text;
        for(Swap s:swaps())out=out.replaceAll("(?i)"+java.util.regex.Pattern.quote(s.from), java.util.regex.Matcher.quoteReplacement(s.to));
        return out;
    }

    String reverseSwaps(String text){
        String out=text;
        List<Swap> ss=swaps();
        Collections.sort(ss,(a,b)->Integer.compare(b.to.length(),a.to.length()));
        for(Swap s:ss)out=out.replaceAll("(?i)"+java.util.regex.Pattern.quote(s.to),java.util.regex.Matcher.quoteReplacement(s.from));
        return out;
    }

    String autocorrect(String raw){
        // The key behavior: undo the quirk replacements, correct the normal word,
        // then apply the replacements again.
        String normal=reverseSwaps(raw);
        String low=normal.toLowerCase(Locale.US);
        String best=Dictionary.best(low);
        if(best==null)return normal;
        if(normal.length()>0&&Character.isUpperCase(normal.charAt(0)))
            best=best.substring(0,1).toUpperCase()+best.substring(1);
        return best;
    }

    static class Dictionary {
        static final String WORDS="a about after again all also am an and any are around as at away back be because been before being best better big but by can come could day did do does doing down each even every few find first for from get give go good got great had has have he her here him his how i if in into is it its just keep know last like little long look made make many may me more most much my new no not now of on once one only or other our out over people please put really right said same say see she should so some something still take than that the their them then there these they thing think this those through time to too two up us use very want was way we well were what when where which while who will with without would write yes you your hello help home world test quirk keyboard autocorrect letter letters word words type typing save message love hi hey thanks thank youre your";
        static String best(String w){
            String[] words=WORDS.split(" ");
            if(Arrays.asList(words).contains(w))return w;
            if(w.length()<2||w.length()>20)return null;
            String best=null;int bd=99;
            for(String x:words){if(Math.abs(x.length()-w.length())>2)continue;int d=lev(w,x);if(d<bd){bd=d;best=x;}}
            int limit=w.length()<=4?1:(w.length()<=8?2:3);
            return bd<=limit?best:null;
        }
        static int lev(String a,String b){
            int[] p=new int[b.length()+1];for(int j=0;j<=b.length();j++)p[j]=j;
            for(int i=1;i<=a.length();i++){int[] c=new int[b.length()+1];c[0]=i;for(int j=1;j<=b.length();j++)c[j]=Math.min(Math.min(c[j-1]+1,p[j]+1),p[j-1]+(a.charAt(i-1)==b.charAt(j-1)?0:1));p=c;}return p[b.length()];
        }
    }
}
