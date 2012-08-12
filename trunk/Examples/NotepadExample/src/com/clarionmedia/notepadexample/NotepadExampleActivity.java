package com.clarionmedia.notepadexample;

import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.clarionmedia.infinitum.activity.InfinitumListActivity;
import com.clarionmedia.infinitum.activity.annotation.InjectLayout;
import com.clarionmedia.infinitum.context.InfinitumContext.DataSource;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.notepadexample.domain.Note;
import com.clarionmedia.notepadexample.widget.NotesAdapter;

@InjectLayout(R.layout.main)
public class NotepadExampleActivity extends InfinitumListActivity {
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerForContextMenu(getListView());
        new LoadNotesAsyncTask().execute();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.addNote:
    	    Intent intent = new Intent(this, NoteTaker.class);
    	    startActivityForResult(intent, 1);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (v.getId() == android.R.id.list) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
            Note selected = (Note) getListView().getAdapter().getItem(info.position);
            menu.setHeaderTitle(selected.getName());
            menu.add(Menu.NONE, 0, 0, "Delete");
            menu.add(Menu.NONE, 1, 1, "Cancel");
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        Note note = (Note) getListView().getAdapter().getItem(info.position);
        if (menuItemIndex == 0) {
        	new DeleteNoteAsyncTask().execute(note);
        }
        return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == 1 && resultCode == RESULT_OK) {
    		new LoadNotesAsyncTask().execute();
    	}
    }
    
    private class DeleteNoteAsyncTask extends AsyncTask<Note, Void, Note> {

		@Override
		protected Note doInBackground(Note... note) {
			Session session = getInfinitumContext().getSession(DataSource.Sqlite);
			session.open();
			session.delete(note[0]);
			session.close();
			return note[0];
		}
		
		@Override
		protected void onPostExecute(Note note) {
			NotesAdapter adapter = (NotesAdapter) getListView().getAdapter();
			adapter.remove(note);
			adapter.notifyDataSetChanged();
		}
    	
    }
    
	private class LoadNotesAsyncTask extends AsyncTask<Void, Void, List<Note>> {

		@Override
		protected List<Note> doInBackground(Void... args) {
			Session session = getInfinitumContext().getSession(DataSource.Sqlite);
			session.open();
			List<Note> notes = session.createCriteria(Note.class).list();
			session.close();
			return notes;
		}
		
		@Override
		protected void onPostExecute(List<Note> notes) {
			NotesAdapter adapter = new NotesAdapter(NotepadExampleActivity.this, android.R.id.text1, notes);
			getListView().setAdapter(adapter);
		}
		
	}
}