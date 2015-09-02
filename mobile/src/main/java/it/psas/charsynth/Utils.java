package it.psas.charsynth;

/**
 * Created by Project s.a.s. on 30/08/2015.
 * Copyright © 1996, 2015 PROJECT s.a.s. All Rights Reserved.
 */

import android.text.InputFilter;
import android.text.Spanned;

public class Utils {

    public static InputFilter editTextInputFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			return source.toString().replaceAll("[^a-zA-Z0-9 \n]", "");
        }
    };
}
