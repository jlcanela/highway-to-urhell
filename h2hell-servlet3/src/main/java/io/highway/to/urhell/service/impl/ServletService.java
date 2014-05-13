package io.highway.to.urhell.service.impl;

import io.highway.to.urhell.domain.EntryPathData;
import io.highway.to.urhell.domain.FrameworkEnum;
import io.highway.to.urhell.domain.FrameworkInformations;
import io.highway.to.urhell.service.LeechService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletService implements LeechService {

	private static final Logger LOG = LoggerFactory
			.getLogger(ServletService.class);
	private List<EntryPathData> listData;

	@Override
	public void receiveData(Object incoming) {
		listData = new ArrayList<EntryPathData>();
		Map<String, ? extends ServletRegistration> map = (Map<String, ? extends ServletRegistration>) incoming;
		for (String key : map.keySet()) {
			LOG.error("Registered Servlet: " + map.get(key).getName());
		}
	}

	@Override
	public FrameworkInformations getFrameworkInformations() {
		FrameworkInformations fwk = new FrameworkInformations();
		fwk.setFrameworkEnum(FrameworkEnum.SERVLET_3);
		fwk.setVersion("OPENJAR");
		fwk.setDetails("openjar");
		fwk.setListEntryPath(listData);
		return fwk;
	}

}