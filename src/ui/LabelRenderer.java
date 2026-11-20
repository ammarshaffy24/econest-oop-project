package ui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

// Shows a combo box item using a chosen piece of text instead of its toString()
class LabelRenderer<T> extends DefaultListCellRenderer {
    private final Function<T, String> label;

    LabelRenderer(Function<T, String> label) {
        this.label = label;
    }

    @Override
    @SuppressWarnings("unchecked")   // the combo box only ever holds T items
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean selected, boolean focused) {
        String text = value == null ? "" : label.apply((T) value);
        return super.getListCellRendererComponent(list, text, index, selected, focused);
    }
}
