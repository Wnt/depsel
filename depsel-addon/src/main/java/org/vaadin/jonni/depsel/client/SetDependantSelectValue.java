package org.vaadin.jonni.depsel.client;

import com.vaadin.shared.communication.ServerRpc;

public interface SetDependantSelectValue extends ServerRpc {
	public void setValue(String value);
}