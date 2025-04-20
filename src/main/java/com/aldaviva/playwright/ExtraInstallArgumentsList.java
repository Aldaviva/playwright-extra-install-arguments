package com.aldaviva.playwright;

import java.util.ArrayList;
import java.util.List;

class ExtraInstallArgumentsList extends ArrayList<String> {

	private static final long serialVersionUID = 1L;
	private final List<String> extraInstallArguments;

	public ExtraInstallArgumentsList(final List<String> commands, final List<String> extraInstallArguments) {
		super(commands);
		this.extraInstallArguments = extraInstallArguments;
	}

	@Override
	public boolean add(final String e) {
		final boolean result = super.add(e);

		if (size() == 3 && "install".equals(e) && extraInstallArguments != null) {
			super.addAll(extraInstallArguments);
		}

		return result;
	}

}