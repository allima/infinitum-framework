package com.clarionmedia.infinitumtest;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;

import com.clarionmedia.infinitum.context.ApplicationContextFactory;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.ObjectMapper;
import com.clarionmedia.infinitumtest.domain.TestModel;

public class InfinitumTestActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        try {
			ApplicationContextFactory.configure(this, R.xml.infinitum);
			TestModel test = new TestModel();
			test.setId(99);
			test.setMyField("hello world!");
			test.setX(42);
			ContentValues cv = ObjectMapper.mapModel(test);
			cv.describeContents();
		} catch (InfinitumConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}