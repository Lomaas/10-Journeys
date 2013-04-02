/**
 * 
 */
package com.saimenstravelapp.activitys.domain;
import com.saimenstravelapp.*;
import com.saimenstravelapp.activitys.IntroductionActivity;
import com.saimenstravelapp.activitys.TryOutGame;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class ImageFragment extends Fragment {
	private final int id;
	static Context ctx;
	public ImageFragment(int id, Context context){
		this.id = id;
		this.ctx = context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Test", "hello");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = null;
		ImageView imageView = null;
		switch (id) {
		case 0:
			view = inflater.inflate(R.layout.fragment_image, container, false);
			imageView = (ImageView) view.findViewById(R.id.imageViewIntro);
			imageView.setImageResource(R.drawable.intro__first);
			return view;
		case 1:
			view = inflater.inflate(R.layout.fragment_image, container, false);
			imageView = (ImageView) view.findViewById(R.id.imageViewIntro);
			imageView.setImageResource(R.drawable.intro__second);
			return view;
		case 2:
			view = inflater.inflate(R.layout.fragment_image, container, false);
			imageView = (ImageView) view.findViewById(R.id.imageViewIntro);
			imageView.setImageResource(R.drawable.intro__third);
			return view;
		case 3:
			view = inflater.inflate(R.layout.fragment_image, container, false);
			imageView = (ImageView) view.findViewById(R.id.imageViewIntro);
			imageView.setImageResource(R.drawable.intro__fourth);
			return view;
		case 4:
			view = inflater.inflate(R.layout.fragment_image, container, false);
			imageView = (ImageView) view.findViewById(R.id.imageViewIntro);
			imageView.setImageResource(R.drawable.intro__fifth);
			return view;
		case 5:
			view = inflater.inflate(R.layout.fragment_image, container, false);
			imageView = (ImageView) view.findViewById(R.id.imageViewIntro);
			imageView.setImageResource(R.drawable.intro__sixth);
			return view;
		case 6:
			view = inflater.inflate(R.layout.fragment_image, container, false);
			imageView = (ImageView) view.findViewById(R.id.imageViewIntro);
			imageView.setImageResource(R.drawable.intro__seventh);
			return view;
		case 7:
			view = inflater.inflate(R.layout.fragment_textview, container, false);
			Button button = (Button) view.findViewById(R.id.fragmentButton);
			
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent().setClass(getActivity().getBaseContext(), TryOutGame.class);
					startActivity(intent);
					IntroductionActivity act = (IntroductionActivity) ctx;
					act.finish();

				}
			});
			return view;
//		case 8:
//			view = inflater.inflate(R.layout.fragment_image, container, false);
//			imageView = (ImageView) view.findViewById(R.id.imageViewIntro);
//			imageView.setImageResource(R.drawable.intro__nineth);
//			return view;
//		case 9:
//			view = inflater.inflate(R.layout.fragment_image, container, false);
//			imageView = (ImageView) view.findViewById(R.id.imageViewIntro);
//			imageView.setImageResource(R.drawable.intro__tenth);
//			return view;
		default:
			return view;


		}
	}
}
