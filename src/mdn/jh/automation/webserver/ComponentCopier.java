package mdn.jh.automation.webserver;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.logic.components.custom.CustomComponent;
import mdn.jh.automation.io.sink.DataSink;
import mdn.jh.automation.io.source.DataSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Copies settings using the storage format, with fresh IDs and no external edges. */
public final class ComponentCopier {
    private ComponentCopier() { }

    public static DataComponentStub copy(DataComponentStub original) throws Exception {
        if (!(original instanceof LogicBase || original instanceof DataSource || original instanceof DataSink))
            throw new IllegalArgumentException("Unsupported component type");
        if (original instanceof DataSource source && source.isEmbedded())
            throw new IllegalArgumentException("Copy the containing custom component instead");
        Element settings = CustomComponent.settingsOnly(original);
        DataComponentStub copy = (DataComponentStub) original.getClass().getConstructor().newInstance();
        try {
            String id = Integer.toString(copy.getId());
            settings.setAttribute("id", id);
            for (Node child = settings.getFirstChild(); child != null; child = child.getNextSibling())
                if (child instanceof Element base && "DataComponent".equals(base.getNodeName())) base.setAttribute("id", id);
            if (copy instanceof CustomComponent custom) custom.initCopy(settings);
            else if (copy instanceof DataSink sink) {
                sink.initSpecific(settings);
                for (Node child = settings.getFirstChild(); child != null; child = child.getNextSibling())
                    if ("DataComponent".equals(child.getNodeName())) sink.initDataComponent(child);
            } else copy.initDataComponent(settings);
            return copy;
        } catch (Exception error) {
            if (copy instanceof LogicBase logic) logic.shutdownRuntime();
            throw error;
        }
    }
}
