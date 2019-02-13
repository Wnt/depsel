package org.vaadin.jonni.depsel.client;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.vaadin.jonni.depsel.DependantSelect;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.ui.AbstractFieldConnector;
import com.vaadin.client.ui.VNativeSelect;
import com.vaadin.client.ui.nativeselect.NativeSelectConnector;
import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.Connect;

@Connect(org.vaadin.jonni.depsel.DependantSelect.class)
public class DependantSelectConnector extends AbstractFieldConnector {

	private HandlerRegistration changeHandlerRegistration;
	private String postponedValue;
	private List<String> currentValueList;
	private VNativeSelect masterWidget;
	private Timer checker;
	
	public DependantSelectConnector() {
		getWidget().getListBox().addChangeHandler(event -> {
			String selectedValue = getWidget().getListBox().getSelectedValue();
			getRpcProxy(DependantSelectClientRpc.class).setValue(selectedValue);
		});
	}

	@Override
	public VNativeSelect getWidget() {
		return (VNativeSelect) super.getWidget();
	}

	@Override
	public DependantSelectState getState() {
		return (DependantSelectState) super.getState();
	}

	@OnStateChange("masterSelect")
	private void onMasterChange() {
		Connector masterSelect = getState().masterSelect;
		NativeSelectConnector mastersConnector = ((NativeSelectConnector) masterSelect);
		masterWidget = mastersConnector.getWidget();		
	}
	
	@OnStateChange("optionMapping")
	private void onMappingChange() {
		Connector masterSelect = getState().masterSelect;
		NativeSelectConnector mastersConnector = ((NativeSelectConnector) masterSelect);
		masterWidget = mastersConnector.getWidget();		
		
		ListBox maserListBox = masterWidget.getListBox();
		if (changeHandlerRegistration != null) {
			changeHandlerRegistration.removeHandler();
		}
		// because there is no GWT API for listening for value changes that happen
		// programmatically, listen via JSNI
		addJsHandler(maserListBox.getElement());
		
		// because IE and Edge browserS do not notify about mutations on programmatic
		// changes, we need to check for value changes periodically
		if (BrowserInfo.get().isIE() || BrowserInfo.get().isEdge()) {
			checker = new Timer() {
				private String lastKnownMasterValue;
				@Override
				public void run() {
					String masterValue = maserListBox.getSelectedItemText();
					if (!Objects.equals(lastKnownMasterValue, masterValue)) {
						updateOptionsList();
					}
					lastKnownMasterValue = masterValue;
				}
			};
			checker.scheduleRepeating(100);
		}
		updateOptionsListBasedOnMasterListBox(maserListBox);
	}

	@Override
	public void onUnregister() {
		super.onUnregister();
		// Checker needs to cancelled when component is detached
		checker.cancel();
	}
	
	public void updateOptionsList() {
		ListBox masterListBox = masterWidget.getListBox();
		updateOptionsListBasedOnMasterListBox(masterListBox);
	}

	private native JavaScriptObject addJsHandler(JavaScriptObject e) /*-{
		var self = this;
		// Options for the observer (which mutations to observe)
		var config = { attributes: true, childList: true, subtree: true };
		
		// Callback function to execute when mutations are observed
		var callback = function(mutationsList, observer) {
			for (var i = 0; i < mutationsList.length; i++) {
				var mutation = mutationsList[i];
				console.warn("'"+mutation.type+"' happened!");
				console.warn(mutation);
				if (mutation.type == 'attributes') {
					self.@org.vaadin.jonni.depsel.client.DependantSelectConnector::updateOptionsList()();
				}
			}
		};
		
		// Create an observer instance linked to the callback function
		var observer = new MutationObserver(callback);
		
		// Start observing the target node for configured mutations
		observer.observe(e, config);
		
		return observer;
	}-*/;

	private void updateOptionsListBasedOnMasterListBox(ListBox masterListBox) {
		String masterValue = "";
		if (masterListBox != null && masterListBox.getSelectedItemText() != null ) {
			masterValue = masterListBox.getSelectedItemText();
		}
		if ("".equals(masterValue)) {
			setOptions(Arrays.asList());
			return;
		}

		String value = masterListBox.getSelectedItemText();
		List<String> optionList = getState().optionMapping.get(value);
		if (optionList == null) {
			optionList = Arrays.asList();
		}
		setOptions(optionList);
	}

	private void setOptions(List<String> optionList) {
		if (optionList.equals(currentValueList)) {
			return;
		}
		final VNativeSelect widget = getWidget();

		ListBox listBox = widget.getListBox();
		String valueBeforeUpdate = listBox.getSelectedItemText();
		final int itemCount = listBox.getItemCount();

		for (int i = 0; i < itemCount; i++) {
			listBox.removeItem(0);
		}
		if (getState().emptySelectionAllowed) {
			listBox.addItem(getState().emptySelectionCaption, "");
		}

		for (int i = 0; i < optionList.size(); i++) {
			String item = optionList.get(i);
			listBox.addItem(item, item);
		}
		setValueToListBox(listBox, valueBeforeUpdate);
		if (postponedValue != null) {
			setValueToListBox(listBox, postponedValue);
			postponedValue = null;
			// because the value might have changed, sync to server
			getRpcProxy(DependantSelectClientRpc.class).setValue(listBox.getSelectedItemText());
		}
		currentValueList = optionList;
	}

	@OnStateChange({ "emptySelectionCaption", "emptySelectionAllowed" })
	private void onEmptySelectionCaptionChange() {
		ListBox listBox = getWidget().getListBox();
		boolean hasEmptyItem = listBox.getItemCount() > 0 && listBox.getValue(0).isEmpty();
		if (hasEmptyItem && getState().emptySelectionAllowed) {
			listBox.setItemText(0, getState().emptySelectionCaption);
		} else if (hasEmptyItem && !getState().emptySelectionAllowed) {
			listBox.removeItem(0);
		} else if (!hasEmptyItem && getState().emptySelectionAllowed) {
			listBox.insertItem(getState().emptySelectionCaption, 0);
			listBox.setValue(0, "");
		}
	}

	@OnStateChange({ "value" })
	private void onValueChange() {
		postponedValue = null;
		ListBox listBox = getWidget().getListBox();
		String newVal = getState().value;
		if (newVal == null) {
			listBox.setSelectedIndex(0);
			return;
		}
		// if the option list has not yet been updated to include this value, postpone
		// the value setting to happen in setOptions
		boolean found = setValueToListBox(listBox, newVal);
		if (!found) {
			postponedValue = newVal;
		}
	}

	private boolean setValueToListBox(ListBox listBox, String newVal) {
		boolean found = false;
		for (int i = 0; i < listBox.getItemCount(); i++) {
			String itemText = listBox.getItemText(i);
			if (newVal.equals(itemText)) {
				listBox.setSelectedIndex(i);
				found = true;
				break;
			}
		}
		return found;
	}
	

}
