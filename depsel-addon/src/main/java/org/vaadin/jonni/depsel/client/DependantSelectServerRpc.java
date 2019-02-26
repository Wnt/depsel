package org.vaadin.jonni.depsel.client;

import com.vaadin.shared.communication.ServerRpc;

public interface DependantSelectServerRpc extends ServerRpc {
	public void setValue(String value);
}