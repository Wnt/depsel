package org.vaadin.jonni.depsel.demo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import org.vaadin.jonni.depsel.DependantSelect;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
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
@Push
@PreserveOnRefresh
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
		firstField.setValue(null);
		
		DependantSelect secondField = new DependantSelect(firstField);
		secondField.setWidth("56px");

		firstField.addValueChangeListener(event -> {
			if (firstField.getValue() != null) {
				switch (firstField.getValue()) {
					case "N": secondField.setValue("N"); break;  
					case "D": secondField.setValue("D"); break;  
					case "C": secondField.setValue("C"); break;  
					case "R": secondField.setValue("R"); break;
					default: break;
				}
			}
		});
		
		secondField.addValueChangeListener(event -> {
			System.out.println(event.getValue()+" "+event.isUserOriginated());
		});
		
		
		
		setAllOptionsToSelects(secondField);

		showAllCheckbox.addValueChangeListener(change -> {
			if (showAllCheckbox.getValue()) {
				setAllOptionsToSelects(secondField);
			} else {
				setLimitedOptionsToSelects(secondField);
			}
		});
		showAllCheckbox.setValue(true);

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
					firstField.setValue("N");
					secondField.setValue("N");
		}
		),
				
				new Button("2", click -> {
					firstField.setValue("D");
					secondField.setValue("D");
		}
		),
				
				new Button("3", click -> {
					firstField.setValue("C");
					secondField.setValue("R");
		}
		)
				
				),
				layout);
		setContent(mockForm);
	}

	private void setLimitedOptionsToSelects(DependantSelect dependantSelect) {
		HashMap<String, List<String>> valueMapping = getLimitedMappings();
		dependantSelect.setOptionMapping(valueMapping);
	}

	private void setAllOptionsToSelects(DependantSelect dependantSelect) {
		HashMap<String, List<String>> valueMapping = getFullMappings();
		dependantSelect.setOptionMapping(valueMapping);
	}

	private HashMap<String, List<String>> getFullMappings() {
		HashMap<String, List<String>> valueMapping = new HashMap<>();
		valueMapping.put("N", Arrays.asList("N"));
		valueMapping.put("D", Arrays.asList("D"));
		valueMapping.put("R", Arrays.asList("N", "C", "D", "R"));
		valueMapping.put("C", Arrays.asList("N", "C", "D", "R"));
		return valueMapping;
	}

	private HashMap<String, List<String>> getLimitedMappings() {
		HashMap<String, List<String>> valueMapping = new HashMap<>();
		valueMapping.put("N", Arrays.asList("N"));
		valueMapping.put("D", Arrays.asList("D"));
		valueMapping.put("R", Arrays.asList("R", "C"));
		valueMapping.put("C", Arrays.asList("R", "C"));
		return valueMapping;
	}
}
