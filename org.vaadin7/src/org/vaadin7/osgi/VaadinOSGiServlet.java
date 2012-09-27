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
 * Contributor:
 * 		Florian Pirchner - migrating to vaadin 7
 */
package org.vaadin7.osgi;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletSession;

/**
 * Used to create instances of applications that have been registered with the
 * container via a component factory.
 * 
 * @author brindy
 */
class VaadinOSGiServlet extends VaadinServlet {

	private static final long serialVersionUID = 1L;

	private final ComponentFactory factory;

	private Set<VaadinSession> sessions = new HashSet<VaadinSession>();

	public VaadinOSGiServlet(ComponentFactory factory) {
		this.factory = factory;
	}

	protected VaadinServletSession doCreateVaadinSession(
			HttpServletRequest request) {
		final VaadinSession info = new VaadinSession(factory.newInstance(null),
				request.getSession());

		info.session.setAttribute(VaadinOSGiServlet.class.getName(),
				new HttpSessionListener() {

					@Override
					public void sessionDestroyed(HttpSessionEvent arg0) {
						info.dispose();
					}

					@Override
					public void sessionCreated(HttpSessionEvent arg0) {

					}

				});
		System.out.println("Ready: " + info); //$NON-NLS-1$
		return (VaadinServletSession) info.instance.getInstance();

	}

	@Override
	public void destroy() {
		super.destroy();

		synchronized (this) {
			HashSet<VaadinSession> sessions = new HashSet<VaadinSession>();
			sessions.addAll(this.sessions);
			this.sessions.clear();
			for (VaadinSession info : sessions) {
				info.dispose();
			}
		}
	}

	/**
	 * Track the component instance and session. If this is disposed the entire
	 * associated http session is also disposed.
	 */
	class VaadinSession {

		final ComponentInstance instance;

		final HttpSession session;

		public VaadinSession(ComponentInstance instance, HttpSession session) {
			this.instance = instance;
			this.session = session;
			sessions.add(this);
		}

		public void dispose() {
			VaadinSession app = (VaadinSession) instance.getInstance();
			if (app != null) {
				app.dispose();
			}

			instance.dispose();

			session.removeAttribute(VaadinOSGiServlet.class.getName());
			sessions.remove(this);
		}
	}
}
