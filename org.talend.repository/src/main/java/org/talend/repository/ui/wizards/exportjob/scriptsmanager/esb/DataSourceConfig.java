package org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb;

import java.net.URI;
import java.util.Collection;

import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.repository.utils.EmfModelUtils;

// OSGi DataSource
public class DataSourceConfig {

	private static final String DB_JDBC = "tJDBCConnection"; //$NON-NLS-1$
	private static final String DB_MYSQL = "tMysqlConnection"; //$NON-NLS-1$

	private String additionalJobBeanParams = ""; //$NON-NLS-1$
	private String additionalJobBundleConfig = ""; //$NON-NLS-1$

    public static boolean isDBConnectionJob(ProcessItem processItem) {
        return !getDBConnectionComponents(processItem).isEmpty();
    }

    private static Collection<NodeType> getDBConnectionComponents(ProcessItem processItem) {
    	return EmfModelUtils.getComponentsByName(processItem, DB_JDBC, DB_MYSQL);
    }

	public DataSourceConfig(ProcessItem processItem) {
        Collection<NodeType> dbComponents = getDBConnectionComponents(processItem);
        if (!dbComponents.isEmpty()) {
        	additionalJobBeanParams +=
        		  "\n\t\t<property name=\"dataSources\">" 
        		+ "\n\t\t\t<map>";
        	
        	for (NodeType dbComponent : dbComponents) {
        		String id = EmfModelUtils.computeTextElementValue("UNIQUE_NAME", dbComponent);
        		String beanPool = id + "Pool";

        		additionalJobBeanParams +=
            		"\n\t\t\t\t<entry key=\"" + id + "\" value-ref=\"" + beanPool + "\" />";

        		String beanDataSource = id + "DataSource";
				additionalJobBundleConfig += getDataSourceConfig(dbComponent, beanDataSource);
				additionalJobBundleConfig +=
			                "\n\t<bean id=\"" + beanPool + "\" class=\"org.apache.commons.dbcp.datasources.SharedPoolDataSource\" destroy-method=\"close\">"
	                      + "\n\t\t<property name=\"connectionPoolDataSource\" ref=\"" + beanDataSource + "\"/>"
	                      + "\n\t</bean>";
			}
        	additionalJobBeanParams +=
        		  "\n\t\t\t</map>"
        		+ "\n\t\t</property>";
        }
	}

	public String getAdditionalJobBeanParams() {
		return additionalJobBeanParams;
	}

	public String getAdditionalJobBundleConfig() {
		return additionalJobBundleConfig;
	}

	private static String getDataSourceConfig(NodeType dbComponent, String beanDataSource) {
		String componentName = dbComponent.getComponentName();
		if(DB_JDBC.equals(componentName)) {
    		String url = EmfModelUtils.computeTextElementValue("URL", dbComponent);
			URI jdbcURI = URI.create(url.substring("jdbc:".length()));
			if ("derby".equals(jdbcURI.getScheme())) {
	    		return "\n\t<bean id=\"" + beanDataSource + "\" class=\"org.apache.derby.jdbc.ClientConnectionPoolDataSource\">"
	    	              + "\n\t\t<property name=\"serverName\" value=\"" + jdbcURI.getHost() + "\"/>"
	    	              + "\n\t\t<property name=\"portNumber\" value=\"" + jdbcURI.getPort() + "\"/>"
	    	              + "\n\t\t<property name=\"databaseName\" value=\"" + jdbcURI.getPath() + "\"/>"
	    	              + "\n\t\t<property name=\"user\" value=\"" + EmfModelUtils.computeTextElementValue("USER", dbComponent) + "\"/>"
	    	              + "\n\t\t<property name=\"password\" value=\"" + EmfModelUtils.computeTextElementValue("PASS", dbComponent) + "\"/>"
	    	              + "\n\t</bean>";
			} else if ("mysql".equals(jdbcURI.getScheme())) {
				return getMysqlDataSourceConfig(dbComponent, beanDataSource, url);
			} else {
				throw new IllegalArgumentException("Unsupported database for tJDBCConnection component: " + jdbcURI.getScheme());
			}
		} else if(DB_MYSQL.equals(componentName)) {
			return getMysqlDataSourceConfig(dbComponent, beanDataSource, null);
		} else {
			throw new IllegalArgumentException("Unsupported database component: " + componentName);
		}
	}

	private static String getMysqlDataSourceConfig(NodeType dbComponent, String beanDataSource, String url) {
		if (null == url) {
			url = "jdbc:mysql://" + EmfModelUtils.computeTextElementValue("HOST", dbComponent)
					+ ':' + EmfModelUtils.computeTextElementValue("PORT", dbComponent)
					+ '/' + EmfModelUtils.computeTextElementValue("DBNAME", dbComponent);
		}
		return "\n\t<bean id=\"" + beanDataSource + "\" class=\"com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource\">"
	        + "\n\t\t<property name=\"url\" value=\"" + url + "\"/>"
	        + "\n\t\t<property name=\"user\" value=\"" + EmfModelUtils.computeTextElementValue("USER", dbComponent) + "\"/>"
	        + "\n\t\t<property name=\"password\" value=\"" + EmfModelUtils.computeTextElementValue("PASS", dbComponent) + "\"/>"
	        + "\n\t</bean>";
	}

}
