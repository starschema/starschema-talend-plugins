package org.talend.core.repository.model.listeners;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.talend.core.model.properties.Item;
import org.talend.repository.documentation.ERepositoryActionName;

/**
 * 
 * DOC talend class global comment. Detailled comment
 */
public abstract class AbstractJobCreateListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent event) {

        if (!event.getPropertyName().equals(ERepositoryActionName.JOB_CREATE.getName())) {
            return;
        }

        if (!(event.getNewValue() instanceof Item)) {
            return;
        }

        final Item item = (Item) event.getNewValue();
        execute(item);
    }

    public abstract void execute(Item item);
}
