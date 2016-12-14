	public void showWaitProgress(Context context, String message) {
        builder = new CustomWaitView.Builder(context).create(message);
        builder.show();

    }

	public void dismissWaitProgress() {
        builder.dismiss();
    }

/**
 * Created by zln on 16/2/24.
 */
public class CustomWaitView extends Dialog {

    public CustomWaitView(Context context, int dialog) {
        super(context, dialog);
    }

    public static class Builder {
        private Context context;
        private TextView textView;

        public Builder(Context context) {
            this.context = context;
        }

        public CustomWaitView create(String message) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final CustomWaitView dialog = new CustomWaitView(context, R.style.Dialog);
            //设置自己的布局
            View layout = inflater.inflate(R.layout.custom_wait_view, null);
            dialog.addContentView(layout, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            if (!message.equals("")) {
                textView = ((TextView) layout.findViewById(R.id.message));
                textView.setVisibility(View.VISIBLE);
                textView.setText(message);
            }

            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            return dialog;
        }

    }
}
