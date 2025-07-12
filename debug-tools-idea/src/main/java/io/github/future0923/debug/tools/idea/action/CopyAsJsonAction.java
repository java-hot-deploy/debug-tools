package io.github.future0923.debug.tools.idea.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.sun.jdi.*;
import io.github.future0923.debug.tools.idea.utils.DebugToolsIcons;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author caoayu
 */
public class CopyAsJsonAction extends AnAction {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();

    private static final Set<String> JDK_PACKAGES = new HashSet<String>() {{
        add("java.");
        add("javax.");
        add("sun.");
        add("com.sun.");
    }};
    private static final Set<String> WRAPPER_TYPES = new HashSet<String>() {{
        add("java.lang.Integer");
        add("java.lang.Long");
        add("java.lang.Double");
        add("java.lang.Float");
        add("java.lang.Short");
        add("java.lang.Byte");
        add("java.lang.Boolean");
        add("java.lang.Character");
    }};
    private static final int MAX_DEPTH = 5;
    private final Set<Integer> visitedObjects = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private int currentDepth = 0;

    public CopyAsJsonAction() {
        getTemplatePresentation().setText("Copy As Json");
        getTemplatePresentation().setIcon(DebugToolsIcons.Json);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        List<XValueNodeImpl> selectedNodes = XDebuggerTree.getSelectedNodes(e.getDataContext());
        if (selectedNodes.isEmpty()) return;
        if (selectedNodes.size() > 1) {
            Messages.showWarningDialog(project, "Only select one node can be copied as JSON.", "Copy As JSON");
            return;
        }

        XValueNodeImpl node = selectedNodes.get(0);
        if (!(node.getValueContainer() instanceof JavaValue)) {
            Messages.showWarningDialog(project, "Only Java objects can be copied as JSON.", "Copy As JSON");
            return;
        }

        JavaValue javaValue = (JavaValue) node.getValueContainer();
        Value jdiVal = javaValue.getDescriptor().getValue();
        if (!(jdiVal instanceof ObjectReference)) {
            Messages.showWarningDialog(project, "Primitive/array values are not supported.", "Copy As JSON");
            return;
        }

        ObjectReference objRef = (ObjectReference) jdiVal;
        try {
            visitedObjects.clear();
            currentDepth = 0;
            Object jsonObject = toJsonObject(objRef);
            String json = GSON.toJson(jsonObject);
            CopyPasteManager.getInstance().setContents(new StringSelection(json));
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("DebugTools")
                    .createNotification("Copied as JSON", "", NotificationType.INFORMATION)
                    .notify(project);
        } catch (Exception ex) {
            Messages.showErrorDialog(project, "Failed to convert to JSON: " + ex.getMessage(), "Copy As JSON");
        } finally {
            visitedObjects.clear();
        }
    }

    private Object toJsonObject(ObjectReference ref) {
        if (ref == null || currentDepth > MAX_DEPTH) {
            return "<max depth reached>";
        }

        int hashCode = System.identityHashCode(ref);
        if (visitedObjects.contains(hashCode)) {
            return "<cyclic reference>";
        }
        visitedObjects.add(hashCode);

        try {
            currentDepth++;
            if (ref instanceof ArrayReference) {
                return handleArray((ArrayReference) ref);
            } else if (ref instanceof StringReference) {
                return ((StringReference) ref).value();
            } else if (isCollectionType(ref)) {
                return handleCollection(ref);
            } else if (isMapType(ref)) {
                return handleMap(ref);
            } else if (isJdkInternalClass(ref.referenceType())) {
                return "<jdk internal: " + ref.referenceType().name() + ">";
            } else {
                return handleObject(ref);
            }
        } finally {
            visitedObjects.remove(hashCode);
            currentDepth--;
        }
    }

    private List<Object> handleArray(ArrayReference arrayRef) {
        List<Object> list = new ArrayList<>();
        for (Value value : arrayRef.getValues()) {
            list.add(renderValue(value));
        }
        return list;
    }

    private List<Object> handleCollection(ObjectReference ref) {
        try {
            Field sizeField = ref.referenceType().fieldByName("size");
            IntegerValue sizeValue = (IntegerValue) ref.getValue(sizeField);
            int size = sizeValue != null ? sizeValue.value() : 0;

            Field elementDataField = ref.referenceType().fieldByName("elementData");
            ArrayReference elementData = (ArrayReference) ref.getValue(elementDataField);

            List<Object> list = new ArrayList<>(size);
            for (int i = 0; i < size && i < elementData.length(); i++) {
                Value element = elementData.getValue(i);
                list.add(renderValue(element));
            }
            return list;
        } catch (Exception e) {
            return Collections.singletonList("<error: " + e.getMessage() + ">");
        }
    }

    private Map<String, Object> handleObject(ObjectReference ref) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Field field : ref.referenceType().allFields()) {
            try {
                if (!isStaticField(field)) {
                    Value value = ref.getValue(field);
                    map.put(field.name(), renderValue(value));
                }
            } catch (Exception e) {
                map.put(field.name(), "<error: " + e.getMessage() + ">");
            }
        }
        return map;
    }

    private Map<Object, Object> handleMap(ObjectReference ref) {
        try {
            ThreadReference thread = getDebugThread(ref);
            if (thread == null) {
                return Collections.singletonMap("error", "no debug thread available");
            }

            Field entrySetField = ref.referenceType().fieldByName("entrySet");
            ObjectReference entrySet = (ObjectReference) ref.getValue(entrySetField);

            Method iteratorMethod = entrySet.referenceType().methodsByName("iterator").get(0);
            ObjectReference iterator = (ObjectReference) entrySet.invokeMethod(
                    thread, iteratorMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);

            Map<Object, Object> map = new LinkedHashMap<>();
            while (true) {
                Method hasNextMethod = iterator.referenceType().methodsByName("hasNext").get(0);
                BooleanValue hasNext = (BooleanValue) iterator.invokeMethod(
                        thread, hasNextMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);

                if (!hasNext.value()) break;

                Method nextMethod = iterator.referenceType().methodsByName("next").get(0);
                ObjectReference entry = (ObjectReference) iterator.invokeMethod(
                        thread, nextMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);

                Field keyField = entry.referenceType().fieldByName("key");
                Field valueField = entry.referenceType().fieldByName("value");

                Object key = renderValue(entry.getValue(keyField));
                Object value = renderValue(entry.getValue(valueField));

                map.put(key, value);
            }
            return map;
        } catch (Exception e) {
            return Collections.singletonMap("error", e.getMessage());
        }
    }

    private Object renderValue(Value v) {
        if (v == null) {
            return null;
        }
        // 处理基本类型包装类
        if (v instanceof ObjectReference) {
            ObjectReference objRef = (ObjectReference) v;
            String className = objRef.referenceType().name();

            if (WRAPPER_TYPES.contains(className)) {
                try {
                    // 获取包装类的实际值
                    Field valueField = objRef.referenceType().fieldByName("value");
                    Value primitiveValue = objRef.getValue(valueField);
                    return renderPrimitiveValue(primitiveValue);
                } catch (Exception e) {
                    return "<error: " + e.getMessage() + ">";
                }
            }

            // 处理其他对象引用
            return toJsonObject(objRef);
        }
        // 处理基本类型
        return renderPrimitiveValue(v);
    }
    private Object renderPrimitiveValue(Value v) {
        if (v instanceof BooleanValue) return ((BooleanValue) v).value();
        if (v instanceof ByteValue) return ((ByteValue) v).value();
        if (v instanceof CharValue) return ((CharValue) v).value();
        if (v instanceof DoubleValue) return ((DoubleValue) v).value();
        if (v instanceof FloatValue) return ((FloatValue) v).value();
        if (v instanceof IntegerValue) return ((IntegerValue) v).value();
        if (v instanceof LongValue) return ((LongValue) v).value();
        if (v instanceof ShortValue) return ((ShortValue) v).value();
        if (v instanceof StringReference) return ((StringReference) v).value();
        return v.toString();
    }
    private boolean isCollectionType(ObjectReference ref) {
        try {
            String className = ref.referenceType().name();
            return className.contains("List") ||
                    className.contains("Set") ||
                    className.contains("Queue") ||
                    className.contains("Collection");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isMapType(ObjectReference ref) {
        try {
            return ref.referenceType().name().contains("Map");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isJdkInternalClass(ReferenceType refType) {
        String className = refType.name();
        for (String pkg : JDK_PACKAGES) {
            if (className.startsWith(pkg) && !WRAPPER_TYPES.contains(className)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStaticField(Field field) {
        return field.isStatic();
    }

    private ThreadReference getDebugThread(ObjectReference ref) {
        try {
            VirtualMachine vm = ref.virtualMachine();
            for (ThreadReference thread : vm.allThreads()) {
                if (thread.isSuspended()) {
                    return thread;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        boolean isVisible = XDebuggerManager.getInstance(project).getCurrentSession() != null;
        e.getPresentation().setEnabledAndVisible(isVisible);
    }
}