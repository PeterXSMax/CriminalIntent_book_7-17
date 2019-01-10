package com.example.peter.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment {
    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleFile;
    private Button mDateButton;
    private CheckBox mCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";//定义一个键


    private static final int REQUEST_DATE = 0;//定义一个返回这个目标fragment的键
    private static final int REQUEST_CONTECT =1;
    private static final int REQUEST_PHOTO = 2;

    public static CrimeFragment newInstance(UUID crimeID){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeID);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //mCrime = new Crime();
        //UUID crimeId = (UUID) getActivity().getIntent().getSerializableExtra(CrimeActivity.EXTRA_CRIME_ID);//获取跳转过来的传值
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);//生成Crime对象
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

    @Override
    public void onPause(){
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_crime, container, false);//第三个参数是因为这个app的开发用的是activity控制fragment所以用false

        mTitleFile = (EditText)v.findViewById(R.id.crime_title);
        mTitleFile.setText(mCrime.getmTitle());//根据传值设置文字
        mTitleFile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //这里很一般不写东西
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setmTitle(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {
                //这个也是一般不太写东西
            }
        });

        mDateButton = (Button)v.findViewById(R.id.crime_date);
        updateDate();
        //mDateButton.setEnabled(false);//现在先禁用一下这个按钮 ，之后再做修改
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                //DatePickerFragment dialog = new DatePickerFragment();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getmDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });


        mCheckBox = (CheckBox)v.findViewById(R.id.crime_sloved);
        mCheckBox.setChecked(mCrime.ismSloved());//根据传值修改是否选中
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setmSloved(isChecked);
            }
        });

        mReportButton = (Button)v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);

            }
        });

        final Intent pickContect = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
//        pickContect.addCategory(Intent.CATEGORY_HOME); //测试无通讯录app
        mSuspectButton = (Button)v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContect, REQUEST_CONTECT);
            }
        });
        if (mCrime.getmSuspect() != null){
            mSuspectButton.setText(mCrime.getmSuspect());
        }

        //如果没有通讯录app的话，就使选择嫌疑人按钮不可操作
        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContect,
                PackageManager.MATCH_DEFAULT_ONLY) == null){
            mSuspectButton.setEnabled(false);
        }

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);


        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.fromFile(mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });
        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK){
            return;
        }

        if(requestCode == REQUEST_DATE){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setmDate(date);
            updateDate();
        }else if(requestCode == REQUEST_CONTECT && data != null){
            Uri contactUri = data.getData();
            String[] quertFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            Cursor c = getActivity().getContentResolver().
                    query(contactUri, quertFields, null,null,null);

            try {
                if(c.getCount() == 0){
                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setmSuspect(suspect);
                mSuspectButton.setText(suspect);
            }finally {
                c.close();
            }
        }else if(requestCode == REQUEST_PHOTO){
            updatePhotoView();
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getmDate().toString());
    }

    private String getCrimeReport(){
        String solvedString = null;
        if(mCrime.ismSloved()){
            solvedString = getString(R.string.crime_report_solved);
        }else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM, dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getmDate()).toString();

        String suspect = mCrime.getmSuspect();
        if(suspect == null){
            suspect = getString(R.string.crime_report_suspect);
        }else{
            suspect = getString(R.string.crime_report_no_suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getmTitle(), dateString, solvedString, suspect);

        return report;
    }

    public void updatePhotoView(){
        if(mPhotoFile == null || !mPhotoFile.exists()){
            mPhotoView.setImageDrawable(null);
        }else{
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(),getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

}
