package com.clarionmedia.notepadexample;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.clarionmedia.infinitum.activity.InfinitumActivity;
import com.clarionmedia.infinitum.activity.annotation.Bind;
import com.clarionmedia.infinitum.activity.annotation.InjectLayout;
import com.clarionmedia.infinitum.activity.annotation.InjectView;
import com.clarionmedia.infinitum.context.InfinitumContext.DataSource;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.notepadexample.domain.Note;

@InjectLayout(R.layout.notetaker)
public class NoteTaker extends InfinitumActivity {
	
	@InjectView(R.id.noteName)
	private EditText mNoteName;
	
	@InjectView(R.id.noteContents)
	private EditText mNoteContents;
	
	@SuppressWarnings("unused")
	@InjectView(R.id.saveNote)
	@Bind(callback = "saveNote")
	private Button mSaveNote;
	
	@SuppressWarnings("unused")
	@InjectView(R.id.discardNote)
	@Bind(callback = "discardNote")
	private Button mDiscardNote;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
	
	@SuppressWarnings("unused")
	private void saveNote(View view) {
		String name = mNoteName.getText().toString().trim();
		String contents = mNoteContents.getText().toString();
		if (name.length() == 0)
			return;
		Note note = new Note();
		note.setName(name);
		note.setContents(contents);
		new SaveNoteAsyncTask().execute(note);
	}
	
	@SuppressWarnings("unused")
	private void discardNote(View view) {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	private class SaveNoteAsyncTask extends AsyncTask<Note, Void, Long> {

		@Override
		protected Long doInBackground(Note... note) {
			Session session = getInfinitumContext().getSession(DataSource.Sqlite);
			session.open();
			long id = session.save(note[0]);
			session.close();
			return id;
		}
		
		@Override
		protected void onPostExecute(Long id) {
			if (id > 0) {
				setResult(RESULT_OK);
				finish();
			}
		}
		
	}

}
