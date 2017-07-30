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

package task.softermii.tastycocktails.cocktails;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import task.softermii.tastycocktails.TCApplication;
import task.softermii.tastycocktails.cocktails.list.ListItem;
import task.softermii.tastycocktails.data.RepositoryContract;
import task.softermii.tastycocktails.data.model.Drink;
import timber.log.Timber;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class CocktailsPresenter implements SearchContract.UserActionsListener {

	private RepositoryContract repository;

	private SearchContract.View view;

	private CompositeDisposable compositeDisposable = new CompositeDisposable();

	public CocktailsPresenter(RepositoryContract repository) {
		this.repository = repository;
	}

	@Override
	public void bindView(@NonNull SearchContract.View view) {
		this.view = view;
	}

	@Override
	public void unbindView() {
		compositeDisposable.dispose();
		this.view = null;
	}

	@Override
	public void startSearch(String search) {
		view.showProgress();
		compositeDisposable.add(
				repository.searchCocktailsByName(search)
						.map(this::convertModel)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(this::displayData, this::handleError));
	}

	@Override
	public void cancelSearch() {
		view.hideProgress();
		if (compositeDisposable.size() > 0) {
			compositeDisposable.clear();
		}
	}

	private void displayData(List<ListItem> data) {
		view.hideProgress();
		view.displayData(data);
	}

	private void handleError(Throwable throwable) {
		Timber.e(throwable);
		view.hideProgress();
		if (TCApplication.isConnected()) {
			view.showQueryError();
		} else {
			view.showNetworkError();
		}
	}

	@Override
	public void loadLastSearch() {
		view.showProgress();
		compositeDisposable.add(
				repository.getLastSearch()
						.map(this::convertModel)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(this::displayData, this::handleError));
	}

	private List<ListItem> convertModel(List<Drink> drinks) {
		List<ListItem> list = new ArrayList<>(drinks.size());
		for (int i = 0; i < drinks.size(); i++) {
			Drink drink = drinks.get(i);
			list.add(new ListItem(drink.getIdDrink(), drink.getStrDrink(), drink.getStrInstructions(), drink.getStrDrinkThumb()));
		}
		return list;
	}
}
