package com.clarionmedia.infinitumtest;

import java.lang.reflect.Field;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import com.clarionmedia.infinitum.context.ApplicationContextFactory;
import com.clarionmedia.infinitum.orm.criteria.BinaryExpression;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.criteria.Criterion;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTemplate;
import com.clarionmedia.infinitumtest.domain.TestModel;

public class SqliteTemplateActivity extends Activity {
	
	public List<Field> mPrimaryKeys;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ApplicationContextFactory.configure(this, R.xml.infinitum);
		TestModel model = new TestModel();
		SqliteTemplate sqlite = new SqliteTemplate(this);
		sqlite.open();
		//sqlite.save(model);
		//TestModel loaded = sqlite.load(TestModel.class, 2L);
		Criteria<TestModel> criteria = sqlite.createCriteria(TestModel.class);
		Criterion c = new BinaryExpression("mMyField", "hello world!", "=", true);
		criteria.add(c);
		String sql = criteria.toSql();
		sqlite.close();
	}

}
