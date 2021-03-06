
package org.opencms.ui.client.contextmenu;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.vaadin.client.ui.VOverlay;

/**
 * Menu is a visible item of ContextMenu component. For every child menu new
 * instance of Menu class will be instantiated.<p>
 *
 * Adapted from ContextMenu by Peter Lehto / Vaadin Ltd.<p>
 */
@SuppressWarnings("deprecation")
class CmsContextMenuOverlay extends VOverlay {

    /** The close handler. */
    private final CloseHandler<PopupPanel> m_closeHandler = new CloseHandler<PopupPanel>() {

        @Override
        public void onClose(CloseEvent<PopupPanel> event) {

            unfocusAll();
        }
    };

    /** The close handler registration. */
    private final HandlerRegistration m_closeHandlerRegistration;

    /** The menu items. */
    private final List<CmsContextMenuItemWidget> m_menuItems;

    /** The root panel. */
    private final FlowPanel m_root;

    /**
     * Constructor.<p>
     */
    public CmsContextMenuOverlay() {
        super(false, false);

        m_closeHandlerRegistration = addCloseHandler(m_closeHandler);
        setStyleName("v-context-menu-container");

        m_root = new FlowPanel();
        m_root.setStyleName("v-context-menu");

        m_menuItems = new LinkedList<CmsContextMenuItemWidget>();

        add(m_root);
    }

    /**
     * Adds a menu item.<p>
     *
     * @param menuItem the item to add
     */
    public void addMenuItem(CmsContextMenuItemWidget menuItem) {

        menuItem.setOverlay(this);

        m_menuItems.add(menuItem);
        m_root.add(menuItem);
    }

    /**
     * Clears the menu items.<p>
     */
    public void clearItems() {

        m_menuItems.clear();
        m_root.clear();
    }

    /**
     * Closes the sub menus.<p>
     */
    public void closeSubMenus() {

        for (CmsContextMenuItemWidget child : m_menuItems) {
            child.hideSubMenu();
        }
    }

    /**
     * Returns the menu items.<p>
     *
     * @return the menu items
     */
    public List<CmsContextMenuItemWidget> getMenuItems() {

        return m_menuItems;
    }

    /**
     * Returns the number of menu items.<p>
     *
     * @return number of visible items in this menu
     */
    public int getNumberOfItems() {

        return this.m_menuItems.size();
    }

    /**
     * @see com.vaadin.client.ui.VOverlay#hide()
     */
    @Override
    public void hide() {

        unfocusAll();
        closeSubMenus();
        super.hide();
    }

    /**
     * Returns whether the sub menu is open.<p>
     *
     * @return <code>true</code> if the sub menu is open
     */
    public boolean isSubmenuOpen() {

        for (CmsContextMenuItemWidget item : m_menuItems) {
            if (item.isSubmenuOpen()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Opens next to the given parent item.<p>
     *
     * @param parentMenuItem the perent item
     */
    public void openNextTo(CmsContextMenuItemWidget parentMenuItem) {

        int left = parentMenuItem.getAbsoluteLeft() + parentMenuItem.getOffsetWidth();
        int top = parentMenuItem.getAbsoluteTop();

        showAt(left, top);
    }

    /**
     * Selects the next sibling.<p>
     *
     * @param item the item
     */
    public void selectItemAfter(CmsContextMenuItemWidget item) {

        int index = m_menuItems.indexOf(item);

        index += 1;

        if (index >= m_menuItems.size()) {
            index = 0;
        }

        CmsContextMenuItemWidget itemToSelect = m_menuItems.get(index);
        itemToSelect.setFocus(true);
    }

    /**
     * Selects the previous sibling.<p>
     *
     * @param item the item
     */
    public void selectItemBefore(CmsContextMenuItemWidget item) {

        int index = m_menuItems.indexOf(item);

        index -= 1;

        if (index < 0) {
            index = m_menuItems.size() - 1;
        }

        CmsContextMenuItemWidget itemToSelect = m_menuItems.get(index);
        itemToSelect.setFocus(true);
    }

    /**
     * Sets the focus.<p>
     *
     * @param focused the focus
     */
    public void setFocus(boolean focused) {

        unfocusAll();

        if (focused) {
            focusFirstItem();
        }
    }

    /**
     * Shows the overlay at the given position.<p>
     *
     * @param x the client x position
     * @param y the client y position
     */
    public void showAt(final int x, final int y) {

        setPopupPositionAndShow(new PopupPanel.PositionCallback() {

            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {

                int left = x + Window.getScrollLeft();
                int top = y + Window.getScrollTop();

                int exceedingWidth = (offsetWidth + left) - (Window.getClientWidth() + Window.getScrollLeft());
                if (exceedingWidth > 0) {
                    left -= exceedingWidth;
                    if (left < 0) {
                        left = 0;
                    }
                }

                int exceedingHeight = (offsetHeight + top) - (Window.getClientHeight() + Window.getScrollTop());
                if (exceedingHeight > 0) {
                    top -= exceedingHeight;
                    if (top < 0) {
                        top = 0;
                    }
                }

                setPopupPosition(left, top);
            }
        });

        normalizeItemWidths();
    }

    /**
     * Unregisters the close handler.<p>
     */
    public void unregister() {

        m_closeHandlerRegistration.removeHandler();
    }

    /**
     * Returns whether the given event targets the popup.<p>
     *
     * @param event the event to check
     *
     * @return <code>true</code> if the event targets the popup
     */
    protected boolean eventTargetsPopup(NativeEvent event) {

        EventTarget target = event.getEventTarget();
        if (Element.is(target)) {
            return getElement().isOrHasChild(Element.as(target));
        }
        return false;
    }

    /**
     * Normalizes the item width.<p>
     */
    protected void normalizeItemWidths() {

        int widestItemWidth = getWidthOfWidestItem();

        for (CmsContextMenuItemWidget item : m_menuItems) {
            if (item.getOffsetWidth() <= widestItemWidth) {
                item.setWidth(widestItemWidth + "px");
            }
        }
    }

    /**
     * Removes the focus from all items.<p>
     */
    void unfocusAll() {

        for (CmsContextMenuItemWidget item : m_menuItems) {
            item.setFocus(false);
        }
    }

    /**
     * Focuses the first item.<p>
     */
    private void focusFirstItem() {

        if (m_menuItems.size() > 0) {
            m_menuItems.iterator().next().setFocus(true);
        }
    }

    /**
     * Returns the item width.<p>
     *
     * @return the item width
     */
    private int getWidthOfWidestItem() {

        int maxWidth = 0;

        for (CmsContextMenuItemWidget item : m_menuItems) {
            int itemWidth = item.getOffsetWidth() + 1;

            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }

        return maxWidth;
    }
}
