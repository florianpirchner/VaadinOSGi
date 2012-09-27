/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *  Contributions:
 * 	Florian Pirchner - migrated to vaadin 7
 */
package uk.org.brindy.guessit7.view;

import uk.org.brindy.guessit7.GuessItUI;
import uk.org.brindy.guessit7.Session;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class MainView {

	@SuppressWarnings("serial")
	public MainView(final GuessItUI ui) {
		System.out.println("MainView.MainView()");
		VerticalLayout vbox = new VerticalLayout();
		vbox.addComponent(new Label(
				"Welcome to Guess It! A demo of Vaadin running in OSGi"));
		vbox.addComponent(new Label("What is your name?"));

		final TextField field = new TextField();
		vbox.addComponent(field);

		Button button = new Button("Start");
		button.setImmediate(true);
		button.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Session session = null;
				if ("".equals(((String) field.getValue()).trim())) {
					session = new Session(ui, "Anonymous");
				} else {
					session = new Session(ui, ((String) field.getValue())
							.trim());
				}
				ui.setUser(session);
				new GameView(ui);
			}
		});
		vbox.addComponent(button);

		ui.setContent(vbox);
	}
}
