package org.vaadin.jonni.depsel.demo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import org.vaadin.jonni.depsel.DependantSelect;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("demo")
@Title("Depsel component Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = DemoUI.class)
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		CheckBox showAllCheckbox = new CheckBox("Show all options in second field", true);

		final HorizontalLayout layout = new HorizontalLayout();

		NativeSelect<String> firstField = new NativeSelect<>();
		firstField.setWidth("56px");

		DependantSelect secondField = new DependantSelect(firstField);
		secondField.setWidth("56px");

		setAllOptionsToSelects(secondField);

		showAllCheckbox.addValueChangeListener(change -> {
			if (showAllCheckbox.getValue()) {
				setAllOptionsToSelects(secondField);
			} else {

				setLimitedOptionsToPair(secondField);
			}
		});

		Button button = new Button("Show field values", click -> Notification
				.show("First field: " + firstField.getValue() + ", second: " + secondField.getValue()));
		layout.addComponents(new TextField(), firstField, secondField, button);
		layout.setComponentAlignment(button, Alignment.BOTTOM_LEFT);
		layout.setMargin(true);
		Button serverSideSetterButton = new Button("Set input values from server-side", click -> {
			Random r = new Random();
			HashMap<String, List<String>> mappings;
			boolean showAll = r.nextBoolean();
			if (showAll) {
				showAllCheckbox.setValue(true);
				mappings = getFullMappings();
			} else {
				showAllCheckbox.setValue(false);
				mappings = getLimitedMappings();
			}
			Set<Entry<String, List<String>>> entrySet = mappings.entrySet();
			int firstValIdx = r.nextInt(entrySet.size());

			String secondValue = "";
			String firstValue = "";
			int i = 0;
			for (Entry<String, List<String>> entry : entrySet) {
				if (i == firstValIdx) {
					firstValue = entry.getKey();
					List<String> list = entry.getValue();
					int secondValIIdx = r.nextInt(list.size());
					secondValue = list.get(secondValIIdx);
					break;
				}
				i++;
			}
			firstField.setValue(firstValue);
			secondField.setValue(secondValue);
			String msg = "Setting values from server-side to " + firstValue + " - " + secondValue + " showing all: " + showAll;
			System.out.println(msg);
			Notification.show(msg, Type.TRAY_NOTIFICATION);
		});
		VerticalLayout mockForm = new VerticalLayout(new HorizontalLayout(showAllCheckbox, serverSideSetterButton,
				
				new Button("1", click -> {
					showAllCheckbox.setValue(false);
					firstField.setValue("B");
					secondField.setValue("B");
		}
		),
				
				new Button("2", click -> {
					firstField.setValue("B");
					secondField.setValue("B");
		}
		),
				
				new Button("3", click -> {
					firstField.setValue("C");
					secondField.setValue("A");
		}
		)
				
				),
				layout);
		setContent(mockForm);
	}

	private void setLimitedOptionsToPair(DependantSelect dependantSelectPair) {
		HashMap<String, List<String>> valueMapping = getLimitedMappings();
		dependantSelectPair.setOptionMapping(valueMapping);
	}

	private void setAllOptionsToSelects(DependantSelect dependantSelect) {
		HashMap<String, List<String>> valueMapping = getFullMappings();
		dependantSelect.setOptionMapping(valueMapping);
	}

	private HashMap<String, List<String>> getFullMappings() {
		HashMap<String, List<String>> valueMapping = new HashMap<>();
		valueMapping.put("A", Arrays.asList("A"));
		valueMapping.put("B", Arrays.asList("B"));
		valueMapping.put("C", Arrays.asList("A", "B", "C", "D"));
		valueMapping.put("D", Arrays.asList("A", "B", "C", "D"));
		return valueMapping;
	}

	private HashMap<String, List<String>> getLimitedMappings() {
		HashMap<String, List<String>> valueMapping = new HashMap<>();
		valueMapping.put("A", Arrays.asList("A"));
		valueMapping.put("B", Arrays.asList("B"));
		valueMapping.put("C", Arrays.asList("A", "B"));
		valueMapping.put("D", Arrays.asList("A", "B"));
		return valueMapping;
	}
}
