/**
 * 
 */
package com.main.activitys.domain;
import com.main.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageFragment extends Fragment {
	private final int id;

	public ImageFragment(int id){
		this.id = id;
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
			view = inflater.inflate(R.layout.fragment_image, container, false);
			imageView = (ImageView) view.findViewById(R.id.imageViewIntro);
			imageView.setImageResource(R.drawable.intro__eight);
			return view;
		case 8:
			view = inflater.inflate(R.layout.fragment_image, container, false);
			imageView = (ImageView) view.findViewById(R.id.imageViewIntro);
			imageView.setImageResource(R.drawable.intro__nineth);
			return view;
		case 9:
			view = inflater.inflate(R.layout.fragment_image, container, false);
			imageView = (ImageView) view.findViewById(R.id.imageViewIntro);
			imageView.setImageResource(R.drawable.intro__tenth);
			return view;
		default:
			return view;


		}
	}
}
