package com.hgy.mylib;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

public class SketchTextWatcher implements TextWatcher {
    private int editStart;
    private int editEnd;
    private int maxLen = 12; // 6 个汉字 12 个英文字符 (表情 2 个字符)

    private EditText editText;

    public SketchTextWatcher(EditText e, int maxLen) {
        editText = e;
        this.maxLen = maxLen*2;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        editStart = editText.getSelectionStart();
        editEnd = editText.getSelectionEnd();
        // 先去掉监听器，否则会出现栈溢出
        editText.removeTextChangedListener(this);
        if (!TextUtils.isEmpty(s.toString())) {
            while (calculateLength(s.toString()) > maxLen) {
                s.delete(--editStart, editEnd--);
            }
        }

        editText.setText(s);
        editText.setSelection(editStart);
        // 恢复监听器
        editText.addTextChangedListener(this);
    }
    /*
     *
     * 英文一个字符  中文两个字符*/

        /*private int calculateLength(String string) {
            char[] ch = string.toCharArray();

            int varlength = 0;
            for (char c : ch) {
                // changed by zyf 0825 , bug 6918，加入中文标点范围 ， TODO 标点范围有待具体化
                if ((c >= 0x2E80 && c <= 0xFE4F) || (c >= 0xA13F && c <= 0xAA40) || c >= 0x80) { // 中文字符范围0x4e00 0x9fbb
//                    if (c >= 0x4E00 && c <= 0x9FBB) { // 中文字符范围 0x4E00-0x9FA5 + 0x9FA6-0x9FBB
                    varlength = varlength + 2;
                } else {
                    varlength++;
                }
            }
            return varlength;
        }*/

    private int calculateLength(String string){
        int length = 0;
        char[] chars = string.toCharArray();
        for (char c : chars) {
            if (isChinese(c)) {
                length += 2;
            } else {
                length++;
            }
        }
        return length;
    }
    private boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }
}
