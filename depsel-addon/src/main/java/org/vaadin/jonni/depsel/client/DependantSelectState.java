package org.vaadin.jonni.depsel.client;

import java.util.List;
import java.util.Map;

import com.vaadin.shared.AbstractFieldState;
import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.nativeselect.NativeSelectState;

public class DependantSelectState extends AbstractFieldState {

	public Connector masterSelect;

	public Map<String, List<String>> optionMapping;

	public String value;

	public boolean emptySelectionAllowed;

	public String emptySelectionCaption;

}
