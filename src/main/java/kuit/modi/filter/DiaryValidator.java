package kuit.modi.filter;

import kuit.modi.exception.CustomException;
import kuit.modi.exception.DiaryExceptionResponseStatus;

public final class DiaryValidator {

    private DiaryValidator() {}

    public static void validateYearMonth(Integer year, Integer month) {
        if (year == null || month == null || month < 1 || month > 12) {
            throw new CustomException(DiaryExceptionResponseStatus.INVALID_YEAR_MONTH);
        }
    }

    public static void validateBounds(Double swLat, Double swLng, Double neLat, Double neLng) {
        boolean hasNull = (swLat == null || swLng == null || neLat == null || neLng == null);
        if (hasNull) {
            throw new CustomException(DiaryExceptionResponseStatus.INVALID_LOCATION);
        }

        boolean inRange = inLatRange(swLat) && inLatRange(neLat) && inLngRange(swLng) && inLngRange(neLng);
        boolean properRect = swLat < neLat && swLng < neLng;

        if (!inRange || !properRect) {
            throw new CustomException(DiaryExceptionResponseStatus.INVALID_LOCATION);
        }
    }

    private static boolean inLatRange(double v) {
        return v >= -90 && v <= 90;
    }

    private static boolean inLngRange(double v) {
        return v >= -180 && v <= 180;
    }
}
