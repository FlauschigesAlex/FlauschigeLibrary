package at.flauschigesalex.defaultLibrary.database.mongo;

import java.util.Comparator;

public final class MongoInformationClassComparator implements Comparator<Class<?>> {

    MongoInformationClassComparator() {
    }

    public int compare(Class<?> mongoInformation1, Class<?> mongoInformation2) {
        int min = Math.min(mongoInformation1.getName().length(), mongoInformation2.getName().length());
        for (int size = 0; size < min; size++) {
            if (weight(mongoInformation1.getName().charAt(size)) > weight(mongoInformation2.getName().charAt(size))) {
                return 1;
            } else if (weight(mongoInformation1.getName().charAt(size)) < weight(mongoInformation2.getName().charAt(size))) {
                return -1;
            }
            if (size == min - 1) {
                if (mongoInformation1.getName().length() > mongoInformation2.getName().length()) {
                    return 1;
                } else if (mongoInformation1.getName().length() < mongoInformation2.getName().length()) {
                    return -1;
                }
            }
        }
        return 0;
    }

    private int weight(char c) {
        String regex = ".0123456789aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ_";
        for (int i = 0; i < regex.length(); i++) {
            if (regex.charAt(i) != c) continue;
            return i;
        }
        return 999999;
    }
}
