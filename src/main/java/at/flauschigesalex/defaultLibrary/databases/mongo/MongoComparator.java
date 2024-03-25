package at.flauschigesalex.defaultLibrary.databases.mongo;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public final class MongoComparator implements Comparator<Class<?>> {

    MongoComparator() {
    }

    public int compare(final @NotNull Class<?> m1, final @NotNull Class<?> m2) {
        final int min = Math.min(m1.getName().length(), m2.getName().length());
        for (int size = 0; size < min; size++) {
            final int[] weight = new int[]{weight(m1.getName().charAt(size)), weight(m2.getName().charAt(size))};

            if (weight[0] != weight[1])
                return Integer.compare(weight[0], weight[1]);
            if (size != (min-1))
                continue;
            return Integer.compare(m1.getName().length(), m2.getName().length());
        }
        return 0;
    }

    private int weight(final char c) {
        final String regex = ".0123456789aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ_";
        for (int i = 0; i < regex.length(); i++)
            if (regex.charAt(i) == c)
                return i;
        return 999999;
    }
}
