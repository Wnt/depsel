package org.vaadin.jonni.depsel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

import org.vaadin.jonni.depsel.client.DependantSelectState;
import org.vaadin.jonni.depsel.client.DependantSelectServerRpc;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.SingleSelect;

/**
 * {@link NativeSelect} like field which option list is depends on the value of
 * a masterSelect NativeSelect field and the set {@link #setOptionMapping(Map)
 * valueMaping}.
 */
public class DependantSelect extends AbstractField<String> implements SingleSelect<String> {

	private final NativeSelect<String> masterSelect;

	/**
	 * Constructs a DependantSelect field with the given masterSelect field.
	 * 
	 * @param masterSelect The master select component, not null
	 */
	public DependantSelect(NativeSelect<String> masterSelect) {		
		Objects.requireNonNull(masterSelect, "masterSelect cannot be null");
		
		this.masterSelect = masterSelect;
		getState().masterSelect = masterSelect;
		getState().emptySelectionAllowed = true;
		getState().emptySelectionCaption = "";

		DependantSelect component = this;
		
		DependantSelectServerRpc rpc = new DependantSelectServerRpc() {
			@Override
			public void setValue(String value) {
				getState().value = value;
				fireEvent(new ValueChangeEvent(component,value,true));				
			}			
		};
				
		registerRpc(rpc);
	}

	/**
	 * Sets the option list mappings from master select to this DependantSelect.
	 * This also sets the items to the master select.
	 * 
	 * @param optionMapping The option mapping, not null
	 */
	public void setOptionMapping(Map<String, List<String>> optionMapping) {
		Objects.requireNonNull(masterSelect, "optionMapping cannot be null");

		getMasterSelect().setItems(new TreeSet<String>(optionMapping.keySet()));
		getState().optionMapping = optionMapping;
	}

	public Map<String, List<String>> getValueMapping() {
		return getState(false).optionMapping;
	}

	@Override
	protected DependantSelectState getState() {
		return (DependantSelectState) super.getState();
	}

	@Override
	protected DependantSelectState getState(boolean markAsDirty) {
		return (DependantSelectState) super.getState(markAsDirty);
	}

	@Override
	public String getValue() {
		return getState(false).value;
	}

	@Override
	protected void doSetValue(String value) {
		getState().value = value;
	}

	public NativeSelect<String> getMasterSelect() {
		return masterSelect;
	}

}
