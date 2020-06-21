import java.util.LinkedList;
import java.util.List;

/**
 * Helper class to generate a .java enum
 */
public class EnumGenerator {

    private static final String COMMENT = "//==============================\n//  AUTOGENERATED BY "+EnumGenerator.class.getSimpleName()+"\n//==============================";

    private final String enumName;
    private String[] parameters;
    private List<Method> methods = new LinkedList<>();
    private List<Instance> instances = new LinkedList<>();
    private String enumPackage;

    public EnumGenerator(String packageName, String enumName) {
        this.enumPackage = packageName;
        parameters = new String[0];
        this.enumName = enumName;
    }

    public void setParams(String... parameters) {
        this.parameters = parameters;
    }

    public void addMethod(String name, String returnType, String... lines) {
        methods.add(new Method(name, returnType, lines));
    }

    public void addInstance(String name, Object... parameters) {
        instances.add(new Instance(name, parameters));
    }

    public String generate() {
        StringBuilder builder = new StringBuilder();
        builder.append(COMMENT);
        builder.append("\npackage ").append(enumPackage).append(";\n");
        builder.append("\npublic enum ").append(enumName).append(" {\n");

        // generate instances
        for(Instance instance : instances) {
            builder.append("\t");
            builder.append(instance.name).append("(");
            Object[] objects = instance.parameters;
            for (int i = 0; i < objects.length; i++) {
                Object param = objects[i];
                if(i != 0) {
                    builder.append(", ");
                }
                builder.append(param.toString());
            }
            builder.append("),\n");
        }
        builder.append(";\n");


        // generate properties & constructor
        if(parameters.length != 0) {
            // properties
            for(String property : parameters) {
                builder.append("\t");
                builder.append("private ").append(property).append(";\n");
            }
            builder.append("\n");

            // constructor
            builder.append("\t");
            builder.append(enumName).append("(");
            for (int i = 0; i < parameters.length; i++) {
                if(i != 0) {
                    builder.append(", ");
                }
                builder.append(parameters[i]);
            }
            builder.append(") {\n");

            // property assignment
            for(String property : parameters) {
                String[] parts = property.split(" ");
                String type = parts[0];
                String name = parts[1];
                builder.append("\t\t");
                builder.append("this.").append(name).append(" = ").append(name).append(";\n");
            }

            builder.append("\t}\n");
        }

        // generate methods
        for(Method m : methods) {
            builder.append("\n");
            builder.append("\tpublic ");
            builder.append(m.returnType).append(" ").append(m.name).append("() {\n");

            for(String line : m.lines) {
                builder.append("\t\t").append(line).append("\n");
            }

            builder.append("\t}\n");
        }

        builder.append("}\n");
        return builder.toString();
    }

    public void setEnumPackage(String enumPackage) {
        this.enumPackage = enumPackage;
    }

    private class Method {
        private String name;
        private String returnType;
        private String[] lines;

        private Method(String name, String returnType, String[] lines) {
            this.name = name;
            this.returnType = returnType;
            this.lines = lines;
        }
    }

    private class Instance {
        private String name;
        private Object[] parameters;

        private Instance(String name, Object[] parameters) {
            this.name = name;
            this.parameters = parameters;
        }
    }
}