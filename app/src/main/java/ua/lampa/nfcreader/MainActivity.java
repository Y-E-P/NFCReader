package ua.lampa.nfcreader;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ua.lampa.nfcreader.factory.NDEFRecordFactory;
import ua.lampa.nfcreader.model.BaseRecord;
import ua.lampa.nfcreader.model.RDTSpRecord;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdpt;
    PendingIntent nfcPendingIntent;
    IntentFilter[] intentFiltersArray;

    private TextView recNumberTxt;
    private ListView lView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        lView = (ListView) findViewById(R.id.recList);

        recNumberTxt = (TextView) findViewById(R.id.recNumber);

        nfcAdpt = NfcAdapter.getDefaultAdapter(this);

        // Check if the smartphone has NFC
        if (nfcAdpt == null) {
            Toast.makeText(this, "NFC not supported", Toast.LENGTH_LONG).show();
            finish();
        }else {
            // Check if NFC is enabled
            if (!nfcAdpt.isEnabled()) {
                Toast.makeText(this, "Enable NFC before using the app", Toast.LENGTH_LONG).show();
            }
        }



        Intent nfcIntent = new Intent(this, getClass());
        nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        nfcPendingIntent =
                PendingIntent.getActivity(this, 0, nfcIntent, 0);

        // Create an Intent Filter limited to the URI or MIME type to
        // intercept TAG scans from.
        IntentFilter tagIntentFilter =
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            //tagIntentFilter.addDataScheme("http");
            // tagIntentFilter.addDataScheme("vnd.android.nfc");
            tagIntentFilter.addDataScheme("tel");
            //tagIntentFilter.addDataType("text/plain");
            intentFiltersArray = new IntentFilter[]{tagIntentFilter};
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d("Nfc", "New intent");
        getTag(intent);
    }

    private void handleIntent(Intent i) {

        Log.d("NFC", "Intent [" + i + "]");

        getTag(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdpt.enableForegroundDispatch(
                this,
                // Intent that will be used to package the Tag Intent.
                nfcPendingIntent,
                // Array of Intent Filters used to declare the Intents you
                // wish to intercept.
                intentFiltersArray,
                // Array of Tag technologies you wish to handle.
                null);
        handleIntent(getIntent());
    }


    @Override
    protected void onPause() {
        super.onPause();
        nfcAdpt.disableForegroundDispatch(this);
    }




    private void getTag(Intent i) {
        if (i == null)
            return ;

        String type = i.getType();
        String action = i.getAction();
        List<BaseRecord> dataList = new ArrayList<BaseRecord>();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Log.d("Nfc", "Action NDEF Found");
            Parcelable[] parcs = i.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            // List record


            for (Parcelable p : parcs) {
                NdefMessage msg = (NdefMessage) p;
                final int numRec = msg.getRecords().length;
                recNumberTxt.setText(String.valueOf(numRec));

                NdefRecord[] records = msg.getRecords();
                for (NdefRecord record: records) {
                    BaseRecord result = NDEFRecordFactory.createRecord(record);
                    if (result instanceof RDTSpRecord)
                        dataList.addAll( ((RDTSpRecord) result).records);
                    else
                        dataList.add(result);

                }
            }

            NdefAdapter adpt = new NdefAdapter(dataList);
            lView.setAdapter(adpt);
        }

    }





    // ListView adapter
    class NdefAdapter extends ArrayAdapter<BaseRecord> {
        List<BaseRecord> recordList;

        public NdefAdapter(List<BaseRecord> recordList) {
            super(MainActivity.this, R.layout.record_layout, recordList);
            this.recordList = recordList;
        }

        @Override
        public int getCount() {
            return recordList.size();
        }

        @Override
        public BaseRecord getItem(int position) {
            return recordList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            Log.d("Nfc","Get VIew");
            if (v == null) {
                LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inf.inflate(R.layout.record_layout, null);
            }

            TextView tnfTxt = (TextView) v.findViewById(R.id.tnfText);
            TextView recContentTxT = (TextView) v.findViewById(R.id.recCont);
            TextView typeTxt = (TextView) v.findViewById(R.id.typeTxt);
            TextView headTxt = (TextView) v.findViewById(R.id.header);

            BaseRecord record = recordList.get(position);
            tnfTxt.setText("" + record.tnf);
            headTxt.setText("MB:" + record.MB + " ME:" + record.ME + " SR:" + record.SR);

            recContentTxT.setText(record.toString());

            return v;

        }
        @Override
        public long getItemId(int position) {
            return recordList.get(position).hashCode();
        }
    }

}
