package com.clarionmedia.notepadexample.widget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.clarionmedia.notepadexample.R;
import com.clarionmedia.notepadexample.domain.Note;

public class NotesAdapter extends ArrayAdapter<Note> {

	private List<Note> mNotes;
	private Context mContext;

	public NotesAdapter(Context context, int textViewResourceId, List<Note> notes) {
		super(context, textViewResourceId, notes);
		mNotes = notes;
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.note, parent, false);
		TextView noteName = (TextView) rowView.findViewById(R.id.noteName);
		TextView noteContents = (TextView) rowView.findViewById(R.id.noteContents);
		Note note = mNotes.get(position);
		noteName.setText(note.getName());
		noteContents.setText(note.getContents());
		return rowView;
	}

}
