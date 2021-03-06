package net.minestom.server.chat;

/**
 * Represent a translatable component which can be used in {@link ColoredText}
 */
public class TranslatableText {

    private String code;
    private String[] arguments;

    private TranslatableText(String code, String[] arguments) {
        this.code = code;
        this.arguments = arguments;
    }

    /**
     * Get the translatable component of the specific code
     *
     * @param code the translatable code
     * @return the translatable component linked to the code
     */
    public static TranslatableText of(String code) {
        return new TranslatableText(code, null);
    }

    /**
     * Get the translatable component and the specific code with arguments
     *
     * @param code      the translatable code
     * @param arguments the translatable component arguments in order
     * @return the translatable component linked to the code and arguments
     */
    public static TranslatableText of(String code, String... arguments) {
        return new TranslatableText(code, arguments);
    }

    @Override
    public String toString() {
        final String prefix = "{@";
        final String suffix = "}";

        StringBuilder content = new StringBuilder(code);

        if (arguments != null && arguments.length > 0) {
            for (String arg : arguments) {
                content.append("," + arg);
            }
        }

        return prefix + content + suffix;
    }
}
