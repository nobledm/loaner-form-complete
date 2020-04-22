package ca.noble.loanerform;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final String TAG = "BaseActivity";
    SharedPreferences prefs;
    EditText etCoordName;
    EditText etCoordPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.menu_settings:
            {
                startActivity(new Intent(this, PrefsActivity.class));
                break;
            }
        }

        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        String coordName = prefs.getString(
                getResources().getString(R.string.prefkey_name),
                getResources().getString(R.string.prefdefault_name)
        );
        String coordPhone = prefs.getString(
                getResources().getString(R.string.prefkey_phone),
                getResources().getString(R.string.prefdefault_phone)
        );

        etCoordName.setText(coordName);
        etCoordPhone.setText(coordPhone);
    }
}
