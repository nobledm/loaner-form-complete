package ca.noble.loanerform;

import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends BaseActivity implements View.OnClickListener
{
    static final String TAG = "MainActivity";

    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView headshot;
    String imagePath, sigPath;

    public static LinearLayout sigCanvas;
    private DrawSignature mSig;
    View view;

    static String coordName;
    static String coordPhone;
    EditText dateText;
    boolean isMember;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assign Defaults/Prefs
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        coordName = prefs.getString(
                getResources().getString(R.string.prefkey_name),
                getResources().getString(R.string.prefdefault_name)
        );
        coordPhone = prefs.getString(
                getResources().getString(R.string.prefkey_phone),
                getResources().getString(R.string.prefdefault_phone)
        );

        etCoordName = findViewById(R.id.et_coord_name);
        etCoordPhone = findViewById(R.id.et_coord_phone);
        etCoordName.setText(coordName);
        etCoordPhone.setText(coordPhone);

        prefs.registerOnSharedPreferenceChangeListener(this);

        isMember = false;

        // Setup Canvas
        sigCanvas = findViewById(R.id.canvas_signature);
        mSig = new DrawSignature(getApplicationContext(), null);
        mSig.setBackgroundColor(Color.WHITE);

        sigCanvas.addView(mSig, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view = sigCanvas;

        // Prepare Buttons
        Button btnClearSig = findViewById(R.id.btn_clearSignature);
        btnClearSig.setOnClickListener(this);

        Button btnSave = findViewById(R.id.form_save);
        btnSave.setOnClickListener(this);

        Button btnTakePic = findViewById(R.id.btn_takePic);
        btnTakePic.setOnClickListener(this);

        Log.d(TAG, "end of create");
    }

    public void checkboxClicked(View v) {
        boolean checked = ((CheckBox) v).isChecked();
        TextView tvDeposit = findViewById(R.id.tv_deposit);

        if (checked)
        {
            tvDeposit.setText(R.string.form_deposit0);
            isMember = true;
        }
        else
        {
            tvDeposit.setText(R.string.form_deposit40);
            isMember = false;
        }
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btn_takePic:
            {
                Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // Check that we have access to the Camera activity to handle the intent
                if (takePicIntent.resolveActivity(getPackageManager()) != null)
                {
                    File imageFile = null;
                    try { imageFile = createImageFile(REQUEST_IMAGE_CAPTURE); }
                    catch (IOException e) { Log.d(TAG, "Error: " + e); }

                    // Confirm an image was made then prepare to set in view
                    if (imageFile != null) {
                        Uri imageURI = FileProvider.getUriForFile(
                                this,
                                "ca.noble.android.fileprovider",
                                imageFile
                        );

                        takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
                        startActivityForResult(takePicIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
                break;
            }
            case R.id.btn_clearSignature:
            {
                mSig.clear();
                break;
            }
            case R.id.form_save:
            {
                File sigFile = null;
                try
                {
                    sigFile = createImageFile(0);

                    // A canvas was saved?
                    if (sigFile != null) {
                        mSig.save(view, sigPath);

                        sendForm();
                    }
                }
                catch (Exception e) { Log.d(TAG, "problem with sig save"); }

                break;
            }
        }
    }

    private File createImageFile(int reqCode) throws IOException
    {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).format(new Date());
        String fileName = "JPG_" + timeStamp + "_";
        File file = File.createTempFile(fileName, ".jpg", storageDir);

        if (reqCode == REQUEST_IMAGE_CAPTURE)
            imagePath = file.getAbsolutePath();
        else
            sigPath = file.getAbsolutePath();

        return file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            headshot = findViewById(R.id.headshot);
            Bitmap image = BitmapFactory.decodeFile(imagePath);
            headshot.setImageBitmap(image);
        }
    }

    final Calendar mCalendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
            dateText.setText(dateFormat.format(mCalendar.getTime()));

            if (dateText.getId() == R.id.et_fromDate)
            {
                mCalendar.add(Calendar.MONTH, 1);

                EditText toDate = findViewById(R.id.et_toDate);
                toDate.setText(dateFormat.format(mCalendar.getTime()));
            }
        }
    };

    public void setDate(View v)
    {
        dateText = (EditText) v;

        new DatePickerDialog(MainActivity.this, date,
                mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    public void openTerms(View v)
    {
        AlertDialog terms = new AlertDialog.Builder(this)
                .setTitle("Rental Agreement")
                .setMessage("I have inspected the above equipment and it appears to be in good working order. " +
                        "I accept full responsibility for the safe keeping of this equipment, " +
                        "and I will make every effort to return it in good working order to the RASC " +
                        "telescope loaner program coordinator at Affordable Storage on or before the return date, " +
                        "or at some other mutually agreed upon time and place. " +
                        "Should there be significant delays (15 days) return the equipment due to my own neglect, " +
                        "I understand that the RASC executive will consider the equipment lost. \n\n" +
                        "If the equipment is lost, damaged, or stolen, I agree to forfeit my deposit and " +
                        "further reimburse the RASC-Edmonton Centre the replacement value of the equipment (given above) " +
                        "or as determined by the RASC-Edmonton Executive if no estimate is provided in this agreement")
                .setPositiveButton("I Agree", null)
                .create();
        terms.show();
    }

    private void sendForm()
    {
        String coordEmail = prefs.getString(
                getResources().getString(R.string.prefkey_email),
                getResources().getString(R.string.prefdefault_email)
        );

        // Get form inputs
        EditText etScope = findViewById(R.id.et_scope_name);
        EditText etEq = findViewById(R.id.et_other_equipment);
        EditText etLendStart = findViewById(R.id.et_fromDate);
        EditText etLendEnd = findViewById(R.id.et_toDate);
        EditText etBorrowerName = findViewById(R.id.et_borrower_name);
        EditText etBorrowerPhone = findViewById(R.id.et_borrower_phone);
        EditText etBorrowerEmail = findViewById(R.id.et_borrower_email);

        String scopeName = etScope.getText().toString();
        String equipment = etEq.getText().toString();
        String lendStart = etLendStart.getText().toString();
        String returnDate = etLendEnd.getText().toString();
        String borrowerName = etBorrowerName.getText().toString();
        String borrowerPhone = etBorrowerPhone.getText().toString();
        String borrowerEmail = etBorrowerEmail.getText().toString();

        String member = isMember ? "Is an RASC Member" : "Is not a member of the RASC";

        if (scopeName.length() > 0
                && lendStart.length() > 0
                && returnDate.length() > 0
                && borrowerName.length() > 0
                && borrowerPhone.length() > 0)
        {
            StringBuilder formMessage = new StringBuilder()
                    .append("Coordinator: " + coordName + "   (" + coordPhone + ")\n\n")
                    .append("Equipment Borrowed: " + "\n")
                    .append(scopeName + "\n")
                    .append(equipment + "\n\n")
                    .append("Lending Period: " + lendStart + " - " + returnDate + "\n\n")
                    .append("Borrower Information:" + "\n")
                    .append(member + "\n")
                    .append("Name: " + borrowerName + "\n")
                    .append("Phone: " + borrowerPhone + "\n")
                    .append("Email: " + borrowerEmail + "\n");

            // Setup Email Intent
            final String SUBJECT = "Loaner Form - ";

            Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{coordEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, SUBJECT + borrowerName);

            if (imagePath != null) {
                ArrayList<Uri> files = new ArrayList<>();
                files.add(Uri.fromFile(new File(imagePath)));
                files.add(Uri.fromFile(new File(sigPath)));

                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                emailIntent.putExtra(Intent.EXTRA_TEXT, formMessage.toString());
                this.startActivity(Intent.createChooser(emailIntent, "Sending email..."));
            } else {
                Toast.makeText(this, "Need a photo", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this,
                    "Please fill all necessary fields: Telescope, Lending Period, Borrower Name & Number",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
