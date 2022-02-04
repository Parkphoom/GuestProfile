package com.example.guestprofile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;


import com.example.guestprofile.Data.DataHolder;
import com.example.guestprofile.Data.DetailInfo;
import com.tejpratapsingh.pdfcreator.activity.PDFCreatorActivity;
import com.tejpratapsingh.pdfcreator.utils.PDFUtil;
import com.tejpratapsingh.pdfcreator.views.PDFBody;
import com.tejpratapsingh.pdfcreator.views.PDFFooterView;
import com.tejpratapsingh.pdfcreator.views.PDFHeaderView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFHorizontalView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFImageView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFLineSeparatorView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFTextView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFVerticalView;
import com.wacinfo.wacextrathaiid.Data.NativeCardInfo;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class PdfCreatorExampleActivity extends PDFCreatorActivity {
    NativeCardInfo cardInfo = DataHolder.getCardInfo();
    DetailInfo detailInfo = DataHolder.getDetailInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        createPDF("test", new PDFUtil.PDFUtilListener() {
            @Override
            public void pdfGenerationSuccess(File savedPDFFile) {
                Toast.makeText(PdfCreatorExampleActivity.this, "PDF Created", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void pdfGenerationFailure(Exception exception) {
                Toast.makeText(PdfCreatorExampleActivity.this, "PDF NOT Created", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected PDFHeaderView getHeaderView(int pageIndex) {
        PDFHeaderView headerView = new PDFHeaderView(getApplicationContext());

        PDFVerticalView verticalView = new PDFVerticalView(getApplicationContext());

        PDFImageView imageView = new PDFImageView(getApplicationContext());
        LinearLayout.LayoutParams imageLayoutParam = new LinearLayout.LayoutParams(
                60,
                60, 0);
        imageView.setImageScale(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageResource(R.drawable.waclogo);
//        imageLayoutParam.setMargins(0, 0, 10, 0);
        imageLayoutParam.gravity = Gravity.CENTER;
        imageView.setLayout(imageLayoutParam);

        verticalView.addView(imageView);

        PDFTextView pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
        SpannableString word = new SpannableString("บัตรทะเบียนผู้พักโรงแรม.....................................................");
        word.setSpan(new ForegroundColorSpan(Color.BLACK), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        pdfTextView.setText(word);
        LinearLayout.LayoutParams textlayputparam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0);
        textlayputparam.gravity = Gravity.CENTER;
        pdfTextView.setLayout(new LinearLayout.LayoutParams(textlayputparam));
        pdfTextView.getView().setGravity(Gravity.CENTER_HORIZONTAL);
        pdfTextView.getView().setTypeface(pdfTextView.getView().getTypeface(), Typeface.BOLD);

        verticalView.addView(pdfTextView);

        headerView.addView(verticalView);

        PDFLineSeparatorView lineSeparatorView1 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        lineSeparatorView1.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                10, 0));
        headerView.addView(lineSeparatorView1);

        return headerView;
    }

    @Override
    protected PDFBody getBodyViews() {
        PDFBody pdfBody = new PDFBody();

        PDFTextView LodgerView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
        LodgerView.setText("(Lodger Registration Card)");
        LinearLayout.LayoutParams textlayputparam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0);
        textlayputparam.gravity = Gravity.CENTER;
        LodgerView.setLayout(new LinearLayout.LayoutParams(textlayputparam));
        pdfBody.addView(LodgerView);

        PDFLineSeparatorView lineSeparatorView1 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        pdfBody.addView(lineSeparatorView1);

        PDFHorizontalView fullnameView = new PDFHorizontalView(getApplicationContext());
        fullnameView.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));

        PDFHorizontalView nameView = new PDFHorizontalView(getApplicationContext());
        nameView.setLayout((new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1)));
        PDFTextView Textname = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("ชื่อตัว");
        Textname.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        nameView.addView(Textname);

        String fullname = cardInfo.getThaiTitle() + " " + cardInfo.getThaiFirstName() + " " + cardInfo.getThaiMiddleName();
        PDFTextView inputname = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText(fullname.replaceAll("-", ""));
        inputname.getView().setPadding(12, 0, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputname.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputname.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        inputname.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        nameView.addView(inputname);
        fullnameView.addView(nameView);

        PDFHorizontalView surnameView = new PDFHorizontalView(getApplicationContext());
        surnameView.setLayout((new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1)));
        PDFTextView Textsurname = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("ชื่อสกุล");
        Textsurname.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        surnameView.addView(Textsurname);

        PDFTextView inputsurname = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText(cardInfo.getThaiLastName());
        inputsurname.getView().setPadding(12, 0, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputsurname.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputsurname.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        inputsurname.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        surnameView.addView(inputsurname);

        fullnameView.addView(surnameView);

        pdfBody.addView(fullnameView);

        PDFLineSeparatorView lineSeparatorView2 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        pdfBody.addView(lineSeparatorView2);

        PDFHorizontalView engnameView = new PDFHorizontalView(getApplicationContext());
        engnameView.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));

        PDFTextView engname = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("(Name)");
        engname.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        PDFTextView engsurname = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("(Surname)");
        engsurname.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        engnameView.addView(engname);
        engnameView.addView(engsurname);

        pdfBody.addView(engnameView);

        PDFLineSeparatorView lineSeparatorView3 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        lineSeparatorView3.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5, 0));
        pdfBody.addView(lineSeparatorView3);


        PDFHorizontalView cidView = new PDFHorizontalView(getApplicationContext());
        engnameView.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));
        PDFTextView txtCID = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("เลขประจำตัวประชาชน");
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.setMarginEnd(12);
        txtCID.setLayout(params1);

        cidView.addView(txtCID);

        for (int i = 0; i < 13; i++) {
            PDFTextView cid = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                    .setText(String.valueOf(cardInfo.getCardNumber().charAt(i)));
            cid.setPadding(6, 3, 6, 3);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(3);
            cid.setLayout(params);
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                cid.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_line_black));
            } else {
                cid.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.border_line_black));
            }
            cidView.addView(cid);

            PDFTextView space = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                    .setText("-");
            space.setPadding(6, 3, 6, 3);
            space.setLayout(params);
            switch (i) {
                case 0:
                case 4:
                case 9:
                    cidView.addView(space);
                    break;
            }
        }
        pdfBody.addView(cidView);

        PDFTextView txtengCID = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("(Identification Card No.)");
        pdfBody.addView(txtengCID);

        PDFLineSeparatorView lineSeparatorView4 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        lineSeparatorView4.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5, 0));
        pdfBody.addView(lineSeparatorView4);

        PDFHorizontalView txtAlienView = new PDFHorizontalView(getApplicationContext());
        txtAlienView.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));
        PDFTextView txtAlien = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("ใบสำคัญประจำตัวคนต่างด้าวเลขที่");
        txtAlien.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        txtAlienView.addView(txtAlien);

        PDFTextView inputAlienNo = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL);
        inputAlienNo.getView().setPadding(12, 0, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputAlienNo.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputAlienNo.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        inputAlienNo.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        txtAlienView.addView(inputAlienNo);

        pdfBody.addView(txtAlienView);

        PDFTextView txtengAlien = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("(Alien Registration Book No.)");
        pdfBody.addView(txtengAlien);


        PDFLineSeparatorView lineSeparatorView5 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        lineSeparatorView5.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5, 0));
        pdfBody.addView(lineSeparatorView5);


        PDFHorizontalView passportView = new PDFHorizontalView(getApplicationContext());
        passportView.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));
        PDFTextView txtPassport = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("หนังสือเดินทางเลขที่");
        txtPassport.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        passportView.addView(txtPassport);

        PDFTextView inputPassport = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL);
        inputPassport.getView().setPadding(12, 0, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputPassport.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputPassport.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        inputPassport.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        passportView.addView(inputPassport);

        pdfBody.addView(passportView);

        PDFTextView txtengPassport = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("(Passport No.)");
        pdfBody.addView(txtengPassport);

        PDFLineSeparatorView lineSeparatorView6 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        lineSeparatorView6.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5, 0));
        pdfBody.addView(lineSeparatorView6);


        PDFHorizontalView detailView = new PDFHorizontalView(getApplicationContext());
        detailView.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));

        PDFHorizontalView occupationView = new PDFHorizontalView(getApplicationContext());
        occupationView.setLayout((new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1)));
        PDFTextView Textoccupation = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("อาชีพ");
        Textoccupation.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        occupationView.addView(Textoccupation);


        PDFTextView inputoccupation = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText(detailInfo.getOccupation());
        inputoccupation.getView().setPadding(12, 0, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputoccupation.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputoccupation.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        inputoccupation.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        occupationView.addView(inputoccupation);
        detailView.addView(occupationView);

        PDFHorizontalView nationnalityView = new PDFHorizontalView(getApplicationContext());
        nationnalityView.setLayout((new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1)));
        PDFTextView Textnationnality = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("สัญชาติ");
        Textnationnality.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        nationnalityView.addView(Textnationnality);

        PDFTextView inputnationnality = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText(cardInfo.getNationality());
        inputnationnality.getView().setPadding(12, 0, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputnationnality.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputnationnality.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        inputnationnality.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        nationnalityView.addView(inputnationnality);

        detailView.addView(nationnalityView);

        pdfBody.addView(detailView);

        PDFLineSeparatorView lineSeparatorView7 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        pdfBody.addView(lineSeparatorView7);

        PDFHorizontalView engdetailView = new PDFHorizontalView(getApplicationContext());
        engdetailView.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));

        PDFTextView engoccupation = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("(Occupation)");
        engoccupation.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        PDFTextView engnationality = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("(Nationality)");
        engnationality.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        engdetailView.addView(engoccupation);
        engdetailView.addView(engnationality);

        pdfBody.addView(engdetailView);

        PDFLineSeparatorView lineSeparatorView8 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        lineSeparatorView8.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5, 0));
        pdfBody.addView(lineSeparatorView8);

        PDFHorizontalView addressView = new PDFHorizontalView(getApplicationContext());
        addressView.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));
        PDFTextView txtAddress = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("ที่อยู่ปัจจุบัน");
        txtAddress.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        addressView.addView(txtAddress);

//        var address = CardAddress()
//        address.homeNo = enpart12
//        address.moo = enpart13
//        address.trok = enpart14
//        address.soi = enpart15
//        address.road = enpart16
//        address.subDistrict = enpart17
//        address.district = enpart18
//        address.province = enpart19
//        address.postalCode = zipcode
//        address.country = "ประเทศไทย"

        String address = Objects.requireNonNull(cardInfo.getAddress()).getHomeNo() + " " + cardInfo.getAddress().getMoo() + " " +
                cardInfo.getAddress().getTrok() + " " + cardInfo.getAddress().getSoi() + " " +
                cardInfo.getAddress().getRoad() + " " + cardInfo.getAddress().getSubDistrict() + " " +
                cardInfo.getAddress().getDistrict() + " " + cardInfo.getAddress().getProvince() + " " +
                cardInfo.getAddress().getPostalCode() ;

        PDFTextView inputAddress = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText(address.replace("-",""));
        inputAddress.getView().setPadding(12, 0, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputAddress.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputAddress.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        inputAddress.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        addressView.addView(inputAddress);

        pdfBody.addView(addressView);

        PDFTextView txtengAddress = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("(Current Address)");
        pdfBody.addView(txtengAddress);

        PDFLineSeparatorView lineSeparatorView9 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        lineSeparatorView9.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5, 0));
        pdfBody.addView(lineSeparatorView9);


        PDFHorizontalView detail2View = new PDFHorizontalView(getApplicationContext());
        detail2View.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));

        PDFHorizontalView address2View = new PDFHorizontalView(getApplicationContext());
        address2View.setLayout((new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1)));
        PDFTextView inputaddress2 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL);

        inputaddress2.getView().setPadding(12, 0, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputaddress2.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputaddress2.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        inputaddress2.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        address2View.addView(inputaddress2);
        detail2View.addView(address2View);

        PDFHorizontalView TelnoView = new PDFHorizontalView(getApplicationContext());
        TelnoView.setLayout((new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1)));
        PDFTextView Texttelno = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("หมายเลขโทรศัพท์");
        Texttelno.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        TelnoView.addView(Texttelno);

        PDFTextView inputTelno = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL);
        inputTelno.getView().setPadding(12, 0, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputTelno.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputTelno.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        inputTelno.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        TelnoView.addView(inputTelno);

        detail2View.addView(TelnoView);

        pdfBody.addView(detail2View);

        PDFLineSeparatorView lineSeparatorView10 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        pdfBody.addView(lineSeparatorView10);

        PDFHorizontalView detail3View = new PDFHorizontalView(getApplicationContext());
        detail3View.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));

        PDFTextView textnull = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("");
        textnull.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        PDFTextView telNoeng = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("(Telophone No.)");
        telNoeng.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        detail3View.addView(textnull);
        detail3View.addView(telNoeng);

        pdfBody.addView(detail3View);

        PDFTextView headerfrom = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("1.     เดินทางมาจากที่ใด");
        headerfrom.getView().setPadding(16, 0, 0, 0);
        pdfBody.addView(headerfrom);

        PDFTextView engheaderfrom = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("        (Place of Departure)");
        engheaderfrom.getView().setPadding(18, 0, 0, 0);
        pdfBody.addView(engheaderfrom);

        PDFHorizontalView fromdetail1View = new PDFHorizontalView(getApplicationContext());
        fromdetail1View.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));

        PDFTextView fromcheck1 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText(" ");
        fromcheck1.setPadding(10, 3, 10, 3);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(38);
        fromdetail1View.setLayout(params);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            fromcheck1.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_line_black));
        } else {
            fromcheck1.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.border_line_black));
        }

        PDFTextView fromdetail1 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("1.1 เดินทางมาจากที่อยู่ปัจจุบันที่เป็นภูมิลำเนาข้างต้น");
        fromdetail1.getView().setPadding(3, 0, 0, 0);
        fromdetail1.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));

        fromdetail1View.addView(fromcheck1);
        fromdetail1View.addView(fromdetail1);

        pdfBody.addView(fromdetail1View);

        PDFTextView fromdetail1eng = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("(Depart from the current address above)");
        fromdetail1eng.getView().setPadding(3, 0, 0, 0);
        LinearLayout.LayoutParams fromdetail1engparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        fromdetail1engparams.setMarginStart(80);
        fromdetail1eng.setLayout(fromdetail1engparams);
        pdfBody.addView(fromdetail1eng);

        PDFHorizontalView fromdetail2View = new PDFHorizontalView(getApplicationContext());
        fromdetail2View.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));

        PDFTextView fromcheck2 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText(" ");
        fromcheck2.setPadding(10, 3, 10, 3);
        LinearLayout.LayoutParams fromcheck2params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        fromcheck2params.setMarginStart(38);
        fromcheck2.setLayout(fromcheck2params);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            fromcheck2.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_line_black));
        } else {
            fromcheck2.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.border_line_black));
        }

        PDFTextView fromdetail2 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("1.2 เดินทางมาจากสถานที่พักอื่น(บ้านเลขที่ ตำบล อำเภอ จังหวัด ประเทศ)");
        fromdetail2.getView().setPadding(3, 0, 0, 0);
        fromdetail2.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));

        PDFTextView inputdesdetail1 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL);
        inputdesdetail1.getView().setPadding(12, 0, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputdesdetail1.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputdesdetail1.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }

        fromdetail2View.addView(fromcheck2);
        fromdetail2View.addView(fromdetail2);
        fromdetail2View.addView(inputdesdetail1);

        pdfBody.addView(fromdetail2View);

        PDFTextView fromdetail2eng = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("(Depart from the current address above)");
        fromdetail2eng.getView().setPadding(3, 0, 0, 0);
        LinearLayout.LayoutParams fromdetail2engengparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        fromdetail2engengparams.setMarginStart(80);
        fromdetail2eng.setLayout(fromdetail2engengparams);
        pdfBody.addView(fromdetail2eng);

        PDFLineSeparatorView lineSeparatorView11 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        lineSeparatorView11.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                3, 0));
        pdfBody.addView(lineSeparatorView11);

        PDFTextView inputfrom1 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL);
        inputfrom1.getView().setPadding(12, 0, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputfrom1.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputfrom1.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        pdfBody.addView(inputfrom1);

        PDFLineSeparatorView lineSeparatorView12 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        lineSeparatorView12.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                3, 0));
        pdfBody.addView(lineSeparatorView12);

        PDFTextView inputfrom2 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL);
        inputfrom2.getView().setPadding(12, 0, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputfrom2.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputfrom2.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        pdfBody.addView(inputfrom2);

        PDFLineSeparatorView lineSeparatorView13 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        pdfBody.addView(lineSeparatorView13);

        PDFTextView headerdestination = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("2.     ประสงค์จะเดินทางต่อไปยังสถานที่ใด");
        headerdestination.getView().setPadding(16, 0, 0, 0);
        pdfBody.addView(headerdestination);

        PDFTextView engheaderdestination = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("        (Next Destination)");
        engheaderdestination.getView().setPadding(18, 0, 0, 0);
        pdfBody.addView(engheaderdestination);

        PDFHorizontalView desdetail1View = new PDFHorizontalView(getApplicationContext());
        desdetail1View.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));

        PDFTextView descheck1 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText(" ");
        descheck1.setPadding(10, 3, 10, 3);
        LinearLayout.LayoutParams descheck1params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        descheck1params.setMarginStart(38);
        desdetail1View.setLayout(descheck1params);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            descheck1.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_line_black));
        } else {
            descheck1.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.border_line_black));
        }

        PDFTextView desdetail1 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("2.1 เดินทางกลับไปยังที่อยู่ปัจจุบันที่เป็นภูมิลำเนา");
        desdetail1.getView().setPadding(3, 0, 0, 0);
        desdetail1.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));

        desdetail1View.addView(descheck1);
        desdetail1View.addView(desdetail1);

        pdfBody.addView(desdetail1View);

        PDFTextView desdetail1eng = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("(Back to the current address above)");
        desdetail1eng.getView().setPadding(3, 0, 0, 0);
        LinearLayout.LayoutParams desdetail1engparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        desdetail1engparams.setMarginStart(80);
        desdetail1eng.setLayout(desdetail1engparams);
        pdfBody.addView(desdetail1eng);

        PDFHorizontalView desdetail2View = new PDFHorizontalView(getApplicationContext());
        desdetail2View.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));

        PDFTextView descheck2 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText(" ");
        descheck2.setPadding(10, 3, 10, 3);
        LinearLayout.LayoutParams descheck2params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        descheck2params.setMarginStart(38);
        descheck2.setLayout(descheck2params);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            descheck2.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_line_black));
        } else {
            descheck2.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.border_line_black));
        }

        PDFTextView desdetail2 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("2.2 เดินทางต่อไปยังสถานที่พักอื่น(บ้านเลขที่ ตำบล อำเภอ จังหวัด ประเทศ)");
        desdetail2.getView().setPadding(3, 0, 0, 0);
        desdetail2.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));

        PDFTextView inputdesdetail2 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL);
        inputdesdetail2.getView().setPadding(12, 0, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputdesdetail2.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputdesdetail2.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }

        desdetail2View.addView(descheck2);
        desdetail2View.addView(desdetail2);
        desdetail2View.addView(inputdesdetail2);

        pdfBody.addView(desdetail2View);

        PDFTextView desdetail2eng = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL)
                .setText("(Next Destination)");
        desdetail2eng.getView().setPadding(3, 0, 0, 0);
        LinearLayout.LayoutParams desdetail2engengparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        desdetail2engengparams.setMarginStart(80);
        desdetail2eng.setLayout(desdetail2engengparams);
        pdfBody.addView(desdetail2eng);

        PDFLineSeparatorView lineSeparatorView14 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        lineSeparatorView14.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                3, 0));
        pdfBody.addView(lineSeparatorView14);

        PDFTextView inputdes1 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL);
        inputdes1.getView().setPadding(12, 0, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputdes1.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputdes1.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        pdfBody.addView(inputdes1);

        PDFLineSeparatorView lineSeparatorView15 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        lineSeparatorView15.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                3, 0));
        pdfBody.addView(lineSeparatorView15);

        PDFTextView inputdes2 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL);
        inputdes2.getView().setPadding(12, 0, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputdes2.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputdes2.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        pdfBody.addView(inputdes2);

        PDFLineSeparatorView lineSeparatorView16 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        pdfBody.addView(lineSeparatorView16);


        return pdfBody;
    }

    @Override
    protected PDFFooterView getFooterView(int pageIndex) {
        PDFFooterView footerView = new PDFFooterView(getApplicationContext());

        PDFHorizontalView footerLinear = new PDFHorizontalView(getApplicationContext());
        footerLinear.setLayout((new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)));

        PDFVerticalView verticalViewLeft = new PDFVerticalView(getApplicationContext());
        LinearLayout.LayoutParams layoutParamsViewLeft = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1);
        verticalViewLeft.setPadding(16, 0, 16, 0);
        verticalViewLeft.setLayout(layoutParamsViewLeft);
        PDFTextView thaidate = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText("วัน เดือน ปี");
        thaidate.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        thaidate.getView().setGravity(Gravity.CENTER_HORIZONTAL);
        verticalViewLeft.addView(thaidate);

        PDFLineSeparatorView line1 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        line1.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2, 0));
        verticalViewLeft.addView(line1);

        PDFTextView thaidatearrival = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText("ที่เข้าพัก");
        thaidatearrival.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        thaidatearrival.getView().setGravity(Gravity.CENTER_HORIZONTAL);
        verticalViewLeft.addView(thaidatearrival);

        PDFLineSeparatorView line2 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        line2.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2, 0));
        verticalViewLeft.addView(line2);

        PDFTextView engdatearrival = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText("(Date of Arrival)");
        engdatearrival.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        engdatearrival.getView().setGravity(Gravity.CENTER_HORIZONTAL);
        verticalViewLeft.addView(engdatearrival);

        PDFLineSeparatorView line3 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        line3.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5, 0));
        verticalViewLeft.addView(line3);

        String[] checkin = detailInfo.getCheckinDate().split(" ");
        PDFTextView inputarrival = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText(checkin[0]);
        inputarrival.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        inputarrival.getView().setGravity(Gravity.CENTER_HORIZONTAL);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputarrival.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputarrival.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        verticalViewLeft.addView(inputarrival);


        PDFLineSeparatorView line4 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        line4.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5, 0));
        verticalViewLeft.addView(line4);

        PDFHorizontalView timeLinear = new PDFHorizontalView(getApplicationContext());
        PDFTextView timetxt = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText("เวลา");
        timetxt.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        timeLinear.addView(timetxt);
        PDFTextView inputtime = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText(checkin[1] + " น.");
        inputtime.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        inputtime.setPadding(-15,0,0,0);
        inputtime.getView().setGravity(Gravity.CENTER_HORIZONTAL);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputtime.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputtime.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        timeLinear.addView(inputtime);
        verticalViewLeft.addView(timeLinear);

        PDFLineSeparatorView line5 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        line5.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2, 0));
        verticalViewLeft.addView(line5);

        PDFTextView engtimetxt = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText("(Time)");
        verticalViewLeft.addView(engtimetxt);

        PDFLineSeparatorView line6 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        line6.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2, 0));
        verticalViewLeft.addView(line6);

        PDFVerticalView verticalViewCenter = new PDFVerticalView(getApplicationContext());
        LinearLayout.LayoutParams layoutParamsViewCenter = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1);
        verticalViewCenter.setPadding(16, 0, 16, 0);
        verticalViewCenter.setLayout(layoutParamsViewCenter);
        PDFTextView thaidate2 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText("วัน เดือน ปี");
        thaidate2.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        thaidate2.getView().setGravity(Gravity.CENTER_HORIZONTAL);
        verticalViewCenter.addView(thaidate2);

        PDFLineSeparatorView line7 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        line7.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2, 0));
        verticalViewCenter.addView(line7);

        PDFTextView thaiexpected = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText("ที่ออกไป");
        thaiexpected.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        thaiexpected.getView().setGravity(Gravity.CENTER_HORIZONTAL);
        verticalViewCenter.addView(thaiexpected);

        PDFLineSeparatorView line8 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        line8.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2, 0));
        verticalViewCenter.addView(line8);

        PDFTextView engexpected = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText("(Expected Departure)");
        engexpected.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        engexpected.getView().setGravity(Gravity.CENTER_HORIZONTAL);
        verticalViewCenter.addView(engexpected);

        PDFLineSeparatorView line9 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        line9.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5, 0));
        verticalViewCenter.addView(line9);

        String[] checkout = detailInfo.getCheckoutDate().split(" ");
        PDFTextView inputexpected = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText(checkout[0]);
        inputexpected.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        inputexpected.getView().setGravity(Gravity.CENTER_HORIZONTAL);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputexpected.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputexpected.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        verticalViewCenter.addView(inputexpected);

        PDFLineSeparatorView line10 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        line10.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5, 0));
        verticalViewCenter.addView(line10);

        PDFHorizontalView timeLinearexpected = new PDFHorizontalView(getApplicationContext());
        PDFTextView timetxtexpected = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText("เวลา");
        timetxtexpected.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        timeLinearexpected.addView(timetxtexpected);
        PDFTextView inputtimeexpected = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText(checkout[1] + " น.");
        inputtimeexpected.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        inputtimeexpected.setPadding(-15,0,0,0);
        inputtimeexpected.getView().setGravity(Gravity.CENTER_HORIZONTAL);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputtimeexpected.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputtimeexpected.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        timeLinearexpected.addView(inputtimeexpected);
        verticalViewCenter.addView(timeLinearexpected);

        PDFTextView engtimetxtexpected = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText("(Time)");
        verticalViewCenter.addView(engtimetxtexpected);

        PDFLineSeparatorView line11 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        line11.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2, 0));
        verticalViewCenter.addView(line11);


        PDFVerticalView verticalViewRight = new PDFVerticalView(getApplicationContext());
        LinearLayout.LayoutParams layoutParamsViewRight = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1);
        verticalViewRight.setPadding(16, 0, 16, 0);
        verticalViewRight.setLayout(layoutParamsViewRight);
        PDFHorizontalView LinearRoomNo = new PDFHorizontalView(getApplicationContext());
        PDFTextView txtRoomNo = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText("ห้องพักเลขที่");
        txtRoomNo.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearRoomNo.addView(txtRoomNo);
        PDFTextView inputRoomNo = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText(detailInfo.getRoomNo());
        inputRoomNo.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        inputRoomNo.getView().setGravity(Gravity.CENTER_HORIZONTAL);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputRoomNo.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputRoomNo.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        LinearRoomNo.addView(inputRoomNo);
        verticalViewRight.addView(LinearRoomNo);

        PDFLineSeparatorView line12 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        line12.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2, 0));
        verticalViewRight.addView(line12);

        PDFTextView engRoomNo = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText("(Room No.)");
        engRoomNo.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        verticalViewRight.addView(engRoomNo);

        PDFLineSeparatorView line13 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        line13.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2, 0));
        verticalViewRight.addView(line13);

        PDFTextView thaisignatureTxt = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText("ลายมือชื่อผู้พัก");
        thaisignatureTxt.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        thaisignatureTxt.getView().setGravity(Gravity.CENTER_HORIZONTAL);
        verticalViewRight.addView(thaisignatureTxt);

        PDFTextView engsignatureTxt = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL).setText("(Guest Signature)");
        engsignatureTxt.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        engsignatureTxt.getView().setGravity(Gravity.CENTER_HORIZONTAL);
        verticalViewRight.addView(engsignatureTxt);

        PDFLineSeparatorView line14 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        line14.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                12, 0));
        verticalViewRight.addView(line14);


        PDFTextView inputsignature = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL);
        inputsignature.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        inputsignature.getView().setGravity(Gravity.CENTER_HORIZONTAL);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            inputsignature.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        } else {
            inputsignature.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.dotted_line));
        }
        verticalViewRight.addView(inputsignature);

        footerLinear.addView(verticalViewLeft);
        footerLinear.addView(verticalViewCenter);
        footerLinear.addView(verticalViewRight);

        footerView.addView(footerLinear);

        return footerView;
    }

    @Nullable
    @Override
    protected PDFImageView getWatermarkView(int forPage) {
        PDFImageView pdfImageView = new PDFImageView(getApplicationContext());
        FrameLayout.LayoutParams childLayoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                200, Gravity.CENTER);
        pdfImageView.setLayout(childLayoutParams);

//        pdfImageView.setImageResource(R.drawable.waclogo);
        pdfImageView.setImageScale(ImageView.ScaleType.FIT_CENTER);
        pdfImageView.getView().setAlpha(0.3F);

        return pdfImageView;
    }

    @Override
    protected void onNextClicked(final File savedPDFFile) {
        Uri pdfUri = Uri.fromFile(savedPDFFile);

        Intent intentPdfViewer = new Intent(PdfCreatorExampleActivity.this, PdfViewerExampleActivity.class);
        intentPdfViewer.putExtra(PdfViewerExampleActivity.PDF_FILE_URI, pdfUri);

        startActivity(intentPdfViewer);
    }
}
