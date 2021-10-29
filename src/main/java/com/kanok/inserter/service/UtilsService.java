package com.kanok.inserter.service;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.SplittableRandom;

public class UtilsService {

    private static final int TITLE_IN_URL_MAX_LENGTH = 70;
    public static final SplittableRandom splittableRandom = new SplittableRandom();

    private UtilsService() {
    }

    public static String makeFriendlyUrl(String name) {
        String friendlyUrl = "";
        String titleInUrl = StringUtils.stripAccents(name);

        titleInUrl = titleInUrl.replaceAll("[\\-| |\\.]+", "-").toLowerCase();

        if (titleInUrl.length() > TITLE_IN_URL_MAX_LENGTH) {
            friendlyUrl = (titleInUrl.substring(0, TITLE_IN_URL_MAX_LENGTH));
        } else {
            friendlyUrl = titleInUrl;
        }
        return friendlyUrl;
    }

    public static long getRandomIdFromList(List<Long> idList) {
        return idList.get(splittableRandom.nextInt(idList.size()));
    }

    public static String getStringFromListById(List<String> string, long id) {
        return string.get((int) id);
    }

    public static int getRole(long id) {
        if (id <= 5) {
            // ROLE ADMIN
            return 1;
        } else if (id <= 50) {
            // ROLE SPECIALISTS
            return 2;
        } else {
            // ROLE USER
            return 3;
        }
    }
}
