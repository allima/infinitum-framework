package com.clarionmedia.infinitumtest;

import android.app.Activity;
import android.os.Bundle;
import com.clarionmedia.infinitum.context.ApplicationContextFactory;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTemplate;
import com.clarionmedia.infinitumtest.domain.TestModel;

public class InfinitumTestActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		try {
			ApplicationContextFactory.configure(this, R.xml.infinitum);
		} catch (InfinitumConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TestModel test = new TestModel();
		SqliteTemplate sqlite = new SqliteTemplate(this);
		sqlite.open();
		sqlite.save(test);
		sqlite.close();
	}
}