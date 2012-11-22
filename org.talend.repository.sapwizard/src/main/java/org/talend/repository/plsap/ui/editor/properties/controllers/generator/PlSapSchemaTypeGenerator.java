
package org.talend.repository.plsap.ui.editor.properties.controllers.generator;

import org.talend.core.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.designer.core.ui.editor.properties.controllers.SchemaTypeController;
import org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator;

public class PlSapSchemaTypeGenerator implements IControllerGenerator {

	private IDynamicProperty dp;


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator#generate()
	 */
	public AbstractElementPropertySectionController generate() {
		return new SchemaTypeController(dp);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator#setDynamicProperty(org.talend.designer.core.ui.editor
	 * .properties.controllers.generator.IDynamicProperty)
	 */
	public void setDynamicProperty(IDynamicProperty dp) {
		this.dp = dp;
	}
}
