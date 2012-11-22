package org.talend.core;
import java.util.Map;

import org.eclipse.emf.common.util.EMap;
import org.talend.core.model.properties.ProcessItem;

public interface IOsgiDependenciesService extends IService{

	final String IMPORT_PACKAGE = "import_package";
	final String REQUIRE_BUNDLE = "require_bundle";
	final String BUNDLE_CLASSPATH = "bundle_classpath";
	final String ITEM_SEPARATOR = ",";
	
	/**
	 * key is {@link #IMPORT_PACKAGE} {@link #REQUIRE_BUNDLE} {@link #BUNDLE_CLASSPATH}
	 * @return
	 */
	public Map<String, String> getBundleDependences(ProcessItem pi, EMap<?, ?> additionProperties);
}