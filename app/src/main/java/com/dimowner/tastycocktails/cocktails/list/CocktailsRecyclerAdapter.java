/*
 * Copyright 2017 Dmitriy Ponomarenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dimowner.tastycocktails.cocktails.list;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.cocktails.CocktailsListFragment;
import com.dimowner.tastycocktails.util.TimeUtils;

import static com.dimowner.tastycocktails.cocktails.CocktailsListFragment.TYPE_HISTORY;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class CocktailsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final static int VIEW_TYPE_NORMAL = 1;
	private final static int VIEW_TYPE_PROGRESS = 2;
	private final static int VIEW_TYPE_FOOTER = 3;
	private static final int VIEW_TYPE_FOOTER2 = 4;

	private boolean showFooter;

	private List<ListItem> mBaseData = new ArrayList<>();

	private String filterStr = "";

	private List<ListItem> mShowingData;

	private ItemClickListener itemClickListener;

	private OnFavoriteClickListener onFavoriteClickListener;

	private ItemLongClickListener itemLongClickListener;

	private int itemLayoutResId;

	private boolean showFooter2 = false;

	//In what type of list is adapter use: normal, fav, history;
	private int type = CocktailsListFragment.TYPE_UNKNOWN;


	public class ItemViewHolder extends RecyclerView.ViewHolder {
		TextView name;
		TextView description;
		ImageView image;
		ImageView btnFev;
		View view;
		LinearLayout container;

		public ItemViewHolder(View itemView) {
			super(itemView);
			this.view = itemView;
			this.name = itemView.findViewById(R.id.list_item_name);
			this.description = itemView.findViewById(R.id.list_item_description);
			this.image = itemView.findViewById(R.id.list_item_image);
			this.btnFev = itemView.findViewById(R.id.list_item_btn_favorite);
			this.container = itemView.findViewById(R.id.container);
		}

		public LinearLayout getContainer() {
			return container;
		}
	}

	static class LoadingViewHolder extends RecyclerView.ViewHolder {

		ProgressBar progressBar;

		LoadingViewHolder(View itemView) {
			super(itemView);
			progressBar = itemView.findViewById(R.id.list_item_progress);
		}
	}

	public static class FooterViewHolder extends RecyclerView.ViewHolder {
		final View view;

		FooterViewHolder(View itemView){
			super(itemView);
			view = itemView;
		}
	}

	public CocktailsRecyclerAdapter(int layoutResId) {
		this.mShowingData = new ArrayList<>();
		this.itemLayoutResId = layoutResId;
	}

	public CocktailsRecyclerAdapter(int type, int layoutResId) {
		this.mShowingData = new ArrayList<>();
		this.itemLayoutResId = layoutResId;
		this.type = type;
	}

	public void showFooter(boolean show) {
		if (showFooter == show) return;
		showFooter = show;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == VIEW_TYPE_NORMAL) {
			View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResId, parent, false);
			return new ItemViewHolder(v);
		} else if (viewType == VIEW_TYPE_PROGRESS) {
			View v = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.list_item_progress, parent, false);
			return new LoadingViewHolder(v);
		} else if (viewType == VIEW_TYPE_FOOTER) {
			View v = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.list_footer, parent, false);
			return new FooterViewHolder(v);
		} else if (viewType == VIEW_TYPE_FOOTER2) {
			View v = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.list_item_footer3, parent, false);
			return new FooterViewHolder(v);
		} else {
			return null;
		}
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder h, final int position1) {
		if (h.getItemViewType() == VIEW_TYPE_NORMAL) {
//			int pos = h.getAdapterPosition();
			final int pos = position1;
			ItemViewHolder holder = (ItemViewHolder) h;
			holder.name.setText(mShowingData.get(pos).getName());
			if (type == TYPE_HISTORY) {
				holder.description.setText(TimeUtils.formatTime(mShowingData.get(pos).getHistory()));
			} else {
				holder.description.setText(mShowingData.get(pos).getCategory());
			}

			if (mShowingData.get(pos).getAvatar_url() != null) {
				Glide.with(holder.view.getContext())
						.load(mShowingData.get(pos).getAvatar_url())
//						.apply(RequestOptions.circleCropTransform())
						.listener(new RequestListener<Drawable>() {
							@Override
							public boolean onLoadFailed(@Nullable GlideException e, Object model,
																 Target<Drawable> target, boolean isFirstResource) {
								holder.image.setImageResource(R.drawable.no_image);
								return false;
							}

							@Override
							public boolean onResourceReady(Drawable resource, Object model,
																	 Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
								return false;
							}
						})
						.into(holder.image);
			} else {
				holder.image.setImageResource(R.drawable.no_image);
			}

			final int id = (int) mShowingData.get(pos).getId();
			holder.btnFev.setOnClickListener(v -> {
				if (onFavoriteClickListener != null) {
					onFavoriteClickListener.onFavoriteClick(
							holder.btnFev, findPositionForId(id), id, -1);
				}
			});

			if (mShowingData.get(pos).isFavorite()) {
				holder.btnFev.setImageResource(R.drawable.round_heart_grey);
			} else {
				holder.btnFev.setImageResource(R.drawable.round_heart_border_grey);
			}

			holder.view.setOnClickListener(v -> {
				if (itemClickListener != null) {
					itemClickListener.onItemClick(v, findPositionForId(id));
				}
			});

			holder.view.setOnLongClickListener(v -> {
				if (itemLongClickListener != null) {
					itemLongClickListener.onItemLongClick(v, id, findPositionForId(id));
				}
				return true;
			});
		} else if (h.getItemViewType() == VIEW_TYPE_PROGRESS) {
			//Do nothing
		}
//		//Set transition names
//		Resources res = holder.view.getResources();
//		ViewCompat.setTransitionName(holder.name, res.getString(R.string.list_item_label_transition));
//		ViewCompat.setTransitionName(holder.description, res.getString(R.string.list_item_content_transition));
//		ViewCompat.setTransitionName(holder.image, res.getString(R.string.list_item_image_transition));
	}

	private int findPositionForId(int id) {
		for (int i = 0; i < mShowingData.size(); i++) {
			if (id == mShowingData.get(i).getId()) {
				return i;
			}
		}
		return 0;
	}

	@Override
	public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
		super.onDetachedFromRecyclerView(recyclerView);
	}

	@Override
	public int getItemCount() {
		return mShowingData.size() + (showFooter ? 1 : 0) + (showFooter2 ? 1 : 0);
	}

	@Override
	public int getItemViewType(int position) {
		if (showFooter) {
			if (position == mShowingData.size()) {
				return VIEW_TYPE_FOOTER;
			} else if (position == mShowingData.size()+1) {
				return VIEW_TYPE_FOOTER2;
			}
		} else {
			if (position == mShowingData.size()) {
				return VIEW_TYPE_FOOTER2;
			}
		}
		return VIEW_TYPE_NORMAL;
	}

	public void showBottomPanelMargin(boolean b) {
		showFooter2 = b;
		if (showFooter) {
			if (b) {
				notifyItemInserted(mShowingData.size() + 1);
			} else {
				notifyItemRemoved(mShowingData.size() + 1);
			}
		} else {
			if (b) {
				notifyItemInserted(mShowingData.size());
			} else {
				notifyItemRemoved(mShowingData.size());
			}
		}
	}

	public ListItem getItem(int pos) {
		if (pos >=0 && pos < mShowingData.size()) {
			return mShowingData.get(pos);
		} else {
			return null;
		}
	}

	public void setData(List<ListItem> data) {
		mBaseData = data;
		if (isFiltered()) {
			updateShowingDataWithFilter();
		} else {
			CocktailsDiffUtilCallback productDiffUtilCallback = new CocktailsDiffUtilCallback(mShowingData, mBaseData);
			DiffUtil.DiffResult productDiffResult = DiffUtil.calculateDiff(productDiffUtilCallback);
			this.mShowingData.clear();
			this.mShowingData.addAll(mBaseData);
			productDiffResult.dispatchUpdatesTo(this);
		}
	}

	public List<ListItem> getData() {
		return mShowingData;
	}

//	public void addItems(List<ListItem> items) {
//		mShowingData.addAll(items);
//		notifyItemRangeInserted(mShowingData.size() - items.size() - 1, items.size());
//	}

	/**
	 * Update showing data by applying search filter.
	 */
	private void updateShowingDataWithFilter() {
		if (isFiltered()) {
			List<ListItem> oldData = new ArrayList<>(mShowingData);
			mShowingData.clear();
			for (int i = 0; i < mBaseData.size(); i++) {
				if (mBaseData.get(i).getName().toLowerCase().contains(filterStr.toLowerCase())) {
					mShowingData.add(mBaseData.get(i));
				}
			}
			CocktailsDiffUtilCallback productDiffUtilCallback = new CocktailsDiffUtilCallback(oldData, mShowingData);
			DiffUtil.DiffResult productDiffResult = DiffUtil.calculateDiff(productDiffUtilCallback);
			productDiffResult.dispatchUpdatesTo(this);
		}
	}

	public void applyFilter(String str) {
		if (str == null || str.isEmpty()) {
			filterStr = "";
			CocktailsDiffUtilCallback productDiffUtilCallback = new CocktailsDiffUtilCallback(mShowingData, mBaseData);
			DiffUtil.DiffResult productDiffResult = DiffUtil.calculateDiff(productDiffUtilCallback);
			this.mShowingData.clear();
			mShowingData.addAll(mBaseData);
			productDiffResult.dispatchUpdatesTo(this);
		} else {
			filterStr = str;
			updateShowingDataWithFilter();
		}
	}

	private boolean isFiltered() {
		return (filterStr != null && !filterStr.isEmpty());
	}

	public void setItemClickListener(ItemClickListener itemClickListener) {
		this.itemClickListener = itemClickListener;
	}

	public void setOnFavoriteClickListener(OnFavoriteClickListener onFavoriteClickListener) {
		this.onFavoriteClickListener = onFavoriteClickListener;
	}

	public void setItemLongClickListener(ItemLongClickListener itemLongClickListener) {
		this.itemLongClickListener = itemLongClickListener;
	}

//	/**
////	 * Save adapters state
////	 * @return adapter state.
////	 */
////	public Parcelable onSaveInstanceState() {
////		SavedState ss = new SavedState(AbsSavedState.EMPTY_STATE);
////		ss.items = mShowingData.toArray(new ListItem[0]);
////		return ss;
////	}
////
////	/**
////	 * Restore adapters state
////	 * @param state Adapter state.
////	 */
////	public void onRestoreInstanceState(Parcelable state) {
////		SavedState ss = (SavedState) state;
////		mShowingData = new ArrayList<>();
////		Collections.addAll(mShowingData, ss.items);
////		notifyDataSetChanged();
////	}
//
//
//	/**
//	 * Object state
//	 */
//	public static class SavedState extends View.BaseSavedState {
//		SavedState(Parcelable superState) {
//			super(superState);
//		}
//
//		private SavedState(Parcel in) {
//			super(in);
//			items = (ListItem[]) in.readParcelableArray(getClass().getClassLoader());
//		}
//
//		@Override
//		public void writeToParcel(Parcel out, int flags) {
//			super.writeToParcel(out, flags);
//			out.writeParcelableArray(items, flags);
//		}
//
//		ListItem[] items;
//
//		public static final Parcelable.Creator<SavedState> CREATOR =
//				new Parcelable.Creator<SavedState>() {
//					@Override
//					public SavedState createFromParcel(Parcel in) {
//						return new SavedState(in);
//					}
//
//					@Override
//					public SavedState[] newArray(int size) {
//						return new SavedState[size];
//					}
//				};
//	}

	public interface ItemClickListener{
		void onItemClick(View view, int position);
	}

	public interface ItemLongClickListener{
		void onItemLongClick(View view, long id, int position);
	}

	public interface OnFavoriteClickListener {
		void onFavoriteClick(ImageView view, int position, int id, int action);
	}
}
