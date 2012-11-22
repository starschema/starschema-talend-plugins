// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.palette;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.PaletteMessages;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPage;
import org.talend.repository.ui.actions.ShowFavoriteAction;
import org.talend.repository.ui.actions.ShowStandardAction;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class TalendFlyoutPaletteComposite extends FlyoutPaletteComposite {

    private static Logger log = Logger.getLogger(TalendFlyoutPaletteComposite.class);

    private static Field paletteContainerField;

    private static Class paletteCompositeClass;

    private static Class changeDockActionClass, resizeActionClass;
    // reflection is used to work our way around some limitation in the FlyoutPaletteComposite upper class
    // retreive method and field in a static way to avoid doing it everytime they are called and improve perf
    static {
        try {
            paletteContainerField = FlyoutPaletteComposite.class.getDeclaredField("paletteContainer"); //$NON-NLS-1$
            paletteContainerField.setAccessible(true);

            Class<?>[] classes = FlyoutPaletteComposite.class.getDeclaredClasses();
            if (classes != null) {
                for (int i = 0; i < classes.length; i++) {
                    if (classes[i].getSimpleName().equals("PaletteComposite")) { //$NON-NLS-1$
                        paletteCompositeClass = classes[i];
                    } else if (classes[i].getSimpleName().equals("ChangeDockAction")) { //$NON-NLS-1$
                        changeDockActionClass = classes[i];
                    } else if (classes[i].getSimpleName().equals("ResizeAction")) { //$NON-NLS-1$
                        resizeActionClass = classes[i];
                    }
                }
            }
        } catch (SecurityException e) {
            handleReflectionFailure(e);
        } catch (NoSuchFieldException e) {
            handleReflectionFailure(e);
        }
    }

    /**
     * DOC Administrator TalendFlyoutPaletteComposite constructor comment.
     * 
     * @param parent
     * @param style
     * @param page
     * @param pvProvider
     * @param preferences
     */
    public TalendFlyoutPaletteComposite(Composite parent, int style, IWorkbenchPage page, PaletteViewerProvider pvProvider,
            FlyoutPreferences preferences) {
        super(parent, style, page, pvProvider, preferences);

        // FIXME ggu
        // use the following codes to add two menu on the palette in Method init of class TitleCanvas
        try {
            // get paletteContainer field
            Object paletteContainer = paletteContainerField.get(this);

            /*
             * reflect the title field of PaletteComposite
             */
            Field titleField = paletteCompositeClass.getDeclaredField("title"); //$NON-NLS-1$
            titleField.setAccessible(true);
            Control title = (Control) titleField.get(paletteContainer);

            // set menu
            Menu titleCanvasMenu = createActionMenu((Control) paletteContainer);
            Method setMenuMethod = Control.class.getDeclaredMethod("setMenu", Menu.class); //$NON-NLS-1$
            setMenuMethod.invoke(title, titleCanvasMenu);

        } catch (IllegalArgumentException e) {
            handleReflectionFailure(e);
        } catch (IllegalAccessException e) {
            handleReflectionFailure(e);
        } catch (SecurityException e) {
            handleReflectionFailure(e);
        } catch (NoSuchMethodException e) {
            handleReflectionFailure(e);
        } catch (InstantiationException e) {
            handleReflectionFailure(e);
        } catch (InvocationTargetException e) {
            handleReflectionFailure(e);
        } catch (NullPointerException e) {
            handleReflectionFailure(e);
        } catch (NoSuchFieldException e) {
            handleReflectionFailure(e);
        }
    }

    private Menu createActionMenu(Control parent) throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        final MenuManager manager = new MenuManager();
        MenuManager mgr = new MenuManager(PaletteMessages.DOCK_LABEL);
        changeDockActionClass.getDeclaredConstructors();
        // reflect the constructor of ChangeDockAction
        Constructor changeDockActionConstructor = changeDockActionClass.getDeclaredConstructor(FlyoutPaletteComposite.class,
                String.class, int.class);
        changeDockActionConstructor.setAccessible(true);
        // instance of ChangeDockAction for left
        Object changeDockActionInstance = changeDockActionConstructor.newInstance(this, PaletteMessages.LEFT_LABEL,
                PositionConstants.WEST);
        if (changeDockActionInstance instanceof IAction) {
            mgr.add((IAction) changeDockActionInstance);
        }
        // instance of ChangeDockAction for right
        changeDockActionInstance = changeDockActionConstructor.newInstance(this, PaletteMessages.RIGHT_LABEL,
                PositionConstants.EAST);
        if (changeDockActionInstance instanceof IAction) {
            mgr.add((IAction) changeDockActionInstance);
        }
        // instance of ResizeAction
        Constructor resizeActionConstructor = resizeActionClass.getDeclaredConstructor(FlyoutPaletteComposite.class);
        resizeActionConstructor.setAccessible(true);
        Object resizeActionInstance = resizeActionConstructor.newInstance(this);
        if (resizeActionInstance instanceof IAction) {
            mgr.add((IAction) resizeActionInstance);
        }
        manager.add(mgr);
        mgr.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager menuMgr) {
                IContributionItem[] items = menuMgr.getItems();
                for (int i = 0; i < items.length; i++) {
                    ((ActionContributionItem) items[i]).update();
                }
            }
        });

        //
        ShowStandardAction showStandardAction = ShowStandardAction.getInstance();
        ShowFavoriteAction showFavoriteAction = ShowFavoriteAction.getInstance();
        manager.add(showStandardAction);
        manager.add(showFavoriteAction);
        showStandardAction.doSetEnable();
        manager.add(mgr);

        addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                manager.dispose();

            }
        });
        return manager.createContextMenu(this);
    }

    /**
     * log the exception and throw a Runtime exception cause this is serious.
     * 
     * @param e
     */
    private static void handleReflectionFailure(Exception e) {
        // our hook is not working so say it
        log.error("Talend Editor hook failed", e);
        throw new RuntimeException(e);

    }

}
