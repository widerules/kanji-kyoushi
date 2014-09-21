package kanjikyoushi.util;

import java.util.Collection;

public class Utilities {

    public static String join(String glue, Collection<? extends Object> contents) {
        StringBuffer buf = new StringBuffer();
        String g = "";

        if (contents != null) {
            for (Object o : contents) {
                buf.append(g + o.toString());
                g = glue;
            }
        }

        return buf.toString();
    }

}
