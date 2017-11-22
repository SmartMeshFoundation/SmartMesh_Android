package com.lingtuan.firefly.custom;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.util.Utils;


public class SubmitDialog extends Dialog {
 
    public SubmitDialog(Context context, int theme) {
        super(context, theme);
    }
 
    public SubmitDialog(Context context) {
        super(context);
    }
 
    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
 
        private Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;
        private int color;
        private DialogInterface.OnClickListener
                        positiveButtonClickListener,
                        negativeButtonClickListener;

        private SubmitDialog dialog;
        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Set the Dialog setCancelable
         * @return
         */
        public void setCancelable(boolean cancelable)
        {
        	if(dialog!=null)
        	{
        		dialog.setCancelable(cancelable);
        		dialog.setCanceledOnTouchOutside(cancelable);
        	}
        }
        /**
         * Set the Dialog message from String
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Set the Dialog message from resource
         * @return
         */
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        /**
         * Set the Dialog title from resource
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        /**
         * Set the Dialog title from String
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
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
         * Set the positive button resource and it's listener
         * @param positiveButtonText
         * @param listener
         * @param color
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText,
                                         DialogInterface.OnClickListener listener, int color) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            this.color = color;
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
 
      
        /**
         * Create the custom dialog
         */
        public SubmitDialog show() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final SubmitDialog dialog = new SubmitDialog(context, 
            		R.style.SubmitDialog);
            this.dialog=dialog;
            View layout = inflater.inflate(R.layout.submit_dialog_layout, null);
            dialog.addContentView(layout, new LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            // set the dialog title
            ((TextView) layout.findViewById(R.id.title)).setText(title);
            if(TextUtils.isEmpty(title)){
            	layout.findViewById(R.id.title).setVisibility(View.GONE);
            	layout.findViewById(R.id.title_line).setVisibility(View.GONE);
            }
            // set the confirm button
            if (positiveButtonText != null) {
                ((TextView) layout.findViewById(R.id.positiveButton))
                        .setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    layout.findViewById(R.id.positiveButton)
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
                layout.findViewById(R.id.positiveButton).setVisibility(
                        View.GONE);
                layout.findViewById(R.id.line2).setVisibility(
                        View.GONE);
            }
            
            if (color != 0)
            {
            	 ((TextView) layout.findViewById(R.id.positiveButton)).setTextColor(color);
            }
            // set the cancel button
            if (negativeButtonText != null) {
                ((TextView) layout.findViewById(R.id.negativeButton))
                        .setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    layout.findViewById(R.id.negativeButton)
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
                layout.findViewById(R.id.negativeButton).setVisibility(
                        View.GONE);
                layout.findViewById(R.id.line2).setVisibility(
                        View.GONE);
            }
            if (color != 0)
            {
            	 ((TextView) layout.findViewById(R.id.negativeButton)).setTextColor(color);
            }
            // set the content message
            if (message != null) {
                ((TextView) layout.findViewById(
                		R.id.message)).setText(message);
            } else if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
                ((LinearLayout) layout.findViewById(R.id.content))
                        .removeAllViews();
                
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT);
                lp.setMargins(Utils.dip2px(context, 10), 0, Utils.dip2px(context, 10), 0);
                ((LinearLayout) layout.findViewById(R.id.content))
                        .addView(contentView, lp);
            }
            dialog.setContentView(layout);
            dialog.show();
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);
            return dialog;
        }
       
   
    /**
     * Create the window custom dialog
     */
    public void showWindowManager(boolean hasTitle)
    {
    	  LayoutInflater inflater = (LayoutInflater) context
                  .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          // instantiate the dialog with the custom Theme
          final SubmitDialog dialog = new SubmitDialog(context, 
          		R.style.SubmitDialog);
          this.dialog=dialog;
          dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
          View layout = inflater.inflate(R.layout.submit_dialog_layout, null);
          dialog.addContentView(layout, new LayoutParams(
                  LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
          // set the dialog title
          if(hasTitle)
          {
              ((TextView) layout.findViewById(R.id.title)).setText(title);
          }
          else{
        	  layout.findViewById(R.id.title).setVisibility(View.GONE);
          }
         
          // set the confirm button
          if (positiveButtonText != null) {
              ((TextView) layout.findViewById(R.id.positiveButton))
                      .setText(positiveButtonText);
              if (positiveButtonClickListener != null) {
                  layout.findViewById(R.id.positiveButton)
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
              layout.findViewById(R.id.positiveButton).setVisibility(
                      View.GONE);
              layout.findViewById(R.id.line2).setVisibility(
                      View.GONE);
          }
          // set the cancel button
          if (negativeButtonText != null) {
              ((TextView) layout.findViewById(R.id.negativeButton))
                      .setText(negativeButtonText);
              if (negativeButtonClickListener != null) {
                  layout.findViewById(R.id.negativeButton)
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
              layout.findViewById(R.id.negativeButton).setVisibility(
                      View.GONE);
              layout.findViewById(R.id.line2).setVisibility(
                      View.GONE);
          }
          // set the content message
          if (message != null) {
              ((TextView) layout.findViewById(
              		R.id.message)).setText(message);
          } else if (contentView != null) {
              // if no message set
              // add the contentView to the dialog body
              ((LinearLayout) layout.findViewById(R.id.content))
                      .removeAllViews();
              
              LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
                      LayoutParams.MATCH_PARENT,
                      LayoutParams.WRAP_CONTENT);
              lp.setMargins(Utils.dip2px(context, 10), 0, Utils.dip2px(context, 10), 0);
              ((LinearLayout) layout.findViewById(R.id.content))
                      .addView(contentView, lp);
          }
          dialog.setContentView(layout);
          dialog.show();
          WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
          params.width = WindowManager.LayoutParams.MATCH_PARENT;
          params.height = WindowManager.LayoutParams.WRAP_CONTENT;
          dialog.getWindow().setAttributes(params);
    }

}
 
}
