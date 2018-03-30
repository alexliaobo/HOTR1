package com.us.hotr.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.us.hotr.R;


/**
 * Created by Mloong on 2017/9/14.
 */

public class OneButtonDialog extends Dialog{

    public OneButtonDialog(Context context) {
        super(context);
    }

    public OneButtonDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context;
        private String message;
        private String positiveButtonText;

        private DialogInterface.OnClickListener positiveButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        public Builder setPositiveButton(int positiveButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        public OneButtonDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final OneButtonDialog dialog = new OneButtonDialog(context,
                    R.style.NoTitleDialog);
            View layout = inflater.inflate(R.layout.dialog_one_button, null);
            dialog.addContentView(layout, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            if (positiveButtonText != null) {
                ((TextView) layout.findViewById(R.id.tv_positive))
                        .setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    ((TextView) layout.findViewById(R.id.tv_positive))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveButtonClickListener.onClick(dialog,
                                            DialogInterface.BUTTON_POSITIVE);
                                }
                            });
                }
            }
            if (message != null) {
                ((TextView) layout.findViewById(R.id.tv_content)).setText(message);
            }
            dialog.setContentView(layout);
            return dialog;
        }
    }
}
