package org.bhumi.bhumisrte.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.bhumi.bhumisrte.R;

public class AboutRteActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView paragraphOne;
    private TextView paragraphTwo;
    private TextView paragraphThree;
    private TextView paragraphFour;
    private Button volunteerSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_rte);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        paragraphOne = findViewById(R.id.firstParagraph);
        paragraphTwo = findViewById(R.id.secondParagraph);
        paragraphThree = findViewById(R.id.thirdParagraph);
        paragraphFour = findViewById(R.id.fourthParagraph);
        volunteerSignUp = findViewById(R.id.volunteerSignUpButton);


        setUpText();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        volunteerSignUp.setOnClickListener(this);
    }

    private void setUpText() {
        Spanned paragraph_one = Html.fromHtml("The <strong>Right to Education (RTE)</strong>" +
                " act of 2009 made India one of the very few countries in the world to make " +
                "education a right of every child. <strong>Section 12(1)c</strong> of the act " +
                "guarantees 25% reservation for children of backward and economically sections of " +
                "the society free education in private schools.");

        Spanned paragraph_two = Html.fromHtml("An estimated <strong>20 lakh such free seats " +
                "are available each year</strong>, yet 80% of them are not taken as there is little " +
                "awareness among under-privileged communities. Our focus is to engage " +
                "<strong>volunteers in creating awareness</strong> among such communities and " +
                "helping them benefit from this <strong>life transforming opportunity</strong>.");

        Spanned paragraph_three = Html.fromHtml("This app developed by our own volunteers " +
                "will help you submit the details of eligible children to Bhumi. Bhumi volunteers " +
                "will then call these parents to assist them in applying for admissions under RTE.");

        Spanned paragraph_four = Html.fromHtml("Transform lives, Volunteer with Bhumi");

        paragraphOne.setText(paragraph_one);
        paragraphTwo.setText(paragraph_two);
        paragraphThree.setText(paragraph_three);
        paragraphFour.setText(paragraph_four);


    }

    @Override
    public void onClick(View v) {
        String url = "http://rte25.bhumi.ngo/volunteer/?utm_srouce=BRapp";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}
