package net.compuways.keywordsmanager;

/**
 * Created by me on 5/23/16.
 */

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 *
 * Create custom Dialog windows for your application
 * Custom dialogs rely on custom layouts wich allow you to
 * create and use your own look & feel.
 *
 * Under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 *
 * @author antoine vianey
 *
 */
public class CustomDialog extends Dialog {

    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }

    public CustomDialog(Context context) {
        super(context);
    }

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {

        private Context context;
        private String positiveButtonText;
        private String neutralButtonText;
        private String negativeButtonText;
        private View contentView;
        private int bgID=-1,layoutID=0;
        private GridLayout gridLayout;
        private View layout;

        private DialogInterface.OnClickListener
                positiveButtonClickListener,
                neutralButtonClickListener,
                negativeButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }



        /**
         * Set a custom content view for the Dialog.
         * If a message is set, the contentView is not
         * added to the Dialog...
         * @param v
         * @return
         */
        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        /**
         * Set the positive button text and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(String positiveButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        /**
         * Set the negative button resource and it's listener
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(int negativeButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.negativeButtonText = (String) context
                    .getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }

        /**
         * Set the negative button text and it's listener
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(String negativeButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        public Builder setNeutralButton(String neutralButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.neutralButtonText = neutralButtonText;
            this.neutralButtonClickListener = listener;
            return this;
        }


        public Builder setBackground(int bgID) {
            this.bgID=bgID;
            return this;
        }

        public Builder setLayoutID(int layoutID) {
            this.layoutID=layoutID;
            return this;
        }


        public GridLayout getGridLayout(){

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            layout = inflater.inflate(layoutID, null);


                LinearLayout llayout = (LinearLayout) layout.findViewById(R.id.tlsDia);
                gridLayout = (GridLayout) layout.findViewById(R.id.gridlayout);

            return gridLayout;
        }

        public TextView getEditText(){

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            layout = inflater.inflate(layoutID, null);


            LinearLayout llayout = (LinearLayout) layout.findViewById(R.id.tlsDia);
            EditText edit = (EditText) layout.findViewById(R.id.input);

            return edit;
        }

        /**
         * Create the custom dialog
         */
        public CustomDialog create() {

            // instantiate the dialog with the custom Theme
            final CustomDialog dialog = new CustomDialog(context,
                    R.style.Dialog);

            if(bgID>0) {
                LinearLayout llayout = (LinearLayout) layout.findViewById(R.id.tlsDia);
                llayout.setBackground(ContextCompat.getDrawable(context, bgID));
            }

            dialog.addContentView(layout, new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            // set the confirm button
            if (positiveButtonText != null) {
                ((Button) layout.findViewById(R.id.positiveButton))
                        .setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.positiveButton))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveButtonClickListener.onClick(
                                            dialog,
                                            DialogInterface.BUTTON_POSITIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
               // layout.findViewById(R.id.positiveButton).setVisibility(
                //        View.GONE);
            }
            // set the cancel button
            if (negativeButtonText != null) {
                ((Button) layout.findViewById(R.id.negativeButton))
                        .setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.negativeButton))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    negativeButtonClickListener.onClick(
                                            dialog,
                                            DialogInterface.BUTTON_NEGATIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                //layout.findViewById(R.id.negativeButton).setVisibility(
                 //       View.GONE);
            }

            if (neutralButtonText != null) {
                ((Button) layout.findViewById(R.id.neutralButton))
                        .setText(neutralButtonText);
                if (neutralButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.neutralButton))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    neutralButtonClickListener.onClick(
                                            dialog,
                                            DialogInterface.BUTTON_NEGATIVE);
                                }
                            });
                }
            }

            dialog.setContentView(layout);
            return dialog;
        }

    }

}
