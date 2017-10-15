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

package com.dimowner.tastycocktails.dagger.cocktails;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.Fragment;

import dagger.Module;
import dagger.Provides;
import com.dimowner.tastycocktails.cocktails.CocktailsPresenter;
import com.dimowner.tastycocktails.cocktails.SearchContract;
import com.dimowner.tastycocktails.data.Repository;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
@Module
public class CocktailsModule {

	private Fragment fragment;

	public CocktailsModule(Fragment fragment) {
		this.fragment = fragment;
	}

	@Provides
	@CocktailsScope
	SearchContract.UserActionsListener provideCocktailsPresenter(Repository repository) {
		CocktailsPresenter presenter = ViewModelProviders.of(fragment).get(CocktailsPresenter.class);
		presenter.setRepository(repository);
		return presenter;
	}
}