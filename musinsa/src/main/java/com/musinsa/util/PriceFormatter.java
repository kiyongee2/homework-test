package com.musinsa.util;

import java.text.DecimalFormat;

/**
 * 가격을 천 단위 콤마 문자열로 변환하는 유틸리티. (예: 10000 -> "10,000")
 */
public final class PriceFormatter {

    private static final DecimalFormat FORMAT = new DecimalFormat("#,###");

    private PriceFormatter() {
    }

    public static String format(long price) {
        return FORMAT.format(price);
    }
}
