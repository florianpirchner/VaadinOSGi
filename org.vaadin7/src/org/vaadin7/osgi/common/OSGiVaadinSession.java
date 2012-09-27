package org.vaadin7.osgi.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.ComponentFactory;
import org.vaadin7.osgi.Constants;
import org.vaadin7.osgi.OSGiUIProvider;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import com.vaadin.server.VaadinServletSession;
import com.vaadin.ui.UI;

public class OSGiVaadinSession extends VaadinServletSession {

	private Map<ComponentFactory, OSGiUIProvider> uiProviders = new HashMap<ComponentFactory, OSGiUIProvider>();
	private BundleContext context;

	private List<Pair> pendingFactories = Collections
			.synchronizedList(new ArrayList<Pair>());

	public OSGiVaadinSession() {
		System.out.println("created");
	}

	@Activate
	public void activate(BundleContext context) throws InvalidSyntaxException {
		this.context = context;

		synchronized (pendingFactories) {
			for (Pair pair : pendingFactories) {
				registerFactory(pair);
			}
			pendingFactories.clear();
		}
	}

	@Deactivate
	public void deactivate() {
		context = null;
	}

	/**
	 * Called by OSGi-DS.
	 * 
	 * @param factory
	 * @param properties
	 */
	@Reference(unbind = "removeUI", dynamic = true, optional = false, multiple = true, target = "component.factory=org.vaadin.UI/*")
	public void addUI(ComponentFactory factory, Map<String, Object> properties) {

		if (context == null) {
			pendingFactories.add(new Pair(factory, properties));
			return;
		}

		registerFactory(new Pair(factory, properties));
	}

	/**
	 * Called by OSGi DS.
	 * 
	 * @param factory
	 * @param properties
	 */
	public void removeUI(ComponentFactory factory,
			Map<String, Object> properties) {
		if (uiProviders.containsKey(factory)) {
			OSGiUIProvider uiProvider = uiProviders.remove(factory);
			removeUIProvider(uiProvider);
			uiProvider.dispose();
		}
	}

	/**
	 * Registers the factory at the session.
	 * 
	 * @param pair
	 */
	@SuppressWarnings("unchecked")
	private void registerFactory(Pair pair) {
		ComponentFactory factory = pair.getFactory();
		String name = (String) pair.getProperties().get("component.factory");
		String className = name.substring(Constants.PREFIX__UI_CLASS.length());
		OSGiUIProvider uiProvider = null;
		try {
			uiProvider = new OSGiUIProvider(factory,
					(Class<? extends UI>) context.getBundle().loadClass(
							className), null);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
		if (uiProvider != null) {
			uiProviders.put(factory, uiProvider);
			addUIProvider(uiProvider);
		}
	}

	/**
	 * A pair of factory and properties.
	 */
	private static class Pair {
		private final ComponentFactory factory;
		private final Map<String, Object> properties;

		public Pair(ComponentFactory factory, Map<String, Object> properties) {
			super();
			this.factory = factory;
			this.properties = properties;
		}

		public ComponentFactory getFactory() {
			return factory;
		}

		public Map<String, Object> getProperties() {
			return properties;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((factory == null) ? 0 : factory.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pair other = (Pair) obj;
			if (factory == null) {
				if (other.factory != null)
					return false;
			} else if (!factory.equals(other.factory))
				return false;
			return true;
		}

	}
}
