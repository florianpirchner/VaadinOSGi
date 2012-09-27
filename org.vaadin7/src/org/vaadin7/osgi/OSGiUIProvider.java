package org.vaadin7.osgi;

import java.util.Dictionary;

import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;

import com.vaadin.server.AbstractUIProvider;
import com.vaadin.server.WrappedRequest;
import com.vaadin.ui.UI;

public class OSGiUIProvider extends AbstractUIProvider {

	private final ComponentFactory factory;
	private final Class<? extends UI> uiClass;
	private ComponentInstance instance;

	@SuppressWarnings("rawtypes")
	public OSGiUIProvider(ComponentFactory factory,
			Class<? extends UI> uiClass, Dictionary properties) {
		super();
		this.factory = factory;
		this.uiClass = uiClass;
	}

	@Override
	public Class<? extends UI> getUIClass(WrappedRequest request) {
		return uiClass;
	}

	@Override
	public UI createInstance(Class<? extends UI> type, WrappedRequest request) {
		instance = factory.newInstance(null);
		return (UI) instance.getInstance();
	}

	public void dispose() {
		if (instance != null) {
			instance.dispose();
			instance = null;
		}
	}

}
