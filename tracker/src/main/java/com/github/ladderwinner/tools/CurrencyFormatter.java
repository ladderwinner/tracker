/*
 * Android SDK for LadderWinner
 *
 * @link https://github.com/LadderWinner-org/LadderWinner-android-sdk
 * @license https://github.com/LadderWinner-org/LadderWinner-sdk-android/blob/master/LICENSE BSD-3 Clause
 */

package com.github.ladderwinner.tools;

import androidx.annotation.Nullable;

import java.math.BigDecimal;

public class CurrencyFormatter {
    @Nullable
    public static String priceString(@Nullable Integer cents) {
        if (cents == null) return null;
        return new BigDecimal(cents).movePointLeft(2).toPlainString();
    }
}
